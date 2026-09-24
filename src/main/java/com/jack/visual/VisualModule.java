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
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class VisualModule implements ClientModInitializer {
    // Combat
    public static boolean killAura = false, aimAssist = false, cpsEnabled = true;
    public static String critMode = "Jump";
    public static float aimSmooth = 0.15f;
    public static double auraRange = 3.5;
    public static int auraCps = 10;
    public static boolean targetPlayersOnly = false;
    public static String lastTarget = null;

    // Movement
    public static boolean fly = false, autoSprint = false, noFall = false;
    public static float flySpeed = 0.05f;

    // Visual
    public static boolean hud = true, esp = false, tracers = false, targetHud = true;
    public static boolean fullbright = false, optimized = true;
    public static boolean waveModel = true, selfNametag = false, customSky = false;
    public static boolean jumpCircle = false, customCrosshair = false, totemCounter = false;
    public static float tracerWidth = 0.02f;
    public static int tracerSegments = 30;
    public static float crosshairSize = 4.0f;
    public static int crosshairColor = 0x00FF88;
    public static int crosshairGap = 3;
    public static float jumpCircleRadiusMax = 1.5f;
    public static float handScale = 1.0f, handSwingSpeed = 1.0f, handWaveIntensity = 0.5f;

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static int attackTick = 0;
    private static boolean rshiftHeld = false;
    private static float origGamma = 0f;
    private static long tickCounter = 0;
    private static boolean jumpCircleActive = false;
    private static double jumpCircleX = 0, jumpCircleY = 0, jumpCircleZ = 0;
    private static float jumpCircleRadius = 0f;
    private static int jumpCircleLife = 0;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((ctx, t) -> {
            if (mc.player == null || mc.options.hudHidden) return;
            tickCounter++;

            if (hud) {
                int x = 6, y = 6;
                ctx.fill(x - 3, y - 3, x + 130, y + 56, 0x66000000);
                ctx.fill(x - 3, y - 3, x + 130, y - 1, 0xFF00AA55);
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§a§lJack §fv2.1"), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7FPS §f" + mc.getCurrentFps() + " §7XYZ §f" + String.format("%.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ())), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7KA " + (killAura ? "§a●" : "§c●") + " §7ESP " + (esp ? "§a●" : "§c●")), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7Aim " + (aimAssist ? "§a●" : "§c●") + " §7Fly " + (fly ? "§a●" : "§c●")), x, y, 0xFFFFFF);
            }

            if (targetHud && killAura) {
                Entity target = findTarget();
                if (target instanceof LivingEntity le) {
                    int tw = 140, th = 50;
                    int tx = ctx.getScaledWindowWidth() / 2 - tw / 2, ty = 30;
                    ctx.fill(tx - 1, ty - 1, tx + tw + 1, ty + th + 1, 0xFF00AA55);
                    ctx.fill(tx, ty, tx + tw, ty + th, 0xCC000000);
                    ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§c" + le.getName().getString()), tx + 4, ty + 4, 0xFFFFFF);
                    float hp = le.getHealth(), maxHp = le.getMaxHealth();
                    ctx.fill(tx + 4, ty + 18, tx + tw - 4, ty + 26, 0xFF222222);
                    ctx.fill(tx + 4, ty + 18, tx + 4 + (int)((tw - 8) * (hp / maxHp)), ty + 26, 0xFF00FF88);
                    ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7HP: §f" + (int)hp + "/" + (int)maxHp), tx + 4, ty + 28, 0xFFFFFF);
                    if (mc.player != null) {
                        ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7Dist: §f" + String.format("%.1f", mc.player.distanceTo(le)) + "m"), tx + 80, ty + 38, 0xFFFFFF);
                    }
                }
            }

            if (customCrosshair) {
                int cx = ctx.getScaledWindowWidth() / 2;
                int cy = ctx.getScaledWindowHeight() / 2;
                int g = crosshairGap;
                int s = (int)crosshairSize;
                int col = 0xFF000000 | crosshairColor;
                ctx.fill(cx - g - s, cy, cx - g, cy + 1, col);
                ctx.fill(cx + g, cy, cx + g + s, cy + 1, col);
                ctx.fill(cx, cy - g - s, cx + 1, cy - g, col);
                ctx.fill(cx, cy + g, cx + 1, cy + g + s, col);
                ctx.fill(cx, cy, cx + 1, cy + 1, col);
            }

            if (totemCounter && mc.player != null) {
                int total = 0;
                for (int i = 0; i < mc.player.getInventory().size(); i++) {
                    var st = mc.player.getInventory().getStack(i);
                    if (st.isOf(Items.TOTEM_OF_UNDYING)) total += st.getCount();
                }
                int ox = ctx.getScaledWindowWidth() - 60;
                int oy = 6;
                ctx.fill(ox - 3, oy - 3, ox + 55, oy + 22, 0x66000000);
                ctx.fill(ox - 3, oy - 3, ox + 55, oy - 1, 0xFF00AA55);
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7Totems:"), ox, oy + 1, 0xFFFFFF);
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§a" + total), ox + 42, oy + 1, 0xFFFFFF);
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
                    String name = e.getName().getString() + " §7[" + String.format("%.1f", mc.player.distanceTo(e)) + "m]";
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
                    for (int i = 0; i < tracerSegments; i++) {
                        float t = i / (float)tracerSegments;
                        float lx = (float)(px + (ex - px) * t);
                        float ly = (float)(py + (ey - py) * t);
                        float lz = (float)(pz + (ez - pz) * t);
                        Box dot = new Box(lx - tracerWidth, ly - tracerWidth, lz - tracerWidth, lx + tracerWidth, ly + tracerWidth, lz + tracerWidth);
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

            if (jumpCircle && jumpCircleActive && jumpCircleLife > 0) {
                VertexConsumer buf = consumers.getBuffer(RenderLayer.getLines());
                int segments = 24;
                float r = jumpCircleRadius;
                float alpha = jumpCircleLife / 20.0f;
                for (int i = 0; i < segments; i++) {
                    double a1 = (i / (double)segments) * Math.PI * 2;
                    double a2 = ((i + 1) / (double)segments) * Math.PI * 2;
                    float midX = (float)(jumpCircleX - cam.x + (Math.cos(a1) + Math.cos(a2)) / 2 * r);
                    float midZ = (float)(jumpCircleZ - cam.z + (Math.sin(a1) + Math.sin(a2)) / 2 * r);
                    float y = (float)(jumpCircleY - cam.y) + 0.05f;
                    Box dot = new Box(midX - 0.05, y - 0.02, midZ - 0.05, midX + 0.05, y + 0.02, midZ + 0.05);
                    VertexRendering.drawBox(m, buf, dot, 0.0f, 1.0f, 0.5f, alpha);
                }
            }

            consumers.draw();
        });

        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if (c.player == null) return;
            boolean pr = InputUtil.isKeyPressed(c.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (pr && !rshiftHeld) { rshiftHeld = true; c.setScreen(new MenuScreen()); }
            if (!pr) rshiftHeld = false;

            if (jumpCircle) {
                if (c.player.isOnGround() && jumpCircleLife == 0) {
                    jumpCircleX = c.player.getX();
                    jumpCircleY = c.player.getY();
                    jumpCircleZ = c.player.getZ();
                }
                if (!c.player.isOnGround() && c.player.getVelocity().y > 0.1 && jumpCircleLife == 0) {
                    jumpCircleActive = true;
                    jumpCircleRadius = 0.3f;
                    jumpCircleLife = 20;
                }
                if (jumpCircleLife > 0) {
                    jumpCircleLife--;
                    jumpCircleRadius += (jumpCircleRadiusMax - 0.3f) / 20f;
                    if (jumpCircleLife == 0) jumpCircleActive = false;
                }
            }

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
