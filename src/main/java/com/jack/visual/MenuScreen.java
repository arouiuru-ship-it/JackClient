package com.jack.visual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {
    public MenuScreen() { super(Text.literal("JackClient")); }
    private String st(boolean b) { return b ? "§aON" : "§cOFF"; }

    private static final int PW = 420, PH = 200;
    private static final int COLW = 132, ROWH = 22, GAP = 6;
    private int px, py;

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float d) { }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        ctx.fill(0, 0, width, height, 0x77000000);
        px = (width - PW) / 2;
        py = (height - PH) / 2;

        ctx.fill(px - 2, py - 2, px + PW + 2, py + PH + 2, 0xFF0A0A0A);
        ctx.fill(px, py, px + PW, py + PH, 0xFF171717);
        ctx.fill(px, py, px + PW, py + 1, 0xFF00FF88);
        ctx.fill(px, py + PH - 1, px + PW, py + PH, 0xFF00FF88);
        ctx.fill(px, py, px + 1, py + PH, 0xFF00FF88);
        ctx.fill(px + PW - 1, py, px + PW, py + PH, 0xFF00FF88);

        ctx.drawCenteredTextWithShadow(textRenderer, "§a§lJACK §f§lCLIENT §7v1.4", px + PW / 2, py + 6, 0xFFFFFF);
        ctx.fill(px + 8, py + 18, px + PW - 8, py + 19, 0xFF303030);

        int sX = px + 8;
        int sY = py + 24;
        int sX2 = sX + COLW + GAP;

        drawBtn(ctx, mx, my, sX, sY, "KillAura " + st(VisualModule.killAura), VisualModule.killAura);
        drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP), "Crit §e" + VisualModule.critMode, !VisualModule.critMode.equals("Off"));
        drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*2, "ESP " + st(VisualModule.esp), VisualModule.esp);
        drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*3, "Aim " + st(VisualModule.aimAssist), VisualModule.aimAssist);
        drawBtn(ctx, mx, my, sX, sY + (ROWH+GAP)*4, "Smooth §e" + String.format("%.2f", VisualModule.aimSmooth), false);

        drawBtn(ctx, mx, my, sX2, sY, "Fly " + st(VisualModule.fly), VisualModule.fly);
        drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP), "Sprint " + st(VisualModule.autoSprint), VisualModule.autoSprint);
        drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*2, "Bright " + st(VisualModule.fullbright), VisualModule.fullbright);
        drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*3, "HUD " + st(VisualModule.hud), VisualModule.hud);
        drawBtn(ctx, mx, my, sX2, sY + (ROWH+GAP)*4, "§cClose", false);
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
        if (btn != 0) return false;
        px = (width - PW) / 2; py = (height - PH) / 2;
        int sX = px + 8, sY = py + 24;
        int sX2 = sX + COLW + GAP;

        if (hit(mx,my,sX,sY)) { VisualModule.killAura = !VisualModule.killAura; return true; }
        if (hit(mx,my,sX,sY+(ROWH+GAP))) { VisualModule.critMode = VisualModule.critMode.equals("Packet")?"Jump":(VisualModule.critMode.equals("Jump")?"Off":"Packet"); return true; }
        if (hit(mx,my,sX,sY+(ROWH+GAP)*2)) { VisualModule.esp = !VisualModule.esp; return true; }
        if (hit(mx,my,sX,sY+(ROWH+GAP)*3)) { VisualModule.aimAssist = !VisualModule.aimAssist; return true; }
        if (hit(mx,my,sX,sY+(ROWH+GAP)*4)) { VisualModule.aimSmooth += 0.05f; if (VisualModule.aimSmooth > 0.9f) VisualModule.aimSmooth = 0.05f; return true; }

        if (hit(mx,my,sX2,sY)) { VisualModule.fly = !VisualModule.fly; return true; }
        if (hit(mx,my,sX2,sY+(ROWH+GAP))) { VisualModule.autoSprint = !VisualModule.autoSprint; return true; }
        if (hit(mx,my,sX2,sY+(ROWH+GAP)*2)) { VisualModule.fullbright = !VisualModule.fullbright; return true; }
        if (hit(mx,my,sX2,sY+(ROWH+GAP)*3)) { VisualModule.hud = !VisualModule.hud; return true; }
        if (hit(mx,my,sX2,sY+(ROWH+GAP)*4)) { close(); return true; }
        return false;
    }

    private boolean hit(double mx, double my, int x, int y) {
        return mx >= x && mx <= x + COLW && my >= y && my <= y + ROWH;
    }

    @Override
    public boolean shouldPause() { return false; }
}
