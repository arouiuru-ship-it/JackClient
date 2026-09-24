package com.jack.visual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

public class VisualModule implements ClientModInitializer {
    public static boolean killAura = false, fly = false, autoSprint = false;
    public static boolean fullbright = false, aimAssist = false, hud = true, esp = false;
    public static String critMode = "Packet";
    public static float aimSmooth = 0.35f;
    public static float flySpeed = 0.05f;
    public static int auraCps = 4;
    public static double auraRange = 3.0;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static int tick = 0, critStage = 0;
    private static boolean rshiftHeld = false;
    private static Entity pendingTarget = null;
    private static float origGamma = 0f;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((ctx, t) -> {
            if (!hud || mc.player == null || mc.options.hudHidden) return;
            int x = 6, y = 6;
            ctx.fill(x - 3, y - 3, x + 130, y + 56, 0x66000000);
            ctx.fill(x - 3, y - 3, x + 130, y - 1, 0xFF00AA55);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§a§lJack §fv1.3"), x, y, 0xFFFFFF); y += 12;
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7FPS §f" + mc.getCurrentFps() + " §7XYZ §f" + String.format("%.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ())), x, y, 0xFFFFFF); y += 12;
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7KA " + (killAura ? "§a●" : "§c●") + " §7Crit §f" + critMode + " §7ESP " + (esp ? "§a●" : "§c●")), x, y, 0xFFFFFF); y += 12;
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7Aim " + (aimAssist ? "§a●" : "§c●") + " §7Fly " + (fly ? "§a●" : "§c●") + " §7Sprint " + (autoSprint ? "§a●" : "§c●")), x, y, 0xFFFFFF);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!esp || mc.world == null || mc.player == null) return;
            MatrixStack m = context.matrixStack();
            var cam = context.camera().getPos();
            VertexConsumerProvider.Immediate consumers = mc.getBufferBuilders().getEntityVertexConsumers();
            VertexConsumer buf = consumers.getBuffer(RenderLayer.getLines());
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof PlayerEntity) || e == mc.player || !e.isAlive()) continue;
                Box b = e.getBoundingBox().offset(-cam.x, -cam.y, -cam.z);
                VertexRendering.drawBox(m, buf, b, 1.0f, 0.15f, 0.15f, 1.0f);
            }
            consumers.draw();
        });
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if (c.player == null) return;
            boolean pr = InputUtil.isKeyPressed(c.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (pr && !rshiftHeld) { rshiftHeld = true; c.setScreen(new MenuScreen()); }
            if (!pr) rshiftHeld = false;

            if (fullbright) {
                if (origGamma == 0f) origGamma = (float)c.options.getGamma().getValue().doubleValue();
                c.options.getGamma().setValue(10.0);
            } else if (origGamma != 0f) { c.options.getGamma().setValue((double)origGamma); origGamma = 0f; }

            if (fly) {
                c.player.getAbilities().allowFlying = true;
                c.player.getAbilities().flying = true;
                c.player.getAbilities().setFlySpeed(flySpeed);
                c.player.sendAbilitiesUpdate();
            } else if (!c.player.isCreative() && !c.player.isSpectator()) {
                c.player.getAbilities().allowFlying = false;
                c.player.getAbilities().flying = false;
            }

            if (autoSprint && c.player.forwardSpeed > 0 && !c.player.isSneaking() && !c.player.isUsingItem()) c.player.setSprinting(true);

            if (aimAssist) {
                Entity best = null; double bd = 6.0;
                for (Entity e : c.world.getEntities()) {
                    if (e == c.player || !e.isAlive() || !(e instanceof LivingEntity)) continue;
                    double d = c.player.distanceTo(e);
                    if (d < bd) { bd = d; best = e; }
                }
                if (best != null) {
                    double dx = best.getX()-c.player.getX(), dy = best.getEyeY()-c.player.getEyeY(), dz = best.getZ()-c.player.getZ();
                    float ty = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
                    float tp = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx*dx+dz*dz))));
                    c.player.setYaw(c.player.getYaw() + wrap(ty - c.player.getYaw()) * aimSmooth);
                    c.player.setPitch(c.player.getPitch() + (tp - c.player.getPitch()) * aimSmooth);
                }
            }

            if (!killAura) { pendingTarget = null; critStage = 0; return; }

            if (critMode.equals("Packet") && pendingTarget != null && critStage > 0) {
                if (!pendingTarget.isAlive() || c.player.distanceTo(pendingTarget) > auraRange + 1.5) { pendingTarget = null; critStage = 0; return; }
                if (critStage == 1) {
                    if (c.player.networkHandler != null) c.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(c.player.getX(), c.player.getY() + 0.0625, c.player.getZ(), false, false));
                    critStage = 2; return;
                }
                if (critStage == 2) {
                    if (c.interactionManager != null) { c.interactionManager.attackEntity(c.player, pendingTarget); c.player.swingHand(Hand.MAIN_HAND); }
                    if (c.player.networkHandler != null) c.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(c.player.getX(), c.player.getY(), c.player.getZ(), true, false));
                    pendingTarget = null; critStage = 0; return;
                }
            }

            if (critMode.equals("Jump") && pendingTarget != null) {
                if (!pendingTarget.isAlive() || c.player.distanceTo(pendingTarget) > auraRange + 1.5) { pendingTarget = null; return; }
                if (c.player.getVelocity().y < 0.0 && c.player.fallDistance > 0.0F) {
                    if (c.interactionManager != null) { c.interactionManager.attackEntity(c.player, pendingTarget); c.player.swingHand(Hand.MAIN_HAND); }
                    pendingTarget = null;
                }
                return;
            }

            tick++;
            if (tick < (20 / auraCps)) return;
            tick = 0;
            Entity t = null; double cl = auraRange;
            for (Entity e : c.world.getEntities()) {
                if (e == c.player || !e.isAlive() || !(e instanceof LivingEntity)) continue;
                double d = c.player.distanceTo(e);
                if (d < cl) { cl = d; t = e; }
            }
            if (t == null) return;
            double dx = t.getX()-c.player.getX(), dy = t.getEyeY()-c.player.getEyeY(), dz = t.getZ()-c.player.getZ();
            c.player.setYaw((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F));
            c.player.setPitch((float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx*dx+dz*dz)))));
            if (critMode.equals("Packet")) { pendingTarget = t; critStage = 1; }
            else if (critMode.equals("Jump")) { if (c.player.isOnGround()) { c.player.jump(); pendingTarget = t; } }
            else if (c.interactionManager != null) { c.interactionManager.attackEntity(c.player, t); c.player.swingHand(Hand.MAIN_HAND); }
        });
    }
    private static float wrap(float a) {
        a = a % 360f;
        if (a >= 180f) a -= 360f;
        if (a < -180f) a += 360f;
        return a;
    }
}
