package com.jack.visual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
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
    public static boolean killAura = false, aimAssist = false;
    public static String critMode = "Off";
    public static float aimSmooth = 0.3f;
    public static double auraRange = 4.5;
    public static boolean targetPlayersOnly = false;
    public static boolean fly = false, autoSprint = false, noFall = false;
    public static float flySpeed = 0.05f;
    public static boolean hud = true, esp = false, targetHud = true;
    public static boolean fullbright = false, optimized = true;
    public static boolean jumpCircle = true;
    public static float jumpCircleRadiusMax = 1.5f;
    public static String lastTarget = null;
    public static boolean autoMine = false;
    public static int autoMineRadius = 8;
    public static String autoMineBlock = "Diamond";
    public static String[] AUTO_MINE_BLOCKS = {
        "Any",
        "Diamond", "Deepslate Diamond",
        "Iron", "Deepslate Iron",
        "Gold", "Deepslate Gold", "Nether Gold",
        "Coal", "Deepslate Coal",
        "Emerald", "Deepslate Emerald",
        "Redstone", "Deepslate Redstone",
        "Lapis", "Deepslate Lapis",
        "Copper", "Deepslate Copper",
        "Quartz", "Ancient Debris",
        "Logs", "Planks", "Leaves",
        "Stone", "Cobblestone", "Deepslate",
        "Dirt", "Grass", "Sand", "Gravel", "Clay",
        "Obsidian", "Ice", "Snow",
        "Glass", "Bookshelf", "Crafting Table",
        "Furnace", "Chest", "Torch"
    };
    public static boolean shaderHand = false;
    public static int handColor = 0x00FF88;
    public static float handAlpha = 0.6f;
    public static boolean swingAnimation = false;
    public static String swingStyle = "Smooth";
    public static float swingSpeed = 1.0f;
    public static float handScale = 1.0f;
    public static float handOffsetX = 0f, handOffsetY = 0f, handOffsetZ = 0f;
    public static float handRotationX = 0f, handRotationY = 0f, handRotationZ = 0f;

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static boolean rshiftHeld = false;
    private static net.minecraft.util.math.BlockPos autoMineTarget = null;
    private static float origGamma = 0f;
    private static boolean jumpCircleActive = false;
    private static double jumpCircleX = 0, jumpCircleY = 0, jumpCircleZ = 0;
    private static float jumpCircleRadius = 0f;
    private static int jumpCircleLife = 0;
    private static boolean jumpFlag = false;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((ctx, t) -> {
            if (mc.player == null || mc.options.hudHidden) return;
            if (hud) {
                int x = 6, y = 6;
                ctx.fill(x - 3, y - 3, x + 130, y + 56, 0x66000000);
                ctx.fill(x - 3, y - 3, x + 130, y - 1, 0xFF00AA55);
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§a§lJack §fv3.0"), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7FPS §f" + mc.getCurrentFps() + " §7XYZ §f" + String.format("%.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ())), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7KA " + (killAura ? "§a●" : "§c●") + " §7Tgt: §f" + (lastTarget != null ? lastTarget : "none")), x, y, 0xFFFFFF); y += 12;
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7Aim " + (aimAssist ? "§a●" : "§c●") + " §7Fly " + (fly ? "§a●" : "§c●")), x, y, 0xFFFFFF);
            }
            if (targetHud && killAura && lastTarget != null) {
                Entity target = pickTarget();
                if (target instanceof LivingEntity le) {
                    int tw = 140, th = 44;
                    int tx = ctx.getScaledWindowWidth() / 2 - tw / 2, ty = 30;
                    ctx.fill(tx - 1, ty - 1, tx + tw + 1, ty + th + 1, 0xFF00AA55);
                    ctx.fill(tx, ty, tx + tw, ty + th, 0xCC000000);
                    ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§c" + le.getName().getString()), tx + 4, ty + 4, 0xFFFFFF);
                    float hp = le.getHealth(), maxHp = le.getMaxHealth();
                    ctx.fill(tx + 4, ty + 18, tx + tw - 4, ty + 26, 0xFF222222);
                    ctx.fill(tx + 4, ty + 18, tx + 4 + (int)((tw - 8) * (hp / maxHp)), ty + 26, 0xFF00FF88);
                    ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7HP: §f" + (int)hp + "/" + (int)maxHp), tx + 4, ty + 30, 0xFFFFFF);
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
                }
            }
            if (jumpCircle && jumpCircleActive && jumpCircleLife > 0) {
                VertexConsumer buf = consumers.getBuffer(RenderLayer.getLines());
                int segments = 20;
                float r = jumpCircleRadius;
                float alpha = Math.max(0f, jumpCircleLife / 20.0f);
                for (int i = 0; i < segments; i++) {
                    double a1 = (i / (double)segments) * Math.PI * 2;
                    double a2 = ((i + 1) / (double)segments) * Math.PI * 2;
                    float midX = (float)(jumpCircleX - cam.x + (Math.cos(a1) + Math.cos(a2)) / 2 * r);
                    float midZ = (float)(jumpCircleZ - cam.z + (Math.sin(a1) + Math.sin(a2)) / 2 * r);
                    float y = (float)(jumpCircleY - cam.y) + 0.05f;
                    Box dot = new Box(midX - 0.06, y - 0.03, midZ - 0.06, midX + 0.06, y + 0.03, midZ + 0.06);
                    VertexRendering.drawBox(m, buf, dot, 0.0f, 1.0f, 0.5f, alpha);
                }
            }
            consumers.draw();
        });

        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if (c.player == null) return;

            // ===== AUTO MINE (Smart AI) =====
            if (autoMine && c.world != null && c.interactionManager != null) {
                if (autoMineTarget == null) autoMineTarget = findAutoMineBlock(c);

                // Проверка цели - сломана?
                if (autoMineTarget != null) {
                    net.minecraft.block.BlockState st = c.world.getBlockState(autoMineTarget);
                    if (st.isAir() || !blockMatches(st)) autoMineTarget = null;
                }

                if (autoMineTarget != null) {
                    net.minecraft.util.math.Vec3d targetCenter = net.minecraft.util.math.Vec3d.ofCenter(autoMineTarget);
                    net.minecraft.util.math.Vec3d playerPos = c.player.getPos();
                    double dist = playerPos.distanceTo(targetCenter);

                    // Смотрим на цель
                    double dx = targetCenter.x - c.player.getX();
                    double dy = targetCenter.y - (c.player.getY() + c.player.getEyeHeight(c.player.getPose()));
                    double dz = targetCenter.z - c.player.getZ();
                    float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
                    float pitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx*dx + dz*dz))));
                    c.player.setYaw(yaw);
                    c.player.setPitch(pitch);

                    if (dist > 3.5) {
                        // === AI ИДЁТ К БЛОКУ ===
                        // Считаем угол к цели относительно текущего yaw
                        float targetYaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90f;
                        float deltaYaw = MathHelper.wrapDegrees(targetYaw - c.player.getYaw());

                        // Если цель прямо - идём вперёд
                        if (Math.abs(deltaYaw) < 45f) {
                            c.player.input.movementForward = 1.0f;
                            c.player.input.movementSideways = 0.0f;
                        } else if (deltaYaw > 0) {
                            // Цель слева - стрейф влево
                            c.player.input.movementForward = 0.5f;
                            c.player.input.movementSideways = 1.0f;
                        } else {
                            // Цель справа - стрейф вправо
                            c.player.input.movementForward = 0.5f;
                            c.player.input.movementSideways = -1.0f;
                        }
                        c.player.setSprinting(true);

                        // === ПРОВЕРКА ПУТИ ВПЕРЁД ===
                        net.minecraft.util.math.Vec3d lookVec = c.player.getRotationVec(1.0f);
                        net.minecraft.util.math.BlockPos front = c.player.getBlockPos().offset(
                            net.minecraft.util.math.Direction.fromHorizontal(lookVec.getHorizontal()));

                        net.minecraft.block.BlockState frontState = c.world.getBlockState(front);
                        net.minecraft.block.BlockState frontUp = c.world.getBlockState(front.up());
                        net.minecraft.block.BlockState frontUp2 = c.world.getBlockState(front.up(2));

                        // 1. Если перед нами блок высотой 1 → прыжок
                        if (!frontState.isAir() && frontUp.isAir() && c.player.isOnGround()) {
                            c.player.jump();
                        }
                        // 2. Если перед нами блок высотой 2+ → копаем его
                        else if (!frontState.isAir() && !frontUp.isAir() && c.player.isOnGround()) {
                            // Ломаем блок перед нами
                            c.interactionManager.updateBlockBreakingProgress(front, net.minecraft.util.math.Direction.UP);
                            c.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                        }
                        // 3. Проверка блока НАД ногами (на голове)
                        net.minecraft.util.math.BlockPos headPos = c.player.getBlockPos().up(2);
                        net.minecraft.block.BlockState headState = c.world.getBlockState(headPos);
                        if (!headState.isAir() && !(headState.getBlock() instanceof net.minecraft.block.FluidBlock)) {
                            c.interactionManager.updateBlockBreakingProgress(headPos, net.minecraft.util.math.Direction.UP);
                            c.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                        }
                        // 4. Проверка блока ПОД ногами (если мы копаемся вниз)
                        net.minecraft.util.math.BlockPos belowTarget = autoMineTarget.down();
                        net.minecraft.block.BlockState belowState = c.world.getBlockState(belowTarget);
                        // (опционально для будущего вертикального копания)
                    } else {
                        // === ДОШЛИ - ЛОМАЕМ ===
                        c.player.input.movementForward = 0.0f;
                        c.player.input.movementSideways = 0.0f;
                        c.player.setSprinting(false);

                        // Если под целью пусто (мы на краю) - не падаем
                        c.interactionManager.updateBlockBreakingProgress(autoMineTarget, net.minecraft.util.math.Direction.UP);
                        c.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                    }
                } else {
                    c.player.input.movementForward = 0.0f;
                    c.player.input.movementSideways = 0.0f;
                    c.player.setSprinting(false);
                }
            }

            

            boolean pr = InputUtil.isKeyPressed(c.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (pr && !rshiftHeld) { rshiftHeld = true; c.setScreen(new MenuScreen()); }
            if (!pr) rshiftHeld = false;

            if (jumpCircle) {
                if (!c.player.isOnGround() && !jumpFlag && c.player.getVelocity().y > 0.1) {
                    jumpFlag = true;
                    jumpCircleActive = true;
                    jumpCircleX = c.player.getX();
                    jumpCircleY = c.player.getY();
                    jumpCircleZ = c.player.getZ();
                    jumpCircleRadius = 0.3f;
                    jumpCircleLife = 20;
                }
                if (c.player.isOnGround()) jumpFlag = false;
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
                Entity best = pickTarget();
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

            // ============ KILLAURA (Meteor-style) ============
            if (!killAura) { lastTarget = null; return; }

            Entity t = pickTarget();
            if (t == null) { lastTarget = null; return; }
            if (t instanceof LivingEntity le) lastTarget = le.getName().getString();

            // Ротация (как в Meteor Rotations.rotate)
            rotateTo(c.player, t, aimSmooth);

            // Проверка дистанции (как в Meteor — по hitbox)
            double distSq = c.player.squaredDistanceTo(t);
            double reach = auraRange + 0.5;
            if (distSq > reach * reach) return;

            // Задержка атаки (как в Meteor delayCheck)
            if (c.player.getAttackCooldownProgress(0.0f) < 0.9f) return;

            // Атака
            if (c.interactionManager != null) {
                c.interactionManager.attackEntity(c.player, t);
                c.player.swingHand(Hand.MAIN_HAND);
            }
        });
    }

    // ===== TARGET PICKER (Meteor-style: сортировка по углу) =====
    private static Entity pickTarget() {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player == null || c.world == null) return null;
        ClientPlayerEntity p = c.player;
        List<Entity> targets = new ArrayList<>();
        for (Entity e : c.world.getEntities()) {
            if (e == p) continue;
            if (!e.isAlive()) continue;
            if (!(e instanceof LivingEntity)) continue;
            if (targetPlayersOnly && !(e instanceof PlayerEntity)) continue;
            double d = p.distanceTo(e);
            if (d > auraRange + 1.0) continue;
            targets.add(e);
        }
        if (targets.isEmpty()) return null;
        targets.sort(Comparator.comparingDouble(e -> {
            float yawTo = getYawTo(e);
            float diff = Math.abs(MathHelper.wrapDegrees(yawTo - p.getYaw()));
            return diff;
        }));
        return targets.get(0);
    }

    // ===== ROTATION (Meteor-style, плавная) =====
    private static void rotateTo(ClientPlayerEntity p, Entity t, float speed) {
        float targetYaw = getYawTo(t);
        float targetPitch = getPitchTo(t, p);
        float deltaYaw = MathHelper.wrapDegrees(targetYaw - p.getYaw());
        float deltaPitch = targetPitch - p.getPitch();

        float maxYawStep = 30.0f;
        float maxPitchStep = 20.0f;
        if (deltaYaw > maxYawStep) deltaYaw = maxYawStep;
        else if (deltaYaw < -maxYawStep) deltaYaw = -maxYawStep;
        if (deltaPitch > maxPitchStep) deltaPitch = maxPitchStep;
        else if (deltaPitch < -maxPitchStep) deltaPitch = -maxPitchStep;

        float mult = Math.max(0.3f, speed);
        p.setYaw(p.getYaw() + deltaYaw * mult);
        p.setPitch(p.getPitch() + deltaPitch * mult);
    }

    private static float getYawTo(Entity e) {
        MinecraftClient c = MinecraftClient.getInstance();
        double dx = e.getX() - c.player.getX();
        double dz = e.getZ() - c.player.getZ();
        return (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
    }

    private static float getPitchTo(Entity e, PlayerEntity p) {
        double dx = e.getX() - p.getX();
        double dy = (e.getY() + e.getHeight() / 2) - (p.getY() + p.getStandingEyeHeight());
        double dz = e.getZ() - p.getZ();
        double dist = Math.sqrt(dx*dx + dz*dz);
        return (float)(-Math.toDegrees(Math.atan2(dy, dist)));
    }



    private static net.minecraft.util.math.BlockPos findAutoMineBlock(MinecraftClient c) {
        if (c.player == null || c.world == null) return null;
        net.minecraft.util.math.BlockPos playerPos = c.player.getBlockPos();
        net.minecraft.util.math.BlockPos best = null;
        double bestDist = autoMineRadius * 2.0;
        int r = autoMineRadius;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    net.minecraft.util.math.BlockPos pos = playerPos.add(x, y, z);
                    net.minecraft.block.BlockState state = c.world.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (!blockMatches(state)) continue;
                    float h = state.getHardness(c.world, pos);
                    if (h < 0 || h > 10.0f) continue;
                    double d = c.player.getPos().distanceTo(net.minecraft.util.math.Vec3d.ofCenter(pos));
                    if (d < bestDist) { bestDist = d; best = pos; }
                }
            }
        }
        return best;
    }


    private static boolean blockMatches(net.minecraft.block.BlockState state) {
        net.minecraft.block.Block b = state.getBlock();
        String m = autoMineBlock;
        if (m.equals("Any")) return !state.isAir();
        if (m.equals("Diamond")) return b == net.minecraft.block.Blocks.DIAMOND_ORE;
        if (m.equals("Deepslate Diamond")) return b == net.minecraft.block.Blocks.DEEPSLATE_DIAMOND_ORE;
        if (m.equals("Iron")) return b == net.minecraft.block.Blocks.IRON_ORE;
        if (m.equals("Deepslate Iron")) return b == net.minecraft.block.Blocks.DEEPSLATE_IRON_ORE;
        if (m.equals("Gold")) return b == net.minecraft.block.Blocks.GOLD_ORE;
        if (m.equals("Deepslate Gold")) return b == net.minecraft.block.Blocks.DEEPSLATE_GOLD_ORE;
        if (m.equals("Nether Gold")) return b == net.minecraft.block.Blocks.NETHER_GOLD_ORE;
        if (m.equals("Coal")) return b == net.minecraft.block.Blocks.COAL_ORE;
        if (m.equals("Deepslate Coal")) return b == net.minecraft.block.Blocks.DEEPSLATE_COAL_ORE;
        if (m.equals("Emerald")) return b == net.minecraft.block.Blocks.EMERALD_ORE;
        if (m.equals("Deepslate Emerald")) return b == net.minecraft.block.Blocks.DEEPSLATE_EMERALD_ORE;
        if (m.equals("Redstone")) return b == net.minecraft.block.Blocks.REDSTONE_ORE;
        if (m.equals("Deepslate Redstone")) return b == net.minecraft.block.Blocks.DEEPSLATE_REDSTONE_ORE;
        if (m.equals("Lapis")) return b == net.minecraft.block.Blocks.LAPIS_ORE;
        if (m.equals("Deepslate Lapis")) return b == net.minecraft.block.Blocks.DEEPSLATE_LAPIS_ORE;
        if (m.equals("Copper")) return b == net.minecraft.block.Blocks.COPPER_ORE;
        if (m.equals("Deepslate Copper")) return b == net.minecraft.block.Blocks.DEEPSLATE_COPPER_ORE;
        if (m.equals("Quartz")) return b == net.minecraft.block.Blocks.NETHER_QUARTZ_ORE;
        if (m.equals("Ancient Debris")) return b == net.minecraft.block.Blocks.ANCIENT_DEBRIS;
        if (m.equals("Logs")) return state.isIn(net.minecraft.registry.tag.BlockTags.LOGS);
        if (m.equals("Planks")) return state.isIn(net.minecraft.registry.tag.BlockTags.PLANKS);
        if (m.equals("Leaves")) return state.isIn(net.minecraft.registry.tag.BlockTags.LEAVES);
        if (m.equals("Stone")) return b == net.minecraft.block.Blocks.STONE;
        if (m.equals("Cobblestone")) return b == net.minecraft.block.Blocks.COBBLESTONE;
        if (m.equals("Deepslate")) return b == net.minecraft.block.Blocks.DEEPSLATE;
        if (m.equals("Dirt")) return b == net.minecraft.block.Blocks.DIRT;
        if (m.equals("Grass")) return b == net.minecraft.block.Blocks.GRASS_BLOCK;
        if (m.equals("Sand")) return b == net.minecraft.block.Blocks.SAND || b == net.minecraft.block.Blocks.RED_SAND;
        if (m.equals("Gravel")) return b == net.minecraft.block.Blocks.GRAVEL;
        if (m.equals("Clay")) return b == net.minecraft.block.Blocks.CLAY;
        if (m.equals("Obsidian")) return b == net.minecraft.block.Blocks.OBSIDIAN;
        if (m.equals("Ice")) return b == net.minecraft.block.Blocks.ICE || b == net.minecraft.block.Blocks.PACKED_ICE || b == net.minecraft.block.Blocks.BLUE_ICE;
        if (m.equals("Snow")) return b == net.minecraft.block.Blocks.SNOW || b == net.minecraft.block.Blocks.SNOW_BLOCK;
        if (m.equals("Glass")) return b == net.minecraft.block.Blocks.GLASS;
        if (m.equals("Bookshelf")) return b == net.minecraft.block.Blocks.BOOKSHELF;
        if (m.equals("Crafting Table")) return b == net.minecraft.block.Blocks.CRAFTING_TABLE;
        if (m.equals("Furnace")) return b == net.minecraft.block.Blocks.FURNACE;
        if (m.equals("Chest")) return b == net.minecraft.block.Blocks.CHEST;
        if (m.equals("Torch")) return b == net.minecraft.block.Blocks.TORCH;
        return false;
    }
}
