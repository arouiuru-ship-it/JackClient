package com.jack.visual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {
    public MenuScreen() { super(Text.literal("JackClient")); }

    private String on(boolean b) { return b ? "§aON" : "§cOFF"; }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 100;

        addDrawableChild(ButtonWidget.builder(Text.literal("§b§lCOMBAT"), b -> {}).dimensions(cx - 110, y, 220, 16).build());
        y += 20;
        addDrawableChild(ButtonWidget.builder(Text.literal("KillAura  " + on(VisualModule.killAura)), b -> { VisualModule.killAura = !VisualModule.killAura; b.setMessage(Text.literal("KillAura  " + on(VisualModule.killAura))); }).dimensions(cx - 110, y, 220, 20).build());
        y += 22;
        addDrawableChild(ButtonWidget.builder(Text.literal("Crit Mode: §e" + VisualModule.critMode), b -> {
            if (VisualModule.critMode.equals("Packet")) VisualModule.critMode = "Jump";
            else if (VisualModule.critMode.equals("Jump")) VisualModule.critMode = "Off";
            else VisualModule.critMode = "Packet";
            b.setMessage(Text.literal("Crit Mode: §e" + VisualModule.critMode));
        }).dimensions(cx - 110, y, 220, 20).build());
        y += 22;
        addDrawableChild(ButtonWidget.builder(Text.literal("Aim Assist  " + on(VisualModule.aimAssist)), b -> { VisualModule.aimAssist = !VisualModule.aimAssist; b.setMessage(Text.literal("Aim Assist  " + on(VisualModule.aimAssist))); }).dimensions(cx - 110, y, 220, 20).build());
        y += 22;
        addDrawableChild(ButtonWidget.builder(Text.literal("Range: §e" + String.format("%.1f", VisualModule.auraRange) + " §7| CPS: §e" + VisualModule.auraCps), b -> {
            VisualModule.auraRange += 0.5;
            if (VisualModule.auraRange > 6.0) { VisualModule.auraRange = 2.0; VisualModule.auraCps += 1; if (VisualModule.auraCps > 20) VisualModule.auraCps = 3; }
            b.setMessage(Text.literal("Range: §e" + String.format("%.1f", VisualModule.auraRange) + " §7| CPS: §e" + VisualModule.auraCps));
        }).dimensions(cx - 110, y, 220, 20).build());
        y += 24;

        addDrawableChild(ButtonWidget.builder(Text.literal("§b§lMOVEMENT"), b -> {}).dimensions(cx - 110, y, 220, 16).build());
        y += 20;
        addDrawableChild(ButtonWidget.builder(Text.literal("Fly  " + on(VisualModule.fly) + "  §7| Speed: §e" + String.format("%.2f", VisualModule.flySpeed)), b -> {
            if (!VisualModule.fly) { VisualModule.fly = true; }
            else { VisualModule.flySpeed += 0.05f; if (VisualModule.flySpeed > 0.5f) { VisualModule.flySpeed = 0.05f; VisualModule.fly = false; } }
            b.setMessage(Text.literal("Fly  " + on(VisualModule.fly) + "  §7| Speed: §e" + String.format("%.2f", VisualModule.flySpeed)));
        }).dimensions(cx - 110, y, 220, 20).build());
        y += 22;
        addDrawableChild(ButtonWidget.builder(Text.literal("AutoSprint  " + on(VisualModule.autoSprint)), b -> { VisualModule.autoSprint = !VisualModule.autoSprint; b.setMessage(Text.literal("AutoSprint  " + on(VisualModule.autoSprint))); }).dimensions(cx - 110, y, 220, 20).build());
        y += 22;
        addDrawableChild(ButtonWidget.builder(Text.literal("NoFall  " + on(VisualModule.noFall)), b -> { VisualModule.noFall = !VisualModule.noFall; b.setMessage(Text.literal("NoFall  " + on(VisualModule.noFall))); }).dimensions(cx - 110, y, 220, 20).build());
        y += 24;

        addDrawableChild(ButtonWidget.builder(Text.literal("§b§lRENDER"), b -> {}).dimensions(cx - 110, y, 220, 16).build());
        y += 20;
        addDrawableChild(ButtonWidget.builder(Text.literal("PlayerESP  " + on(VisualModule.esp)), b -> { VisualModule.esp = !VisualModule.esp; b.setMessage(Text.literal("PlayerESP  " + on(VisualModule.esp))); }).dimensions(cx - 110, y, 220, 20).build());
        y += 22;
        addDrawableChild(ButtonWidget.builder(Text.literal("Fullbright  " + on(VisualModule.fullbright)), b -> { VisualModule.fullbright = !VisualModule.fullbright; b.setMessage(Text.literal("Fullbright  " + on(VisualModule.fullbright))); }).dimensions(cx - 110, y, 220, 20).build());
        y += 24;

        addDrawableChild(ButtonWidget.builder(Text.literal("§cClose"), b -> close()).dimensions(cx - 110, y, 220, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        ctx.fill(0, 0, width, height, 0x88000000);
        int pw = 260, ph = 380;
        int px = (width - pw) / 2, py = (height - ph) / 2;
        ctx.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, 0xFF00AA55);
        ctx.fill(px, py, px + pw, py + ph, 0xF0101010);
        ctx.drawCenteredTextWithShadow(textRenderer, "§a§lJackClient §f§lv1.3", width / 2, py + 10, 0xFFFFFF);
        super.render(ctx, mx, my, d);
    }

    @Override
    public boolean shouldPause() { return false; }
}
