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
        int cx = width / 2 - 75;
        int y = height / 2 - 95;
        addDrawableChild(ButtonWidget.builder(Text.literal("KillAura " + st(VisualModule.killAura)), b -> { VisualModule.killAura = !VisualModule.killAura; b.setMessage(Text.literal("KillAura " + st(VisualModule.killAura))); }).dimensions(cx, y, 150, 16).build()); y += 18;
        addDrawableChild(ButtonWidget.builder(Text.literal("Crit §e" + VisualModule.critMode), b -> { VisualModule.critMode = VisualModule.critMode.equals("Packet") ? "Jump" : (VisualModule.critMode.equals("Jump") ? "Off" : "Packet"); b.setMessage(Text.literal("Crit §e" + VisualModule.critMode)); }).dimensions(cx, y, 150, 16).build()); y += 18;
        addDrawableChild(ButtonWidget.builder(Text.literal("ESP " + st(VisualModule.esp)), b -> { VisualModule.esp = !VisualModule.esp; b.setMessage(Text.literal("ESP " + st(VisualModule.esp))); }).dimensions(cx, y, 150, 16).build()); y += 18;
        addDrawableChild(ButtonWidget.builder(Text.literal("Aim " + st(VisualModule.aimAssist) + " §7sm §e" + String.format("%.2f", VisualModule.aimSmooth)), b -> { if (!VisualModule.aimAssist) { VisualModule.aimAssist = true; } else { VisualModule.aimSmooth += 0.05f; if (VisualModule.aimSmooth > 0.9f) { VisualModule.aimSmooth = 0.1f; VisualModule.aimAssist = false; } } b.setMessage(Text.literal("Aim " + st(VisualModule.aimAssist) + " §7sm §e" + String.format("%.2f", VisualModule.aimSmooth))); }).dimensions(cx, y, 150, 16).build()); y += 18;
        addDrawableChild(ButtonWidget.builder(Text.literal("Fly " + st(VisualModule.fly)), b -> { VisualModule.fly = !VisualModule.fly; b.setMessage(Text.literal("Fly " + st(VisualModule.fly))); }).dimensions(cx, y, 150, 16).build()); y += 18;
        addDrawableChild(ButtonWidget.builder(Text.literal("Sprint " + st(VisualModule.autoSprint)), b -> { VisualModule.autoSprint = !VisualModule.autoSprint; b.setMessage(Text.literal("Sprint " + st(VisualModule.autoSprint))); }).dimensions(cx, y, 150, 16).build()); y += 18;
        addDrawableChild(ButtonWidget.builder(Text.literal("Bright " + st(VisualModule.fullbright)), b -> { VisualModule.fullbright = !VisualModule.fullbright; b.setMessage(Text.literal("Bright " + st(VisualModule.fullbright))); }).dimensions(cx, y, 150, 16).build()); y += 18;
        addDrawableChild(ButtonWidget.builder(Text.literal("CPS §e" + VisualModule.auraCps), b -> { VisualModule.auraCps++; if (VisualModule.auraCps > 20) VisualModule.auraCps = 2; b.setMessage(Text.literal("CPS §e" + VisualModule.auraCps)); }).dimensions(cx, y, 150, 16).build()); y += 18;
        addDrawableChild(ButtonWidget.builder(Text.literal("§cClose"), b -> close()).dimensions(cx, y, 150, 16).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        ctx.fill(0, 0, width, height, 0x66000000);
        int pw = 180, ph = 220;
        int px = (width - pw) / 2, py = (height - ph) / 2;
        ctx.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF00AA55);
        ctx.fill(px, py, px + pw, py + ph, 0xEE121212);
        ctx.fill(px, py, px + pw, py + 14, 0xFF00AA55);
        ctx.drawCenteredTextWithShadow(textRenderer, "§lJackClient", width / 2, py + 3, 0xFFFFFF);
        super.render(ctx, mx, my, d);
    }

    @Override
    public boolean shouldPause() { return false; }
}
