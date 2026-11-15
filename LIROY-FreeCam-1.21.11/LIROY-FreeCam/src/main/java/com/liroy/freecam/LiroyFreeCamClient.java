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
    private static Vec3d prevCameraPos = Vec3d.ZERO;
    private static Vec3d cameraPos = Vec3d.ZERO;
    private static float cameraYaw;
    private static float cameraPitch;

    private static Vec3d lockedPlayerPos = Vec3d.ZERO;
    private static float lockedPlayerYaw;
    private static float lockedPlayerPitch;

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
                prevCameraPos = cameraPos;
                cameraYaw = client.player.getYaw();
                cameraPitch = client.player.getPitch();

                lockedPlayerPos = client.player.getPos();
                lockedPlayerYaw = client.player.getYaw();
                lockedPlayerPitch = client.player.getPitch();
            } else {
                client.player.setYaw(lockedPlayerYaw);
                client.player.setPitch(lockedPlayerPitch);
            }
        }

        if (!enabled || client.player == null || client.world == null) {
            return;
        }

        // קריאת סיבוב העכבר למצלמה, והחזרת ראש השחקן למקום הנעול
        float yawDelta = client.player.getYaw() - lockedPlayerYaw;
        float pitchDelta = client.player.getPitch() - lockedPlayerPitch;
        cameraYaw += yawDelta;
        cameraPitch = MathHelper.clamp(cameraPitch + pitchDelta, -90.0F, 90.0F);

        client.player.setYaw(lockedPlayerYaw);
        client.player.setPitch(lockedPlayerPitch);
        client.player.prevYaw = lockedPlayerYaw;
        client.player.prevPitch = lockedPlayerPitch;

        // נעילת השחקן במקום כדי שלא יזוז בזמן שמטיסים את המצלמה
        client.player.setPosition(lockedPlayerPos);
        client.player.setVelocity(Vec3d.ZERO);

        // שמירת המיקום הקודם של המצלמה לצורך תנועה חלקה (Interpolation)
        prevCameraPos = cameraPos;

        Vec3d look = getLookVector(cameraYaw, cameraPitch);
        Vec3d right = new Vec3d(-look.z, 0.0, look.x).normalize();
        Vec3d movement = Vec3d.ZERO;

        long window = client.getWindow().getHandle();
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_W)) movement = movement.add(look.x, 0, look.z);
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_S)) movement = movement.add(-look.x, 0, -look.z);
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_A)) movement = movement.add(-right.x, 0, -right.z);
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_D)) movement = movement.add(right.x, 0, right.z);
        if (upKey.isPressed()) movement = movement.add(0, 1, 0);
        if (downKey.isPressed()) movement = movement.add(0, -1, 0);

        if (movement.lengthSquared() > 0) {
            double speed = speedKey.isPressed() ? 1.5 : 0.45;
            cameraPos = cameraPos.add(movement.normalize().multiply(speed));
        }
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

    public static Vec3d getSmoothCameraPos(float tickDelta) {
        return prevCameraPos.lerp(cameraPos, tickDelta);
    }

    public static float getCameraYaw() {
        return cameraYaw;
    }

    public static float getCameraPitch() {
        return cameraPitch;
    }
}
