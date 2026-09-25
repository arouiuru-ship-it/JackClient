package com.jack.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {
    public MenuScreen() { super(Text.literal("JackClient")); }
    private String st(boolean b) { return b ? "§aON" : "§cOFF"; }
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final int PW = 420, PH = 230;
    private static final int COLW = 132, ROWH = 22, GAP = 6;
    private static final int TABW = 100, TABH = 20;
    private int px, py;
    private static int tab = 0;

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float d) { }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        ctx.fill(0, 0, width, height, 0x77000000);
        px = (width - PW) / 2; py = (height - PH) / 2;
        ctx.fill(px - 2, py - 2, px + PW + 2, py + PH + 2, 0xFF0A0A0A);
        ctx.fill(px, py, px + PW, py + PH, 0xFF171717);
        ctx.fill(px, py, px + PW, py + 1, 0xFF00FF88);
        ctx.fill(px, py + PH - 1, px + PW, py + PH, 0xFF00FF88);
        ctx.fill(px, py, px + 1, py + PH, 0xFF00FF88);
        ctx.fill(px + PW - 1, py, px + PW, py + PH, 0xFF00FF88);

        ctx.drawCenteredTextWithShadow(textRenderer, "§a§lJACK §f§lCLIENT §7v3.1", px + PW / 2, py + 6, 0xFFFFFF);
        ctx.fill(px + 8, py + 18, px + PW - 8, py + 19, 0xFF303030);

        int tabY = py + 24;
        drawTab(ctx, mx, my, px + 8, tabY, "COMBAT", tab == 0);
        drawTab(ctx, mx, my, px + 8 + TABW + 4, tabY, "VISUAL", tab == 1);

        int sX = px + 8, sY = py + 50;
        int sX2 = sX + COLW + GAP;

        if (tab == 0) {
            drawBtn(ctx, mx, my, sX, sY, "KillAura " + st(VisualModule.killAura), VisualModule.killAura);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP), "Aim Assist " + st(VisualModule.aimAssist), VisualModule.aimAssist);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*2, "Smooth §e" + String.format("%.2f", VisualModule.aimSmooth), false);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*3, "Reach §e" + String.format("%.1f", VisualModule.auraRange), false);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*4, "AutoMine " + st(VisualModule.autoMine), VisualModule.autoMine);

            drawBtn(ctx, mx, my, sX2, sY, "Block: §e" + VisualModule.autoMineBlock, false);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP), "AutoSprint " + st(VisualModule.autoSprint), VisualModule.autoSprint);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*2, "NoFall " + st(VisualModule.noFall), VisualModule.noFall);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*3, "Crit §e" + VisualModule.critMode, !VisualModule.critMode.equals("Off"));
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*4, "§cClose", false);
        } else {
            drawBtn(ctx, mx, my, sX, sY, "PlayerESP " + st(VisualModule.esp), VisualModule.esp);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP), "JumpCircle " + st(VisualModule.jumpCircle), VisualModule.jumpCircle);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*2, "SwingAnim " + st(VisualModule.swingAnimation), VisualModule.swingAnimation);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*3, "Style: §e" + VisualModule.swingStyle, false);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*4, "SwingSpd §e" + String.format("%.2f", VisualModule.swingSpeed), false);

            drawBtn(ctx, mx, my, sX2, sY, "Fullbright " + st(VisualModule.fullbright), VisualModule.fullbright);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP), "Optimized " + st(VisualModule.optimized), VisualModule.optimized);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*2, "§7FPS: §f" + mc.getCurrentFps(), false);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*3, "§7Target: §f" + (VisualModule.lastTarget != null ? VisualModule.lastTarget : "none"), false);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*4, "§cClose", false);
        }
    }

    private void drawTab(DrawContext ctx, int mx, int my, int x, int y, String label, boolean active) {
        boolean hov = mx >= x && mx <= x + TABW && my >= y && my <= y + TABH;
        int bg = active ? 0xFF00AA55 : (hov ? 0xFF2A2A2A : 0xFF1F1F1F);
        ctx.fill(x, y, x + TABW, y + TABH, bg);
        ctx.fill(x, y, x + TABW, y + 1, 0xFF00FF88);
        ctx.drawCenteredTextWithShadow(textRenderer, label, x + TABW / 2, y + 6, 0xFFFFFF);
    }

    private void drawBtn(DrawContext ctx, int mx, int my, int x, int y, String label, boolean active) {
        boolean hov = mx >= x && mx <= x + COLW && my >= y && my <= y + ROWH;
        int bg = hov ? 0xFF2A2A2A : 0xFF1F1F1F;
        if (active) bg = hov ? 0xFF004D2A : 0xFF00331F;
        ctx.fill(x, y, x + COLW, y + ROWH, bg);
        ctx.fill(x, y, x + 2, y + ROWH, active ? 0xFF00FF88 : 0xFF444444);
        ctx.fill(x, y, x + COLW, y + 1, hov ? 0xFF00FF88 : 0xFF3A3A3A);
        ctx.fill(x, y + ROWH - 1, x + COLW, y + ROWH, hov ? 0xFF00FF88 : 0xFF3A3A3A);
        ctx.fill(x + COLW - 1, y, x + COLW, y + ROWH, hov ? 0xFF00FF88 : 0xFF3A3A3A);
        ctx.drawTextWithShadow(textRenderer, label, x + 7, y + (ROWH - 8) / 2, 0xFFFFFF);
    }

    private String[] SWING_STYLES = {"Smooth", "Exhibition", "Spin", "Slide", "No Swing", "None"};

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return false;
        px = (width - PW) / 2; py = (height - PH) / 2;
        int tabY = py + 24;
        if (hitW(mx, my, px + 8, tabY, TABW, TABH)) { tab = 0; return true; }
        if (hitW(mx, my, px + 8 + TABW + 4, tabY, TABW, TABH)) { tab = 1; return true; }

        int sX = px + 8, sY = py + 50;
        int sX2 = sX + COLW + GAP;

        if (tab == 0) {
            if (hit(mx,my,sX,sY)) { VisualModule.killAura = !VisualModule.killAura; return true; }
            if (hit(mx,my,sX,sY+(ROWH+GAP))) { VisualModule.aimAssist = !VisualModule.aimAssist; return true; }
            if (hit(mx,my,sX,sY+(ROWH+GAP)*2)) { VisualModule.aimSmooth += 0.05f; if (VisualModule.aimSmooth > 0.9f) VisualModule.aimSmooth = 0.05f; return true; }
            if (hit(mx,my,sX,sY+(ROWH+GAP)*3)) { VisualModule.auraRange += 0.5; if (VisualModule.auraRange > 6.0) VisualModule.auraRange = 3.0; return true; }
            if (hit(mx,my,sX,sY+(ROWH+GAP)*4)) { VisualModule.autoMine = !VisualModule.autoMine; return true; }
            if (hit(mx,my,sX2,sY)) { int idx = 0; for (int i = 0; i < VisualModule.AUTO_MINE_BLOCKS.length; i++) if (VisualModule.AUTO_MINE_BLOCKS[i].equals(VisualModule.autoMineBlock)) { idx = i; break; } VisualModule.autoMineBlock = VisualModule.AUTO_MINE_BLOCKS[(idx + 1) % VisualModule.AUTO_MINE_BLOCKS.length]; return true; }
            if (hit(mx,my,sX2,sY+(ROWH+GAP))) { VisualModule.autoSprint = !VisualModule.autoSprint; return true; }
            if (hit(mx,my,sX2,sY+(ROWH+GAP)*2)) { VisualModule.noFall = !VisualModule.noFall; return true; }
            if (hit(mx,my,sX2,sY+(ROWH+GAP)*3)) { VisualModule.critMode = VisualModule.critMode.equals("Off")?"Jump":(VisualModule.critMode.equals("Jump")?"Packet":"Off"); return true; }
            if (hit(mx,my,sX2,sY+(ROWH+GAP)*4)) { close(); return true; }
        } else {
            if (hit(mx,my,sX,sY)) { VisualModule.esp = !VisualModule.esp; return true; }
            if (hit(mx,my,sX,sY+(ROWH+GAP))) { VisualModule.jumpCircle = !VisualModule.jumpCircle; return true; }
            if (hit(mx,my,sX,sY+(ROWH+GAP)*2)) { VisualModule.swingAnimation = !VisualModule.swingAnimation; return true; }
            if (hit(mx,my,sX,sY+(ROWH+GAP)*3)) {
                int idx = 0;
                for (int i = 0; i < SWING_STYLES.length; i++) if (SWING_STYLES[i].equals(VisualModule.swingStyle)) { idx = i; break; }
                VisualModule.swingStyle = SWING_STYLES[(idx + 1) % SWING_STYLES.length];
                return true;
            }
            if (hit(mx,my,sX,sY+(ROWH+GAP)*4)) { VisualModule.swingSpeed += 0.1f; if (VisualModule.swingSpeed > 2.0f) VisualModule.swingSpeed = 0.5f; return true; }
            if (hit(mx,my,sX2,sY)) { VisualModule.fullbright = !VisualModule.fullbright; return true; }
            if (hit(mx,my,sX2,sY+(ROWH+GAP))) { VisualModule.optimized = !VisualModule.optimized; return true; }
            if (hit(mx,my,sX2,sY+(ROWH+GAP)*4)) { close(); return true; }
        }
        return false;
    }

    private boolean hit(double mx, double my, int x, int y) {
        return mx >= x && mx <= x + COLW && my >= y && my <= y + ROWH;
    }
    private boolean hitW(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean shouldPause() { return false; }
}
