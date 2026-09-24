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
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class VisualModule implements ClientModInitializer {
    public static boolean killAura = false, fly = false, autoSprint = false;
    public static boolean fullbright = false, aimAssist = false, hud = true, esp = false;
    public static boolean waveModel = true, targetHud = true, targetPlayersOnly = false;
    public static boolean noFall = false, cpsEnabled = true, optimized = true;
    public static String critMode = "Packet";
    public static float aimSmooth = 0.15f;
    public static float flySpeed = 0.05f;
    public static double auraRange = 3.0;
    public static int auraCps = 10;
    public static float handScale = 1.0f;
    public static float handSwingSpeed = 1.0f;
    public static float handWaveIntensity = 0.5f;
    public static String lastTarget = null;

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static int attackTick = 0;
    private static boolean rshiftHeld = false;
    private static float origGamma = 0f;
    private static long tickCounter = 0;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((ctx, t) -> {
            if (mc.player == null || mc.options.hudHidden) return;
            tickCounter++;
            if (hud) {
                int x = 6, y = 6;
                ctx.fill(x - 3, y - 3, x + 130, y + 56, 0x66000000);
                ctx.fill(x - 3, y - 3, x + 130, y - 1, 0xFF00AA55);
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§a§lJack §fv1.8"), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7FPS §f" + mc.getCurrentFps() + " §7XYZ §f" + String.format("%.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ())), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7KA " + (killAura ? "§a●" : "§c●") + " §7Crit §f" + critMode + " §7ESP " + (esp ? "§a●" : "§c●")), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7Aim " + (aimAssist ? "§a●" : "§c●") + " §7Fly " + (fly ? "§a●" : "§c●")), x, y, 0xFFFFFF);
            }
            if (targetHud && killAura) {
                Entity target = findTarget();
                if (target instanceof LivingEntity le) {
                    int tw = 120, th = 40;
                    int tx = ctx.getScaledWindowWidth() / 2 - tw / 2, ty = 30;
                    ctx.fill(tx - 1, ty - 1, tx + tw + 1, ty + th + 1, 0xFF00AA55);
                    ctx.fill(tx, ty, tx + tw, ty + th, 0xCC000000);
                    ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§c" + le.getName().getString()), tx + 4, ty + 4, 0xFFFFFF);
                    float hp = le.getHealth() / le.getMaxHealth();
                    ctx.fill(tx + 4, ty + 22, tx + tw - 4, ty + 30, 0xFF222222);
                    ctx.fill(tx + 4, ty + 22, tx + 4 + (int)((tw - 8) * hp), ty + 30, 0xFF00FF88);
                    ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7" + (int)le.getHealth() + "❤"), tx + 4, ty + 32, 0xFFFFFF);
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

            if (aimAssist) {
                Entity best = findTarget();
                if (best instanceof LivingEntity) {
                    double dx = best.getX() - c.player.getX();
                    double dy = (best.getY() + best.getEyeHeight(best.getPose()) / 2) - (c.player.getY() + c.player.getEyeHeight(c.player.getPose()));
                    double dz = best.getZ() - c.player.getZ();
                    float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
                    float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx*dx + dz*dz))));
                    // Плавное наведение (по коду Meteor: speed * delta)
                    float deltaYaw = MathHelper.wrapDegrees(targetYaw - c.player.getYaw());
                    float deltaPitch = targetPitch - c.player.getPitch();
                    float rotYaw = (float)(aimSmooth * (deltaYaw >= 0 ? 1 : -1) * 0.5f);
                    if ((rotYaw >= 0 && rotYaw > deltaYaw) || (rotYaw < 0 && rotYaw < deltaYaw)) rotYaw = deltaYaw;
                    c.player.setYaw(c.player.getYaw() + rotYaw);
                    float rotPitch = (float)(aimSmooth * (deltaPitch >= 0 ? 1 : -1) * 0.5f);
                    if ((rotPitch >= 0 && rotPitch > deltaPitch) || (rotPitch < 0 && rotPitch < deltaPitch)) rotPitch = deltaPitch;
                    c.player.setPitch(c.player.getPitch() + rotPitch);
                }
            }

            if (!killAura) { attackTick = 0; lastTarget = null; return; }

            Entity t = findTarget();
            if (t == null) { attackTick = 0; lastTarget = null; return; }
            if (t instanceof LivingEntity le) lastTarget = le.getName().getString();

            if (waveModel) {
                double wave = Math.sin(tickCounter * 0.2) * 0.05;
                t.setPosition(t.getX(), t.getY() + wave, t.getZ());
            }

            double dx = t.getX() - c.player.getX(), dy = t.getEyeY() - c.player.getEyeY(), dz = t.getZ() - c.player.getZ();
            float ty = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
            float tp = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx*dx + dz*dz))));
            c.player.setYaw(c.player.getYaw() + MathHelper.wrapDegrees(ty - c.player.getYaw()) * 0.6f);
            c.player.setPitch(c.player.getPitch() + (tp - c.player.getPitch()) * 0.6f);

            attackTick++;
            int delay = cpsEnabled ? Math.max(1, 20 / auraCps) : 1;
            if (attackTick < delay) return;
            attackTick = 0;

            if (critMode.equals("Packet")) {
                if (c.player.isOnGround() && !c.player.isSubmergedInWater() && !c.player.isInLava() && !c.player.isClimbing()) {
                    double px = c.player.getX(), py = c.player.getY(), pz = c.player.getZ();
                    if (c.player.networkHandler != null) {
                        c.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(px, py + 0.0625, pz, false, false));
                        c.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(px, py, pz, false, false));
                    }
                }
                if (c.interactionManager != null) {
                    c.interactionManager.attackEntity(c.player, t);
                    c.player.swingHand(Hand.MAIN_HAND);
                }
            } else if (critMode.equals("Jump")) {
                if (c.player.isOnGround()) c.player.jump();
                else if (c.player.getVelocity().y < 0.0 && c.interactionManager != null) {
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
        double bestHealth = Double.MAX_VALUE;
        for (Entity e : c.world.getEntities()) {
            if (e == c.player || !e.isAlive() || !(e instanceof LivingEntity)) continue;
            if (targetPlayersOnly && !(e instanceof PlayerEntity)) continue;
            double d = c.player.distanceTo(e);
            if (d > auraRange) continue;
            double hp = ((LivingEntity) e).getHealth();
            if (hp < bestHealth) { bestHealth = hp; best = e; }
        }
        return best;
    }

    private static float wrap(float a) {
        a = a % 360f;
        if (a >= 180f) a -= 360f;
        if (a < -180f) a += 360f;
        return a;
    }
}
