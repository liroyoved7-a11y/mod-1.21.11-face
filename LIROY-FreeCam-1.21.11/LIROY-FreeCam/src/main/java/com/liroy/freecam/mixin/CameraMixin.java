package com.liroy.freecam.mixin;

import com.liroy.freecam.LiroyFreeCamClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"))
    private void liroyFreeCam$detachCamera(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!LiroyFreeCamClient.isEnabled()) {
            return;
        }

        Vec3d pos = LiroyFreeCamClient.getSmoothCameraPos(tickDelta);
        this.setPos(pos.x, pos.y, pos.z);
        this.setRotation(LiroyFreeCamClient.getCameraYaw(), LiroyFreeCamClient.getCameraPitch());
    }
}
