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
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class VisualModule implements ClientModInitializer {
    public static boolean killAura = false, aimAssist = false, cpsEnabled = true;
    public static String critMode = "Jump";
    public static float aimSmooth = 0.15f;
    public static double auraRange = 3.5;
    public static int auraCps = 10;
    public static boolean targetPlayersOnly = false;
    public static boolean fly = false, autoSprint = false, noFall = false;
    public static float flySpeed = 0.05f;
    public static boolean hud = true, esp = false, targetHud = true;
    public static boolean fullbright = false, optimized = true;
    public static float handScale = 1.0f, handSwingSpeed = 1.0f, handWaveIntensity = 0.5f;
    public static String lastTarget = null;

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static int attackTick = 0;
    private static boolean rshiftHeld = false;
    private static float origGamma = 0f;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((ctx, t) -> {
            if (mc.player == null || mc.options.hudHidden) return;
            if (hud) {
                int x = 6, y = 6;
                ctx.fill(x - 3, y - 3, x + 130, y + 56, 0x66000000);
                ctx.fill(x - 3, y - 3, x + 130, y - 1, 0xFF00AA55);
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§a§lJack §fv2.3"), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7FPS §f" + mc.getCurrentFps() + " §7XYZ §f" + String.format("%.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ())), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7KA " + (killAura ? "§a●" : "§c●") + " §7Crit §f" + critMode + " §7ESP " + (esp ? "§a●" : "§c●")), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7Aim " + (aimAssist ? "§a●" : "§c●") + " §7Fly " + (fly ? "§a●" : "§c●")), x, y, 0xFFFFFF);
            }
            if (targetHud && killAura) {
                Entity target = findTarget();
                if (target instanceof LivingEntity le) {
                    int tw = 140, th = 44;
                    int tx = ctx.getScaledWindowWidth() / 2 - tw / 2, ty = 30;
                    ctx.fill(tx - 1, ty - 1, tx + tw + 1, ty + th + 1, 0xFF00AA55);
                    ctx.fill(tx, ty, tx + tw, ty + th, 0xCC000000);
                    ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§c" + le.getName().getString()), tx + 4, ty + 4, 0xFFFFFF);
                    float hp = le.getHealth(), maxHp = le.getMaxHealth();
                    ctx.fill(tx + 4, ty + 18, tx + tw - 4, ty + 26, 0xFF222222);
                    ctx.fill(tx + 4, ty + 18, tx + 4 + (int)((tw - 8) * (hp / maxHp)), ty + 26, 0xFF00FF88);
                    ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7HP: §f" + (int)hp + "/" + (int)maxHp + " §7Dist: §f" + String.format("%.1f", mc.player.distanceTo(le)) + "m"), tx + 4, ty + 30, 0xFFFFFF);
                }
            }
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
            if (noFall) c.player.fallDistance = 0.0F;

            if (aimAssist) {
                Entity best = findTarget();
                if (best instanceof LivingEntity) {
                    float tYaw = getYawTo(best);
                    float tPitch = getPitchTo(best, c.player);
                    float dYaw = MathHelper.wrapDegrees(tYaw - c.player.getYaw());
                    float dPitch = tPitch - c.player.getPitch();
                    float maxStep = aimSmooth * 0.5f;
                    if (dYaw > maxStep) dYaw = maxStep; else if (dYaw < -maxStep) dYaw = -maxStep;
                    if (dPitch > maxStep) dPitch = maxStep; else if (dPitch < -maxStep) dPitch = -maxStep;
                    c.player.setYaw(c.player.getYaw() + dYaw);
                    c.player.setPitch(c.player.getPitch() + dPitch);
                }
            }

            if (!killAura) { attackTick = 0; lastTarget = null; return; }

            Entity t = findTarget();
            if (t == null) { attackTick = 0; lastTarget = null; return; }
            if (t instanceof LivingEntity le) lastTarget = le.getName().getString();

            float ty = getYawTo(t);
            float tp = getPitchTo(t, c.player);
            c.player.setYaw(c.player.getYaw() + MathHelper.wrapDegrees(ty - c.player.getYaw()) * 0.6f);
            c.player.setPitch(c.player.getPitch() + (tp - c.player.getPitch()) * 0.6f);

            attackTick++;
            int delay = cpsEnabled ? Math.max(1, 20 / auraCps) : 1;
            if (attackTick < delay) return;
            attackTick = 0;

            if (critMode.equals("Jump")) {
                if (c.player.isOnGround()) {
                    c.player.jump();
                } else if (c.player.fallDistance > 0.0F && c.player.getVelocity().y < 0.0) {
                    if (c.interactionManager != null) {
                        c.interactionManager.attackEntity(c.player, t);
                        c.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            } else if (critMode.equals("Packet")) {
                if (c.player.isOnGround() && !c.player.isSubmergedInWater() && !c.player.isInLava() && !c.player.isClimbing()) {
                    double px = c.player.getX(), py = c.player.getY(), pz = c.player.getZ();
                    if (c.player.networkHandler != null) {
                        c.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround(px, py + 0.0625, pz, false, false));
                        c.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround(px, py, pz, false, false));
                    }
                }
                if (c.interactionManager != null) {
                    c.interactionManager.attackEntity(c.player, t);
                    c.player.swingHand(Hand.MAIN_HAND);
                }
            } else {
                if (c.interactionManager != null) {
                    c.interactionManager.attackEntity(c.player, t);
                    c.player.swingHand(Hand.MAIN_HAND);
                }
            }
        });
    }

    private static Entity findTarget() {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player == null || c.world == null) return null;
        Entity best = null;
        double bestDist = auraRange;
        for (Entity e : c.world.getEntities()) {
            if (e == c.player || !e.isAlive() || !(e instanceof LivingEntity)) continue;
            if (targetPlayersOnly && !(e instanceof PlayerEntity)) continue;
            double d = c.player.distanceTo(e);
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }

    private static float getYawTo(Entity e) {
        MinecraftClient c = MinecraftClient.getInstance();
        double dx = e.getX() - c.player.getX();
        double dz = e.getZ() - c.player.getZ();
        return (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
    }

    private static float getPitchTo(Entity e, PlayerEntity p) {
        double dx = e.getX() - p.getX();
        double dy = (e.getY() + e.getEyeHeight(e.getPose())) - (p.getY() + p.getEyeHeight(p.getPose()));
        double dz = e.getZ() - p.getZ();
        double dist = Math.sqrt(dx*dx + dz*dz);
        return (float)(-Math.toDegrees(Math.atan2(dy, dist)));
    }
}
