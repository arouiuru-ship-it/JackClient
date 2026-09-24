package com.jack.visual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class VisualModule implements ClientModInitializer {
    public static boolean killAura = false, fly = false, crits = true;
    public static boolean targetPlayersOnly = true;
    public static double auraRange = 3.0;
    public static int auraCps = 12;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static int tick = 0;
    private static boolean rshiftHeld = false;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((ctx, t) -> {
            if (mc.player == null || mc.options.hudHidden) return;
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("FPS " + mc.getCurrentFps()), 5, 5, 0xFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(String.format("XYZ %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ())), 5, 17, 0xFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("KA " + (killAura ? "ON" : "OFF")), 5, 29, 0xFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("Crit " + (crits ? "ON" : "OFF")), 55, 29, 0xFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("Fly " + (fly ? "ON" : "OFF")), 115, 29, 0xFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("[RShift] Menu"), 5, 41, 0xFFFFFF);
        });
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if (c.player == null) return;
            boolean pressed = InputUtil.isKeyPressed(c.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (pressed && !rshiftHeld) { rshiftHeld = true; c.setScreen(new MenuScreen()); }
            if (!pressed) rshiftHeld = false;
            if (fly) {
                c.player.getAbilities().allowFlying = true;
                c.player.getAbilities().flying = true;
                c.player.sendAbilitiesUpdate();
            } else if (!c.player.isCreative() && !c.player.isSpectator()) {
                c.player.getAbilities().allowFlying = false;
                c.player.getAbilities().flying = false;
            }
            if (!killAura) return;
            tick++;
            if (tick < (20 / auraCps)) return;
            tick = 0;
            Entity t = null; double cl = auraRange;
            for (Entity e : c.world.getEntities()) {
                if (e == c.player || !e.isAlive()) continue;
                if (targetPlayersOnly && !(e instanceof PlayerEntity)) continue;
                if (!targetPlayersOnly && !(e instanceof LivingEntity)) continue;
                double d = c.player.distanceTo(e);
                if (d < cl) { cl = d; t = e; }
            }
            if (t != null) {
                double dx = t.getX()-c.player.getX(), dy = t.getEyeY()-c.player.getEyeY(), dz = t.getZ()-c.player.getZ();
                float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
                float pitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx*dx+dz*dz))));
                c.player.setYaw(yaw); c.player.setPitch(pitch);
                if (crits && c.player.networkHandler != null) {
                    c.player.fallDistance = 0.1F;
                    c.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(c.player.getX(), c.player.getY() + 0.0625, c.player.getZ(), false, false));
                    c.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(c.player.getX(), c.player.getY(), c.player.getZ(), false, false));
                }
                if (c.interactionManager != null) {
                    c.interactionManager.attackEntity(c.player, t);
                    c.player.swingHand(Hand.MAIN_HAND);
                }
            }
        });
    }
}
