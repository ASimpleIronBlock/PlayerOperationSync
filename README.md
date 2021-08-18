# PlayerOperationSync
玩家的移动 跳跃和潜行同步


PlayerOperationSychronizePlugin.jar的是服务器插件,版本为spigot 1.17.1
PlayerOperationSyncMod-1.0.0.jar是客户端的mod,版本为fabric 1.17.1

本模组有三种轮换控制者的模式:
		单人轮流控制:控制者是一个人,他可以控制每个人的操作,控制者每隔一段时间会轮换,默认轮换时间为5min,可以通过指令修改
		
		先单人轮流控制,然后多人同时控制:单人轮流控制时同上,但是在所有玩家都轮换为控制者之后,会进入多人控制状态,然后经过轮换的时间后,重新进入单人轮流控制状态
		
		多人同时控制: 每个人都是控制者,你的操作会可以控制其他人,其他人的操作也可以控制你
		
同步的操作可以通过修改配置文件修改:
		配置文件的路径:服务器文件夹\plugins\playerOperationSychronize\config.yml (这个配置文件要加入插件后启动一次服务器才会自动生成)
			配置文件的内容:
				syncMove:同步移动,设为true为同步,设为false为不同步
				syncSprint:同步疾跑,设为true为同步,设为false为不同步
				syncSneak:同步潜行,设为true为同步设为false为不同步
				syncJump:同步跳跃,设为true为同步,设为false为不同步
## 命令列表:
	-/pluginStart 开始运行插件
         -/pluginStop 停止运行插件
         -/nextOperator 立即将当前控制者设为下一个人
         -/setOperator <name>立即将当前控制者设为指定的人
         -/setOperatorChangeTime <number> 设置轮换控制者的时间(tick)
         -/setLoopMode <number>设置操控者的循环方式 0->单人轮流控制,1->先单人轮流控制,然后多人同时控制,2->多人同时控制
         -/resetAll 重置所有人mod的所有状态
         -/resetKey 重置所有人的按键状态
          -/pluginHelp 打印此文本 
	  -/testPlayer <playername>测试<PlayerName>有没有成功安装配套的模组
## 使用具体方法:
			先执行/pluginStart 开启插件
			然后执行/setloopmode设置想要的模式
			如果出现没有效果的情况 执行/reload
			如果还不行 使用指令/testPlayer检查玩家是否成功安装模组,如果玩家在执行指令后说了"mod已安装"即为安装成功,否则为安装失败
			
## 开发人员名单:
	作者:Iron__Block
	测试:@爱吃鲤鱼的御坂酱呀  uid:1226669900
	     @雙囍臨門  uid:670477948

  
 
