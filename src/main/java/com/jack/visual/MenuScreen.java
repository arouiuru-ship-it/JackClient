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
    private static int tab = 0;                 // 0=Combat, 1=Visual
    private static boolean killAuraSub = false; // подменю KillAura

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float d) { }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        ctx.fill(0, 0, width, height, 0x77000000);
        px = (width - PW) / 2; py = (height - PH) / 2;

        // Рамка
        ctx.fill(px - 2, py - 2, px + PW + 2, py + PH + 2, 0xFF0A0A0A);
        ctx.fill(px, py, px + PW, py + PH, 0xFF171717);
        ctx.fill(px, py, px + PW, py + 1, 0xFF00FF88);
        ctx.fill(px, py + PH - 1, px + PW, py + PH, 0xFF00FF88);
        ctx.fill(px, py, px + 1, py + PH, 0xFF00FF88);
        ctx.fill(px + PW - 1, py, px + PW, py + PH, 0xFF00FF88);

        if (killAuraSub) {
            renderKillAuraSub(ctx, mx, my);
            return;
        }

        // Заголовок
        ctx.drawCenteredTextWithShadow(textRenderer, "§a§lJACK §f§lCLIENT §7v1.8", px + PW / 2, py + 6, 0xFFFFFF);
        ctx.fill(px + 8, py + 18, px + PW - 8, py + 19, 0xFF303030);

        // Вкладки
        int tabY = py + 24;
        drawTab(ctx, mx, my, px + 8, tabY, "COMBAT", tab == 0);
        drawTab(ctx, mx, my, px + 8 + TABW + 4, tabY, "VISUAL", tab == 1);

        int sX = px + 8, sY = py + 50;
        int sX2 = sX + COLW + GAP;

        if (tab == 0) {
            // ===== COMBAT =====
            drawBtn(ctx, mx, my, sX, sY, "KillAura " + st(VisualModule.killAura), VisualModule.killAura);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP), "§7LMB toggle §8| §7RMB settings", false);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*2, "Aim Assist " + st(VisualModule.aimAssist), VisualModule.aimAssist);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*3, "Smooth §e" + String.format("%.2f", VisualModule.aimSmooth), false);

            drawBtn(ctx, mx, my, sX2, sY, "Fly " + st(VisualModule.fly), VisualModule.fly);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP), "AutoSprint " + st(VisualModule.autoSprint), VisualModule.autoSprint);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*2, "NoFall " + st(VisualModule.noFall), VisualModule.noFall);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*3, "§cClose", false);
        } else {
            // ===== VISUAL =====
            drawBtn(ctx, mx, my, sX, sY, "PlayerESP " + st(VisualModule.esp), VisualModule.esp);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP), "TargetHUD " + st(VisualModule.targetHud), VisualModule.targetHud);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*2, "Wave Model " + st(VisualModule.waveModel), VisualModule.waveModel);
            drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*3, "HUD " + st(VisualModule.hud), VisualModule.hud);

            drawBtn(ctx, mx, my, sX2, sY, "Fullbright " + st(VisualModule.fullbright), VisualModule.fullbright);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP), "§7FPS: §f" + mc.getCurrentFps(), false);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*2, "§7Target: §f" + (VisualModule.lastTarget != null ? VisualModule.lastTarget : "none"), false);
            drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*3, "§cClose", false);
        }
    }

    private void renderKillAuraSub(DrawContext ctx, int mx, int my) {
        ctx.drawCenteredTextWithShadow(textRenderer, "§a§lKILLAURA §f§lSETTINGS", px + PW / 2, py + 6, 0xFFFFFF);
        ctx.fill(px + 8, py + 18, px + PW - 8, py + 19, 0xFF303030);

        int sX = px + 8, sY = py + 40;
        int sX2 = sX + COLW + GAP;

        drawBtn(ctx, mx, my, sX, sY, "Crit Mode §e" + VisualModule.critMode, !VisualModule.critMode.equals("Off"));
        drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP), "Reach §e" + String.format("%.1f", VisualModule.auraRange), false);
        drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*2, "CPS §e" + VisualModule.auraCps, false);
        drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*3, "Target: " + (VisualModule.targetPlayersOnly ? "Players" : "All"), false);

        drawBtn(ctx, mx, my, sX2, sY, "§7Crit: Packet/Jump/Off", false);
        drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP), "§7Reach: 3.0 - 6.0 blocks", false);
        drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*2, "§7CPS: 2 - 20", false);
        drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*3, "§c◀ Back to Combat", false);
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

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        px = (width - PW) / 2; py = (height - PH) / 2;

        // ===== ПОДМЕНЮ KILLAURA =====
        if (killAuraSub) {
            if (btn == 1) { killAuraSub = false; return true; } // ПКМ закрывает
            if (btn == 0) {
                int sX = px + 8, sY = py + 40;
                int sX2 = sX + COLW + GAP;
                if (hit(mx,my,sX,sY)) { VisualModule.critMode = VisualModule.critMode.equals("Packet")?"Jump":(VisualModule.critMode.equals("Jump")?"Off":"Packet"); return true; }
                if (hit(mx,my,sX,sY+(ROWH+GAP))) { VisualModule.auraRange += 0.5; if (VisualModule.auraRange > 6.0) VisualModule.auraRange = 3.0; return true; }
                if (hit(mx,my,sX,sY+(ROWH+GAP)*2)) { VisualModule.auraCps++; if (VisualModule.auraCps > 20) VisualModule.auraCps = 2; return true; }
                if (hit(mx,my,sX,sY+(ROWH+GAP)*3)) { VisualModule.targetPlayersOnly = !VisualModule.targetPlayersOnly; return true; }
                if (hit(mx,my,sX2,sY+(ROWH+GAP)*3)) { killAuraSub = false; return true; }
            }
            return false;
        }

        // ===== ВКЛАДКИ =====
        int tabY = py + 24;
        if (btn == 0) {
            if (hitW(mx, my, px + 8, tabY, TABW, TABH)) { tab = 0; return true; }
            if (hitW(mx, my, px + 8 + TABW + 4, tabY, TABW, TABH)) { tab = 1; return true; }
        }

        int sX = px + 8, sY = py + 50;
        int sX2 = sX + COLW + GAP;

        if (tab == 0) {
            // KillAura: LMB toggle, RMB submenu
            if (hit(mx,my,sX,sY)) {
                if (btn == 0) { VisualModule.killAura = !VisualModule.killAura; return true; }
                if (btn == 1) { killAuraSub = true; return true; }
            }
            if (btn == 0) {
                if (hit(mx,my,sX,sY+(ROWH+GAP)*2)) { VisualModule.aimAssist = !VisualModule.aimAssist; return true; }
                if (hit(mx,my,sX,sY+(ROWH+GAP)*3)) { VisualModule.aimSmooth += 0.05f; if (VisualModule.aimSmooth > 0.9f) VisualModule.aimSmooth = 0.05f; return true; }
                if (hit(mx,my,sX2,sY)) { VisualModule.fly = !VisualModule.fly; return true; }
                if (hit(mx,my,sX2,sY+(ROWH+GAP))) { VisualModule.autoSprint = !VisualModule.autoSprint; return true; }
                if (hit(mx,my,sX2,sY+(ROWH+GAP)*2)) { VisualModule.noFall = !VisualModule.noFall; return true; }
                if (hit(mx,my,sX2,sY+(ROWH+GAP)*3)) { close(); return true; }
            }
        } else {
            if (btn == 0) {
                if (hit(mx,my,sX,sY)) { VisualModule.esp = !VisualModule.esp; return true; }
                if (hit(mx,my,sX,sY+(ROWH+GAP))) { VisualModule.targetHud = !VisualModule.targetHud; return true; }
                if (hit(mx,my,sX,sY+(ROWH+GAP)*2)) { VisualModule.waveModel = !VisualModule.waveModel; return true; }
                if (hit(mx,my,sX,sY+(ROWH+GAP)*3)) { VisualModule.hud = !VisualModule.hud; return true; }
                if (hit(mx,my,sX2,sY)) { VisualModule.fullbright = !VisualModule.fullbright; return true; }
                if (hit(mx,my,sX2,sY+(ROWH+GAP)*3)) { close(); return true; }
            }
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
