package com.jack.visual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {
    public MenuScreen() { super(Text.literal("JackClient")); }
    private String st(boolean b) { return b ? "§aON" : "§cOFF"; }

    @Override
    protected void init() {
        int cx = width / 2 - 110;
        int y = height / 2 - 100;
        addDrawableChild(ButtonWidget.builder(Text.literal("KillAura   " + st(VisualModule.killAura)), b -> { VisualModule.killAura = !VisualModule.killAura; b.setMessage(Text.literal("KillAura   " + st(VisualModule.killAura))); }).dimensions(cx, y, 220, 20).build()); y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Crit Mode  §e" + VisualModule.critMode), b -> { VisualModule.critMode = VisualModule.critMode.equals("Packet") ? "Jump" : (VisualModule.critMode.equals("Jump") ? "Off" : "Packet"); b.setMessage(Text.literal("Crit Mode  §e" + VisualModule.critMode)); }).dimensions(cx, y, 220, 20).build()); y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Aim Assist  " + st(VisualModule.aimAssist)), b -> { VisualModule.aimAssist = !VisualModule.aimAssist; b.setMessage(Text.literal("Aim Assist  " + st(VisualModule.aimAssist))); }).dimensions(cx, y, 220, 20).build()); y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Aim Smooth  §e" + String.format("%.2f", VisualModule.aimSmooth)), b -> { VisualModule.aimSmooth += 0.05f; if (VisualModule.aimSmooth > 0.9f) VisualModule.aimSmooth = 0.1f; b.setMessage(Text.literal("Aim Smooth  §e" + String.format("%.2f", VisualModule.aimSmooth))); }).dimensions(cx, y, 220, 20).build()); y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Fly  " + st(VisualModule.fly)), b -> { VisualModule.fly = !VisualModule.fly; b.setMessage(Text.literal("Fly  " + st(VisualModule.fly))); }).dimensions(cx, y, 220, 20).build()); y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("AutoSprint  " + st(VisualModule.autoSprint)), b -> { VisualModule.autoSprint = !VisualModule.autoSprint; b.setMessage(Text.literal("AutoSprint  " + st(VisualModule.autoSprint))); }).dimensions(cx, y, 220, 20).build()); y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Fullbright  " + st(VisualModule.fullbright)), b -> { VisualModule.fullbright = !VisualModule.fullbright; b.setMessage(Text.literal("Fullbright  " + st(VisualModule.fullbright))); }).dimensions(cx, y, 220, 20).build()); y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD  " + st(VisualModule.hud)), b -> { VisualModule.hud = !VisualModule.hud; b.setMessage(Text.literal("HUD  " + st(VisualModule.hud))); }).dimensions(cx, y, 220, 20).build()); y += 28;
        addDrawableChild(ButtonWidget.builder(Text.literal("§cClose"), b -> close()).dimensions(cx, y, 220, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        ctx.fill(0, 0, width, height, 0x88000000);
        int pw = 260, ph = 340;
        int px = (width - pw) / 2, py = (height - ph) / 2;
        ctx.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, 0xFF00AA55);
        ctx.fill(px, py, px + pw, py + ph, 0xF0121212);
        ctx.fill(px, py, px + pw, py + 20, 0xFF00AA55);
        ctx.drawCenteredTextWithShadow(textRenderer, "§lJackClient §fv1.3", width / 2, py + 6, 0xFFFFFF);
        super.render(ctx, mx, my, d);
    }

    @Override
    public boolean shouldPause() { return false; }
}
