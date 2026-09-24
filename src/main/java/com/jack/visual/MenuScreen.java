package com.jack.visual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {
    public MenuScreen() { super(Text.literal("JackClient")); }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 80;
        addDrawableChild(ButtonWidget.builder(Text.literal("KillAura: " + (VisualModule.killAura ? "ON" : "OFF")), b -> { VisualModule.killAura = !VisualModule.killAura; b.setMessage(Text.literal("KillAura: " + (VisualModule.killAura ? "ON" : "OFF"))); }).dimensions(cx - 75, y, 150, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Crits: " + (VisualModule.crits ? "ON" : "OFF")), b -> { VisualModule.crits = !VisualModule.crits; b.setMessage(Text.literal("Crits: " + (VisualModule.crits ? "ON" : "OFF"))); }).dimensions(cx - 75, y, 150, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Fly: " + (VisualModule.fly ? "ON" : "OFF")), b -> { VisualModule.fly = !VisualModule.fly; b.setMessage(Text.literal("Fly: " + (VisualModule.fly ? "ON" : "OFF"))); }).dimensions(cx - 75, y, 150, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Target: " + (VisualModule.targetPlayersOnly ? "Players" : "All")), b -> { VisualModule.targetPlayersOnly = !VisualModule.targetPlayersOnly; b.setMessage(Text.literal("Target: " + (VisualModule.targetPlayersOnly ? "Players" : "All"))); }).dimensions(cx - 75, y, 150, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Range: " + VisualModule.auraRange), b -> { VisualModule.auraRange += 0.5; if (VisualModule.auraRange > 6.0) VisualModule.auraRange = 2.0; b.setMessage(Text.literal("Range: " + VisualModule.auraRange)); }).dimensions(cx - 75, y, 150, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close()).dimensions(cx - 75, y, 150, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        renderBackground(ctx, mx, my, d);
        ctx.drawCenteredTextWithShadow(textRenderer, "JackClient v1.1", width / 2, height / 2 - 105, 0xFFFFFF);
        super.render(ctx, mx, my, d);
    }

    @Override
    public boolean shouldPause() { return false; }
}
