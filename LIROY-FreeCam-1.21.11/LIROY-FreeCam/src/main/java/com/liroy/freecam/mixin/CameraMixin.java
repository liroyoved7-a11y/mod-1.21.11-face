package com.liroy.freecam.mixin;

import com.liroy.freecam.LiroyFreeCamClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "update", at = @At("TAIL"))
    private void liroyFreeCam$detachCamera(CallbackInfo ci) {
        if (!LiroyFreeCamClient.isEnabled()) {
            return;
        }

        Camera camera = (Camera)(Object)this;
        Vec3d pos = LiroyFreeCamClient.getCameraPos();
        camera.setPos(pos.x, pos.y, pos.z);
        camera.setRotation(LiroyFreeCamClient.getCameraYaw(), LiroyFreeCamClient.getCameraPitch());
    }
}
