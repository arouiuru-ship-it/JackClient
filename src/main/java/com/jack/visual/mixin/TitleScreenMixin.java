package com.jack.visual.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void jackBackground(DrawContext ctx, int mx, int my, float d, CallbackInfo ci) {
        Screen s = (Screen)(Object)this;
        int w = s.width;
        int h = s.height;

        // Рисуем ванильную панораму сначала
        // (не отменяем, просто добавляем поверх свой слой)
        // Ничего не делаем тут — пусть ванильный рендер идёт.
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void jackRender(DrawContext ctx, int mx, int my, float d, CallbackInfo ci) {
        Screen s = (Screen)(Object)this;
        int w = s.width;
        int h = s.height;
        var tr = s.getTextRenderer();
        int green = 0xFF00FF88;
        int pad = 6, len = 70, th = 2;

        // === МЯГКИЙ ГРАДИЕНТ ПОВЕРХ ПАНОРАМЫ ===
        // Верх — от тёмного к прозрачному
        int stepsTop = 40;
        int topH = h / 2;
        for (int i = 0; i < stepsTop; i++) {
            float t = (float)i / stepsTop;
            int alpha = (int)(0xAA * (1.0f - t)); // 170 -> 0
            ctx.fill(0, (int)(topH * t), w, (int)(topH * (t + 1.0f/stepsTop)) + 1, alpha << 24);
        }
        // Низ — от прозрачного к тёмному
        int stepsBot = 40;
        int botH = h / 2;
        for (int i = 0; i < stepsBot; i++) {
            float t = (float)i / stepsBot;
            int alpha = (int)(0xAA * t); // 0 -> 170
            ctx.fill(0, topH + (int)(botH * t), w, topH + (int)(botH * (t + 1.0f/stepsBot)) + 1, alpha << 24);
        }

        // Легкий зелёный оттенок по краям (виньетка)
        ctx.fill(0, 0, w, 2, 0x3300FF88);
        ctx.fill(0, h - 2, w, h, 0x3300FF88);

        // === ЗЕЛЁНЫЕ РАМКИ ПО УГЛАМ ===
        // Верх-лево
        ctx.fill(pad, pad, pad + len, pad + th, green);
        ctx.fill(pad, pad, pad + th, pad + len, green);
        // Верх-право
        ctx.fill(w - pad - len, pad, w - pad, pad + th, green);
        ctx.fill(w - pad - th, pad, w - pad, pad + len, green);
        // Низ-лево
        ctx.fill(pad, h - pad - th, pad + len, h - pad, green);
        ctx.fill(pad, h - pad - len, pad + th, h - pad, green);
        // Низ-право
        ctx.fill(w - pad - len, h - pad - th, w - pad, h - pad, green);
        ctx.fill(w - pad - th, h - pad - len, w - pad, h - pad, green);

        // === ТЕКСТЫ ===
        ctx.drawCenteredTextWithShadow(tr, Text.literal("§a§lJ A C K C L I E N T"), w / 2, 14, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(tr, Text.literal("§7v1.5  §8•  §7Fabric 1.21.4"), w / 2, 28, 0xFFFFFF);

        String hello = "§a● §fHello, §aAdmin";
        int hw = tr.getWidth(hello);
        ctx.drawTextWithShadow(tr, Text.literal(hello), w - hw - 16, 14, 0xFFFFFF);

        ctx.drawTextWithShadow(tr, Text.literal("§8jacked §7in §8/ §7ready"), 16, 14, 0xFFFFFF);

        ctx.drawTextWithShadow(tr, Text.literal("§8JackClient §7build §f#dev"), 16, h - 22, 0xFFFFFF);
        String cr = "§8made by §aJack §8& §aFox";
        int cw = tr.getWidth(cr);
        ctx.drawTextWithShadow(tr, Text.literal(cr), w - cw - 16, h - 22, 0xFFFFFF);
    }
}
