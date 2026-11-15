package com.liroy.freecam;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class LiroyFreeCamClient implements ClientModInitializer {
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("liroy_freecam", "general"));

    private static KeyBinding toggleKey;
    private static KeyBinding upKey;
    private static KeyBinding downKey;
    private static KeyBinding speedKey;

    private static boolean enabled = false;
    private static Vec3d cameraPos = Vec3d.ZERO;
    private static float cameraYaw;
    private static float cameraPitch;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.liroy_freecam.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));
        upKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.liroy_freecam.up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_SPACE,
                CATEGORY
        ));
        downKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.liroy_freecam.down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL,
                CATEGORY
        ));
        speedKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.liroy_freecam.speed", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(LiroyFreeCamClient::tick);
    }

    private static void tick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            if (client.player == null || client.world == null) {
                enabled = false;
                return;
            }
            enabled = !enabled;
            if (enabled) {
                cameraPos = client.player.getCameraPosVec(1.0F);
                cameraYaw = client.player.getYaw();
                cameraPitch = client.player.getPitch();
            }
        }

        if (!enabled || client.player == null || client.world == null) {
            return;
        }

        Vec3d look = getLookVector(cameraYaw, cameraPitch);
        Vec3d right = new Vec3d(-look.z, 0.0, look.x).normalize();
        Vec3d movement = Vec3d.ZERO;

        if (client.options.forwardKey.isPressed()) movement = movement.add(look.x, 0, look.z);
        if (client.options.backKey.isPressed()) movement = movement.add(-look.x, 0, -look.z);
        if (client.options.leftKey.isPressed()) movement = movement.add(-right.x, 0, -right.z);
        if (client.options.rightKey.isPressed()) movement = movement.add(right.x, 0, right.z);
        if (upKey.isPressed()) movement = movement.add(0, 1, 0);
        if (downKey.isPressed()) movement = movement.add(0, -1, 0);

        if (movement.lengthSquared() > 0) {
            double speed = speedKey.isPressed() ? 1.2 : 0.35;
            cameraPos = cameraPos.add(movement.normalize().multiply(speed));
        }

        cameraYaw = client.player.getYaw();
        cameraPitch = MathHelper.clamp(client.player.getPitch(), -90.0F, 90.0F);
    }

    private static Vec3d getLookVector(float yaw, float pitch) {
        float yawRad = yaw * ((float)Math.PI / 180.0F);
        float pitchRad = pitch * ((float)Math.PI / 180.0F);
        float cosPitch = MathHelper.cos(pitchRad);
        return new Vec3d(
                -MathHelper.sin(yawRad) * cosPitch,
                -MathHelper.sin(pitchRad),
                MathHelper.cos(yawRad) * cosPitch
        ).normalize();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static Vec3d getCameraPos() {
        return cameraPos;
    }

    public static float getCameraYaw() {
        return cameraYaw;
    }

    public static float getCameraPitch() {
        return cameraPitch;
    }
}
