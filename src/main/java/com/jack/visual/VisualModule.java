package com.jack.visual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class VisualModule implements ClientModInitializer {
    public static boolean killAura = false, fly = false;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((m, t) -> {
            if (mc.player == null || mc.options.hudHidden) return;
            mc.textRenderer.drawWithShadow(m, Text.literal("FPS: " + mc.getCurrentFps()), 5, 5, 0xFFFFFF);
            mc.textRenderer.drawWithShadow(m, Text.literal(String.format("XYZ: %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ())), 5, 17, 0xFFFFFF);
            mc.textRenderer.drawWithShadow(m, Text.literal("KillAura [R]: " + (killAura ? "ON" : "OFF")), 5, 29, 0xFFFFFF);
            mc.textRenderer.drawWithShadow(m, Text.literal("Fly [G]: " + (fly ? "ON" : "OFF")), 5, 41, 0xFFFFFF);
        });

        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if (c.player == null) return;
            if (net.minecraft.client.util.InputUtil.isKeyPressed(c.getWindow().getHandle(), GLFW.GLFW_KEY_R)) {
                killAura = !killAura;
                c.player.sendMessage(Text.literal("KA: " + (killAura ? "ON" : "OFF")), true);
            }
            if (net.minecraft.client.util.InputUtil.isKeyPressed(c.getWindow().getHandle(), GLFW.GLFW_KEY_G)) {
                fly = !fly;
                c.player.sendMessage(Text.literal("Fly: " + (fly ? "ON" : "OFF")), true);
            }
            if (fly) {
                c.player.getAbilities().allowFlying = true;
                c.player.getAbilities().flying = true;
                c.player.sendAbilitiesUpdate();
            }
            if (killAura) {
                Entity t = null; double cl = 3.0;
                for (Entity e : c.world.getEntities()) {
                    if (e == c.player || !e.isAlive() || !(e instanceof LivingEntity)) continue;
                    double d = c.player.distanceTo(e);
                    if (d < cl) { cl = d; t = e; }
                }
                if (t != null) {
                    double dx = t.getX()-c.player.getX(), dy = t.getEyeY()-c.player.getEyeY(), dz = t.getZ()-c.player.getZ();
                    float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
                    float pitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx*dx+dz*dz))));
                    c.player.setYaw(yaw); c.player.setPitch(pitch);
                    if (c.interactionManager != null) {
                        c.interactionManager.attackEntity(c.player, t);
                        c.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }
        });
    }
}
