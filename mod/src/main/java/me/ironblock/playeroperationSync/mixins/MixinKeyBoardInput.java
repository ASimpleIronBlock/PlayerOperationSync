package me.ironblock.playeroperationSync.mixins;

import me.ironblock.playeroperationSync.PlayerOperationSyncMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyBoardInput {

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void onPreTick(CallbackInfo ci) {
        if (PlayerOperationSyncMod.isRunning) {//        mod启用
            if (PlayerOperationSyncMod.controlMode) {   //所有人互相控制
                KeyBinding.updatePressedStates();
                PlayerOperationSyncMod.sendKeyBindingsToServer();   //把自己的按键发出去
                //按照其他人的按键控制自己
                if (PlayerOperationSyncMod.forward&&PlayerOperationSyncMod.syncMap.get(3)){
                    MinecraftClient.getInstance().options.keyForward.setPressed(true);
                }
                if (PlayerOperationSyncMod.backward&&PlayerOperationSyncMod.syncMap.get(4)){
                    MinecraftClient.getInstance().options.keyBack.setPressed(true);
                }
                if (PlayerOperationSyncMod.left&&PlayerOperationSyncMod.syncMap.get(5)){
                    MinecraftClient.getInstance().options.keyLeft.setPressed(true);
                }
                if (PlayerOperationSyncMod.right&&PlayerOperationSyncMod.syncMap.get(6)){
                    MinecraftClient.getInstance().options.keyRight.setPressed(true);
                }
                if (PlayerOperationSyncMod.jump&&PlayerOperationSyncMod.syncMap.get(2)){
                    MinecraftClient.getInstance().options.keyJump.setPressed(true);
                }
                if (PlayerOperationSyncMod.sneak&&PlayerOperationSyncMod.syncMap.get(1)){
                    MinecraftClient.getInstance().options.keySneak.setPressed(true);
                }
                if (PlayerOperationSyncMod.sprint&&PlayerOperationSyncMod.syncMap.get(0)){
                    MinecraftClient.getInstance().options.keySprint.setPressed(true);
                }
            } else {            //单人控制模式
                        if (!PlayerOperationSyncMod.selfControllable){
                            if (PlayerOperationSyncMod.syncMap.get(3))
                            MinecraftClient.getInstance().options.keyForward.setPressed(PlayerOperationSyncMod.forward);
                            if (PlayerOperationSyncMod.syncMap.get(4))
                            MinecraftClient.getInstance().options.keyBack.setPressed(PlayerOperationSyncMod.backward);
                            if (PlayerOperationSyncMod.syncMap.get(6))
                            MinecraftClient.getInstance().options.keyRight.setPressed(PlayerOperationSyncMod.right);
                            if (PlayerOperationSyncMod.syncMap.get(5))
                            MinecraftClient.getInstance().options.keyLeft.setPressed(PlayerOperationSyncMod.left);
                            if (PlayerOperationSyncMod.syncMap.get(2))
                            MinecraftClient.getInstance().options.keyJump.setPressed(PlayerOperationSyncMod.jump);
                            if (PlayerOperationSyncMod.syncMap.get(1))
                            MinecraftClient.getInstance().options.keySneak.setPressed(PlayerOperationSyncMod.sneak);
                            if (PlayerOperationSyncMod.syncMap.get(0))
                            MinecraftClient.getInstance().options.keySprint.setPressed(PlayerOperationSyncMod.sprint);
                        }else {
                            PlayerOperationSyncMod.sendKeyBindingsToServer();
                        }

            }

        }

    }

}
