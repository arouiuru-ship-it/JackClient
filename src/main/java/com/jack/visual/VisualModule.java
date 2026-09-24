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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VisualModule implements ClientModInitializer {
    public static boolean killAura = false, fly = false, autoSprint = false;
    public static boolean fullbright = false, aimAssist = false, hud = true, esp = false;
    public static boolean waveModel = true, targetHud = true, targetPlayersOnly = false;
    public static boolean tracers = false, customSky = false, selfNametag = false;
    public static boolean noFall = false, cpsEnabled = true, optimized = true;
    public static String critMode = "Jump";   // Jump по умолчанию — самый надёжный
    public static float aimSmooth = 0.15f;
    public static float flySpeed = 0.05f;
    public static double auraRange = 3.5;
    public static int auraCps = 10;
    public static String lastTarget = null;
    public static float handScale = 1.0f, handSwingSpeed = 1.0f, handWaveIntensity = 0.5f;
    public static float tracerWidth = 0.01f;
    public static int tracerSegments = 30;
    public static boolean targetHudShowArmor = true;
    public static boolean targetHudShowDistance = true;
    public static boolean jumpCircle = false, customCrosshair = false, totemCounter = false;
    public static float crosshairSize = 4.0f;
    public static int crosshairColor = 0x00FF88;
    public static int crosshairGap = 3;
    public static float jumpCircleRadiusMax = 1.5f;

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static int attackTick = 0;
    private static boolean rshiftHeld = false;
    private static float origGamma = 0f;
    private static long tickCounter = 0;
    private static boolean jumpCircleActive = false;
    private static double jumpCircleX, jumpCircleY, jumpCircleZ;
    private static float jumpCircleRadius = 0f;
    private static int jumpCircleLife = 0;
    private static double jumpCircleX, jumpCircleY, jumpCircleZ;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((ctx, t) -> {
            if (mc.player == null || mc.options.hudHidden) return;
            tickCounter++;
            if (hud) {
                int x = 6, y = 6;
                ctx.fill(x - 3, y - 3, x + 130, y + 56, 0x66000000);
                ctx.fill(x - 3, y - 3, x + 130, y - 1, 0xFF00AA55);
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§a§lJack §fv2.0"), x, y, 0xFFFFFF); y += 12;
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
            if (mc.world == null || mc.player == null) return;
            MatrixStack m = context.matrixStack();
            var cam = context.camera().getPos();
            VertexConsumerProvider.Immediate consumers = mc.getBufferBuilders().getEntityVertexConsumers();

            if (esp) {
                VertexConsumer buf = consumers.getBuffer(RenderLayer.getLines());
                for (Entity e : mc.world.getEntities()) {
                    if (!(e instanceof PlayerEntity) || e == mc.player || !e.isAlive()) continue;
                    Box b = e.getBoundingBox().offset(-cam.x, -cam.y, -cam.z);
                    VertexRendering.drawBox(m, buf, b, 1.0f, 0.15f, 0.15f, 1.0f);
                    double dx = e.getX() - cam.x;
                    double dy = (e.getY() + e.getHeight() + 0.5) - cam.y;
                    double dz = e.getZ() - cam.z;
                    m.push();
                    m.translate(dx, dy, dz);
                    m.multiply(mc.getEntityRenderDispatcher().camera.getRotation());
                    m.scale(-0.025f, -0.025f, 0.025f);
                    String name = e.getName().getString() + " \u00a77[" + String.format("%.1f", mc.player.distanceTo(e)) + "m]";
                    mc.textRenderer.draw(Text.literal(name), -mc.textRenderer.getWidth(name) / 2f, 0, 0xFFFFFF, true, m.peek().getPositionMatrix(), consumers, net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
                    m.pop();
                }
            }

            if (tracers) {
                VertexConsumer buf = consumers.getBuffer(RenderLayer.getLines());
                double px = mc.player.getX() - cam.x;
                double py = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) - cam.y;
                double pz = mc.player.getZ() - cam.z;
                for (Entity e : mc.world.getEntities()) {
                    if (!(e instanceof PlayerEntity) || e == mc.player || !e.isAlive()) continue;
                    double ex = e.getX() - cam.x;
                    double ey = e.getY() + e.getHeight() / 2 - cam.y;
                    double ez = e.getZ() - cam.z;
                    // Рисуем линию из маленьких боксов (drawLine нет в 1.21.4)
                    for (int i = 0; i < 20; i++) {
                        float t = i / 20.0f;
                        float lx = (float)(px + (ex - px) * t);
                        float ly = (float)(py + (ey - py) * t);
                        float lz = (float)(pz + (ez - pz) * t);
                        Box dot = new Box(lx - 0.01, ly - 0.01, lz - 0.01, lx + 0.01, ly + 0.01, lz + 0.01);
                        VertexRendering.drawBox(m, buf, dot, 1.0f, 0.0f, 0.0f, 1.0f);
                    }
                }
            }

            if (selfNametag && mc.options.getPerspective().isFrontView()) {
                double dx = mc.player.getX() - cam.x;
                double dy = (mc.player.getY() + mc.player.getHeight() + 0.5) - cam.y;
                double dz = mc.player.getZ() - cam.z;
                m.push();
                m.translate(dx, dy, dz);
                m.multiply(mc.getEntityRenderDispatcher().camera.getRotation());
                m.scale(-0.025f, -0.025f, 0.025f);
                String name = mc.player.getName().getString();
                mc.textRenderer.draw(Text.literal(name), -mc.textRenderer.getWidth(name) / 2f, 0, 0x00FF88, true, m.peek().getPositionMatrix(), consumers, net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
                m.pop();
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

            // ПЛАВНЫЙ AIM (метод Meteor Rotations)
            if (aimAssist) {
                Entity best = findTarget();
                if (best instanceof LivingEntity) {
                    float targetYaw = getYawTo(best);
                    float targetPitch = getPitchTo(best, c.player);
                    float deltaYaw = MathHelper.wrapDegrees(targetYaw - c.player.getYaw());
                    float deltaPitch = targetPitch - c.player.getPitch();

                    // aimSmooth от 0.05 до 0.9 — итоговый множитель
                    float mult = aimSmooth * 0.5f;

                    // Ограничиваем шаг за тик
                    float maxYawStep = 15.0f * mult;
                    float maxPitchStep = 10.0f * mult;

                    if (deltaYaw > maxYawStep) deltaYaw = maxYawStep;
                    else if (deltaYaw < -maxYawStep) deltaYaw = -maxYawStep;

                    if (deltaPitch > maxPitchStep) deltaPitch = maxPitchStep;
                    else if (deltaPitch < -maxPitchStep) deltaPitch = -maxPitchStep;

                    c.player.setYaw(c.player.getYaw() + deltaYaw);
                    c.player.setPitch(c.player.getPitch() + deltaPitch);
                }
            }

            if (!killAura) { lastTarget = null; return; }

            // ==== SMOOTH KILLAURA ====
            Entity t = findTarget();
            if (t == null) { lastTarget = null; return; }
            if (t instanceof LivingEntity le) lastTarget = le.getName().getString();

            // ПЛАВНАЯ ротация: ограниченный шаг за тик (как в Meteor Rotations)
            float targetYaw = getYawTo(t);
            float targetPitch = getPitchTo(t, c.player);
            float deltaYaw = MathHelper.wrapDegrees(targetYaw - c.player.getYaw());
            float deltaPitch = targetPitch - c.player.getPitch();

            // Максимальный шаг за один тик — 25 градусов yaw, 15 pitch
            float maxYawStep = 25.0f;
            float maxPitchStep = 15.0f;

            if (deltaYaw > maxYawStep) deltaYaw = maxYawStep;
            else if (deltaYaw < -maxYawStep) deltaYaw = -maxYawStep;

            if (deltaPitch > maxPitchStep) deltaPitch = maxPitchStep;
            else if (deltaPitch < -maxPitchStep) deltaPitch = -maxPitchStep;

            // Дополнительное сглаживание (мягкий множитель)
            float smooth = 0.6f;
            c.player.setYaw(c.player.getYaw() + deltaYaw * smooth);
            c.player.setPitch(c.player.getPitch() + deltaPitch * smooth);

            // Атака через натуральный кулдаун
            if (c.player.getAttackCooldownProgress(0.0f) >= 1.0f) {
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
        double bestDist = 6.0;
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
