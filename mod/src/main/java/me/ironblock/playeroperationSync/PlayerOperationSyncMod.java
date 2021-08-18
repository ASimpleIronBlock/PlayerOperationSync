package me.ironblock.playeroperationSync;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PlayerOperationSyncMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static Identifier identifier = new Identifier("iron__block:keystate");
    public static boolean isRunning = false;
    public static boolean selfControllable = false;
    //是一个人控制所有人还是所有人互相控制 false:一个人控制所有人,true:所有人互相控制
    public static boolean controlMode = false;
    public static boolean forward = false;
    public static boolean backward = false;
    public static boolean left = false;
    public static boolean right = false;
    public static boolean jump = false;
    public static boolean sneak = false;
    public static boolean sprint = false;

    public static Map<Integer, Boolean> syncMap = new HashMap<>();
    static {
        syncMap.put(0,true);
        syncMap.put(1,true);
        syncMap.put(2,true);
        syncMap.put(3,true);
        syncMap.put(4,true);
        syncMap.put(5,true);
        syncMap.put(6,true);
    }

    @Override
    public void onInitialize() {
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
        ClientPlayNetworking.registerGlobalReceiver(identifier, (client, handler, buf, responseSender) -> {
            byte[] message = new byte[buf.capacity()];
            buf.resetReaderIndex();
            buf.readBytes(message, 0, message.length);
            for (byte theByte : message) {
                if (theByte == (byte) 0xff) {
                    selfControllable = true;
                    KeyBinding.updatePressedStates();
                    return;
                }
                if (theByte == (byte) 0xfe) {
                    selfControllable = false;
                    KeyBinding.updatePressedStates();
                    return;
                }
                if (theByte == (byte) 0xfd) {
                    isRunning = true;
                }
                if (theByte == (byte) 0xfc) {
                    isRunning = false;
                }
                if (theByte == (byte) 0xfb) {
                    controlMode = true;
                    KeyBinding.updatePressedStates();
                }
                if (theByte == (byte) 0xfa) {
                    controlMode = false;
                    KeyBinding.updatePressedStates();
                }
                if (theByte == (byte) 0xf9) {
                    isRunning = false;
                    selfControllable = true;
                    controlMode = false;
                    forward = false;
                    backward = false;
                    left = false;
                    right = false;
                    jump = false;
                    sneak = false;
                    sprint = false;
                }
                if (theByte == (byte) 0xf8) {
                    forward = false;
                    backward = false;
                    left = false;
                    right = false;
                    jump = false;
                    sneak = false;
                    sprint = false;
                }
                if (theByte == (byte) 0xaa){
                    MinecraftClient.getInstance().player.sendChatMessage("mod已安装");
                }


                int operationType = (theByte&(byte) 0xf0) >> 4;
                int operationState = (theByte&(byte) 0x0f);
                LOGGER.info("operationType:" + operationType + ",operationState:" + operationState);
                if (operationState==0||operationState==1) {
                    switch (operationType) {
                        case 0 -> {if (syncMap.get(0))sprint = operationState == 1;}
                        case 1 -> {if (syncMap.get(1))sneak = operationState == 1;}
                        case 2 -> {if (syncMap.get(2))jump = operationState == 1;}
                        case 3 -> {if (syncMap.get(3))forward = operationState == 1;}
                        case 4 -> {if (syncMap.get(4))backward = operationState == 1;}
                        case 5 -> {if (syncMap.get(5))left = operationState == 1;}
                        case 6 -> {if (syncMap.get(6))right = operationState == 1;}
                    }
                }else{
                    syncMap.put(operationType, operationState == 3);
                }
            }
        });

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

    private static boolean lastTickSprintPressed = false;
    private static boolean lastTickSneakPressed = false;
    private static boolean lastTickJumpPressed = false;
    private static boolean lastTickForwardPressed = false;
    private static boolean lastTickBackPressed = false;
    private static boolean lastTickLeftPressed = false;
    private static boolean lastTickRightPressed = false;

    public static void sendKeyBindingsToServer() {
        PacketByteBuf buf = PacketByteBufs.create();
        if (MinecraftClient.getInstance().options.keySprint.isPressed() != lastTickSprintPressed)
            buf.writeByte(writeToInt((byte) 0, MinecraftClient.getInstance().options.keySprint.isPressed()));
        if (MinecraftClient.getInstance().options.keySneak.isPressed() != lastTickSneakPressed)
            buf.writeByte(writeToInt((byte) 1, MinecraftClient.getInstance().options.keySneak.isPressed()));
        if (MinecraftClient.getInstance().options.keyJump.isPressed() != lastTickJumpPressed)
            buf.writeByte(writeToInt((byte) 2, MinecraftClient.getInstance().options.keyJump.isPressed()));
        if (MinecraftClient.getInstance().options.keyForward.isPressed() != lastTickForwardPressed)
            buf.writeByte(writeToInt((byte) 3, MinecraftClient.getInstance().options.keyForward.isPressed()));
        if (MinecraftClient.getInstance().options.keyBack.isPressed() != lastTickBackPressed)
            buf.writeByte(writeToInt((byte) 4, MinecraftClient.getInstance().options.keyBack.isPressed()));
        if (MinecraftClient.getInstance().options.keyLeft.isPressed() != lastTickLeftPressed)
            buf.writeByte(writeToInt((byte) 5, MinecraftClient.getInstance().options.keyLeft.isPressed()));
        if (MinecraftClient.getInstance().options.keyRight.isPressed() != lastTickRightPressed)
            buf.writeByte(writeToInt((byte) 6, MinecraftClient.getInstance().options.keyRight.isPressed()));
        if (buf.capacity()!=0)
        ClientPlayNetworking.send(identifier, buf);
        lastTickSprintPressed = MinecraftClient.getInstance().options.keySprint.isPressed();
        lastTickSneakPressed = MinecraftClient.getInstance().options.keySneak.isPressed();
        lastTickJumpPressed = MinecraftClient.getInstance().options.keyJump.isPressed();
        lastTickForwardPressed = MinecraftClient.getInstance().options.keyForward.isPressed();
        lastTickBackPressed = MinecraftClient.getInstance().options.keyBack.isPressed();
        lastTickLeftPressed = MinecraftClient.getInstance().options.keyLeft.isPressed();
        lastTickRightPressed = MinecraftClient.getInstance().options.keyRight.isPressed();
    }


    private static byte writeToInt(byte operation, boolean state) {
        return ((byte) (operation << 4|(state ? 1 : 0)));
    }
}
