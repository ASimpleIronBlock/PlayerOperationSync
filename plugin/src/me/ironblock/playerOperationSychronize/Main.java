package me.ironblock.playerOperationSychronize;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String identifier = "iron__block:keystate";
    public static boolean running = false;
    private boolean controlEachOther = false;
    private int currentOperator = -1;
    private final Timer timer = new Timer(5 * 60 * 20);
    //循环方式:0:单人控制循环,1:单人-多人控制循环,2.多人控制
    private static int loopMode = 0;

    private static boolean syncSprint = true;
    private static boolean syncMove = true;
    private static boolean syncSneak = true;
    private static boolean syncJump = true;


    public static final String PROTOCOL = " /*\n" + "         * 如果字节是0xff,则表示设定为控制者\n" + "         * 如果字节是0xfe.则取消设定为控制者\n" + "         * 如果字节是0xfd,则表示启用mod\n" + "         * 如果字节是0xfc,则表示禁用mod\n" + "         * 如果字节是0xfb,则表示调整为互相控制模式\n" + "         * 如果字节是0xfa,则表示调整为单人控制模式\n" + "         * \n" + "         * 其余:\n" + "         *      前四个bit表示操纵对象:\n" + "         *                      0:疾跑\n" + "         *                      1:潜行\n" + "         *                      2:跳跃\n" + "         *                      3:前进\n" + "         *                      4:后退\n" + "         *                      5:向左\n" + "         *                      6:向右\n" + "         *      后四个bit表示操纵状态:0->false,1->true\n" + "         *\n" + "         *\n" + "         */;";
    public static final String pluginHelp = """
            此插件/模组 的功能是让所有人的移动,疾跑,潜行同步,并按照设置定时轮换操控者.
            插件的指令列表:
               -/pluginStart 开始运行插件
               -/pluginStop 停止运行插件
               -/nextOperator 立即将当前控制者设为下一个人
               -/setOperator <name>立即将当前控制者设为指定的人
               -/setOperatorChangeTime 设置轮换控制者的时间(tick)
               -/setLoopMode <numbers>设置操控者的循环方式 0->单人轮流控制,1->先单人轮流控制,然后多人同时控制,2->多人同时控制
               -/resetAll 重置所有人mod的所有状态
               -/resetKey 重置所有人的按键状态
               -/pluginHelp 打印此文本\040
            
            """;
    /*
     * 如果字节是0xff,则表示设定为控制者
     * 如果字节是0xfe.则取消设定为控制者
     * 如果字节是0xfd,则表示启用mod
     * 如果字节是0xfc,则表示禁用mod
     * 如果字节是0xfb,则表示调整为互相控制模式
     * 如果字节是0xfa,则表示调整为单人控制模式
     * 如果字节是0xf9,则表示重置客户端所有状态
     * 如果字节是0xf8,则表示重置客户端按键有关状态
     * 设置:
     *      前四个bit表示操纵对象:
     *                      10:疾跑
     *                      11:潜行
     *                      12:跳跃
     *                      13:前进
     *                      14:后退
     *                      15:向左
     *                      16:向右
     *      后四个bit表示操纵状态:0->false,1->true
     * 其余:
     *      前四个bit表示操纵对象:
     *                      0:疾跑
     *                      1:潜行
     *                      2:跳跃
     *                      3:前进
     *                      4:后退
     *                      5:向左
     *                      6:向右
     *      后四个bit表示操纵状态:0->false,1->true
     *
     *
     */
    @Override
    public void onEnable(){
        //config
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        syncSprint = config.getBoolean("syncSprint");
        syncMove = config.getBoolean("syncMove");
        syncSneak = config.getBoolean("syncSneak");
        syncJump = config.getBoolean("syncJump");
        getServer().getPluginManager().registerEvents(this,this);
        getServer().getMessenger().registerIncomingPluginChannel(this, identifier, this::handleKeyStates);
        getServer().getMessenger().registerOutgoingPluginChannel(this, identifier);

        try {
            resetAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getServer().getPluginCommand("pluginStart").setExecutor((commandSender, command, s, strings) -> {
            running = true;
            getServer().broadcastMessage("启动插件");
            sendConfigPacket();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendPluginMessage(this,identifier,new byte[]{(byte) 0xfd});

            }
            return true;
        });
        getServer().getPluginCommand("pluginStop").setExecutor((commandSender, command, s, strings) -> {
            running = false;
            getServer().broadcastMessage("禁用插件");
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendPluginMessage(this,identifier,new byte[]{(byte) 0xfc});
            }
            return true;
        });
        getServer().getPluginCommand("nextOperator").setExecutor((commandSender, command, s, strings) -> {
            if (running)
           nextOperator();
            else
                commandSender.sendMessage("插件尚未启动");
            return true;
        });
        getServer().getPluginCommand("setOperatorChangeTime").setExecutor((commandSender, command, s, strings) -> {
            if (running){
                timer.setTime(Integer.parseInt(strings[0]));
                Bukkit.broadcastMessage("玩家轮换时间已设置为" + (Integer.parseInt(strings[0]) + "(tick)"));
            } else
                commandSender.sendMessage("插件尚未启动");

            return true;
        });
        getServer().getPluginCommand("setOperator").setExecutor((commandSender, command, s, strings) -> {
            if (running) {
                List<? extends Player> onLinePlayers = Bukkit.getOnlinePlayers().stream().toList();
                Player player = Bukkit.getPlayerExact(strings[0]);
                int index = onLinePlayers.indexOf(player);
                if (index!=-1){
                    Player newOperator = onLinePlayers.get(index);
                    currentOperator = index;
                    for (Player onLinePlayer : onLinePlayers) {
                        onLinePlayer.sendPluginMessage(this, identifier, new byte[]{(byte) 0xfe});
                    }
                    newOperator.sendPluginMessage(this, identifier, new byte[]{(byte) 0xff});
                }else{
                    return false;
                }
            }else
                commandSender.sendMessage("插件尚未启动");
            return true;
        });
        getServer().getPluginCommand("setLoopMode").setExecutor((commandSender, command, s, strings) -> {
            loopMode = Integer.parseInt(strings[0]);
            StringBuilder sb = new StringBuilder();
            sb.append("已将游戏模式设置为:");
            switch (loopMode){
                case 0->sb.append("单人轮流控制");
                case 1-> sb.append("单人->多人轮流控制");
                case 2-> sb.append("多人轮流控制");
            }
            timer.updateNow();
            getServer().broadcastMessage(sb.toString());
            return true;
        });
        getServer().getPluginCommand("resetAll").setExecutor((commandSender, command, s, strings) -> {
            resetAll();
            return true;
        });
        getServer().getPluginCommand("resetKey").setExecutor((commandSender, command, s, strings) -> {
            resetKey();
            return true;
        });
        getServer().getPluginCommand("pluginHelp").setExecutor((commandSender, command, s, strings) -> {
            commandSender.sendMessage(pluginHelp);
            return true;
        });
        getServer().getPluginCommand("testPlayer").setExecutor((commandSender, command, s, strings) -> {
            Player player = Bukkit.getPlayerExact(strings[0]);
            getServer().broadcastMessage("测试" + player.getName() + "是否安装了mod");
            player.sendPluginMessage(this,identifier,new byte[]{(byte) 0xaa});
            return true;
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                onTick();
            }
        }.runTaskTimer(this, 1, 1);

    }
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event){
        event.getPlayer().sendPluginMessage(this,identifier,new byte[]{(byte) (running?0xfd:0xfc)});
        sendConfigPacket();
    }

    public void onTick() {
        try {
            if (running) {
                Collection<? extends Player> onLinePlayers = Bukkit.getOnlinePlayers();
                if (currentOperator==-1){
                    nextOperator();
                }
                if (onLinePlayers.size()>0) {

                    if (timer.update()) {
                        timer.reset();
                        switch (loopMode){
                            case 0->{
                                controlEachOther = false;
                                syncControlEachOther();
                                nextOperator();
                            }
                            case 1 ->{
                                if (currentOperator == onLinePlayers.size()-1) {
                                    getServer().broadcastMessage("已设置为互相控制模式");
                                    controlEachOther = true;
                                    syncControlEachOther();
                                    currentOperator++;
                                }else{
                                    if (controlEachOther){
                                        controlEachOther = false;
                                        syncControlEachOther();
                                    }
                                    nextOperator();
                                }
                            }
                            case 2->{
                                controlEachOther = true;
                                syncControlEachOther();
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /*
     * 如果字节是0xff,则表示设定为控制者
     * 如果字节是0xfe.则取消设定为控制者
     * 如果字节是0xfd,则表示启用mod
     * 如果字节是0xfc,则表示禁用mod
     * 如果字节是0xfb,则表示调整为互相控制模式
     * 如果字节是0xfa,则表示调整为单人控制模式
     *
     * 其余:
     *      前四个bit表示操纵对象:
     *                      0:疾跑
     *                      1:潜行
     *                      2:跳跃
     *                      3:前进
     *                      4:后退
     *                      5:向左
     *                      6:向右
     *      后四个bit表示操纵状态:0->false,1->true
     *
     *
     */

    public void handleKeyStates(String channel, Player player, byte[] message) {
        if (Objects.equals(channel, identifier)) {
            if (controlEachOther||isOperator(player)) {
                for (Player onLinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!onLinePlayer.equals(player)){
                        onLinePlayer.sendPluginMessage(this, identifier, message);
                    }

                }
            }else{
                LOGGER.warn(player.getName()+"不是控制者,但是他发了包,包的内容"+Arrays.toString(message));
            }
        }
    }
    //TODO:所有人都互相控制版本
    private void nextOperator(){
        if (loopMode!=2) {

            Collection<? extends Player> onLinePlayers = Bukkit.getOnlinePlayers();
            timer.reset();
            for (Player onLinePlayer : onLinePlayers) {
                onLinePlayer.sendPluginMessage(this, identifier, new byte[]{(byte) 0xfe});
            }
            currentOperator++;
            if (currentOperator >= onLinePlayers.size()) {
                currentOperator = 0;
            }
            Player newOperator = onLinePlayers.stream().toList().get(currentOperator);

            newOperator.sendPluginMessage(this, identifier, new byte[]{(byte) 0xff});
            getServer().broadcastMessage("已将" + newOperator.getName() + "设为控制者");
            new BukkitRunnable() {
                @Override
                public void run() {
                    resetKey();
                }
            }.runTaskLater(this, 1);
            syncControlEachOther();

        }else{
            getServer().broadcastMessage("多人控制无法设置控制者");
        }
    }

    private void syncControlEachOther(){
        Collection<? extends Player> onLinePlayers = Bukkit.getOnlinePlayers();
        for (Player onLinePlayer : onLinePlayers) {
            onLinePlayer.sendPluginMessage(this, identifier, new byte[]{controlEachOther?(byte) 0xfb:(byte) 0xfa});
        }
    }

    private void resetAll(){
        List<? extends Player> onLinePlayers = Bukkit.getOnlinePlayers().stream().toList();
        for (Player onLinePlayer : onLinePlayers) {
            onLinePlayer.sendPluginMessage(this, identifier, new byte[]{(byte) 0xf9});
        }
    }
    private void resetKey(){
        List<? extends Player> onLinePlayers = Bukkit.getOnlinePlayers().stream().toList();
        for (Player onLinePlayer : onLinePlayers) {
            onLinePlayer.sendPluginMessage(this, identifier, new byte[]{(byte) 0xf8});
        }
    }

    private boolean isOperator(Player player){
        List<? extends Player> onLinePlayers = Bukkit.getOnlinePlayers().stream().toList();
        if(currentOperator>=onLinePlayers.size()||currentOperator==-1){
            return false;
        }
        return onLinePlayers.get(currentOperator).equals(player);
    }
    /*
     * 如果字节是0xff,则表示设定为控制者
     * 如果字节是0xfe.则取消设定为控制者
     * 如果字节是0xfd,则表示启用mod
     * 如果字节是0xfc,则表示禁用mod
     * 如果字节是0xfb,则表示调整为互相控制模式
     * 如果字节是0xfa,则表示调整为单人控制模式
     * 如果字节是0xf9,则表示重置客户端所有状态
     * 如果字节是0xf8,则表示重置客户端按键有关状态
     * 如果字节是0xaa,则表示检测客户端是否安装成功了mod
     * 设置:
     *      前四个bit表示操纵对象:
     *                      10:疾跑
     *                      11:潜行
     *                      12:跳跃
     *                      13:前进
     *                      14:后退
     *                      15:向左
     *                      16:向右
     *      后四个bit表示操纵状态:0->false,1->true
     * 其余:
     *      前四个bit表示操纵对象:
     *                      0:疾跑
     *                      1:潜行
     *                      2:跳跃
     *                      3:前进
     *                      4:后退
     *                      5:向左
     *                      6:向右
     *      后四个bit表示操纵状态:0->false,1->true
     *
     *
     */
    private void sendConfigPacket(){
        byte[] bytes = new byte[8];
        bytes[0] = ((byte) (0 << 4|(syncSprint ? 3 : 2)));
        bytes[1] = ((byte) (1 << 4|(syncSneak ? 3 : 2)));
        bytes[2] = ((byte) (2 << 4|(syncJump ? 3 : 2)));
        bytes[3] = ((byte) (3 << 4|(syncMove ? 3 : 2)));
        bytes[4] = ((byte) (4 << 4|(syncMove ? 3 : 2)));
        bytes[5] = ((byte) (5 << 4|(syncMove ? 3 : 2)));
        bytes[6] = ((byte) (6 << 4|(syncMove ? 3 : 2)));
        for (byte aByte : bytes) {
            int operationType = (aByte&(byte) 0xf0) >> 4;
            int operationState = (aByte&(byte) 0x0f);
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendPluginMessage(this,identifier,bytes);
        }
    }


}
