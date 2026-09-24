package com.jack.visual.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    // Путь к твоей картинке. Кинь PNG в src/main/resources/assets/jack-visual/textures/bg.png
    private static final Identifier BG = Identifier.of("jack-visual", "textures/bg.png");

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void jackBackground(DrawContext ctx, int mx, int my, float d, CallbackInfo ci) {
        ci.cancel();
        Screen s = (Screen)(Object)this;
        int w = s.width;
        int h = s.height;

        // ===== ФОН =====
        // Если картинка есть в ресурсах — рисуем её, растягивая на весь экран
        try {
            ctx.drawTexture(BG, 0, 0, 0, 0, w, h, w, h);
        } catch (Exception e) {
            // Фолбэк — мягкий тёмно-синий градиент
            int steps = 40;
            for (int i = 0; i < steps; i++) {
                float t = (float)i / steps;
                int r = (int)(0x18 + (0x08 - 0x18) * t);
                int g = (int)(0x18 + (0x08 - 0x18) * t);
                int b = (int)(0x28 + (0x10 - 0x28) * t);
                int col = 0xFF000000 | (r << 16) | (g << 8) | b;
                ctx.fill(0, (int)(h * t), w, (int)(h * (t + 1.0f/steps)) + 1, col);
            }
        }

        // Лёгкое затемнение для читаемости текста
        ctx.fill(0, 0, w, 40, 0x55000000);
        ctx.fill(0, h - 30, w, h, 0x55000000);

        // Мягкое зелёное свечение сверху
        ctx.fill(0, 0, w, 1, 0xAA00FF88);
        ctx.fill(0, h - 1, w, h, 0xAA00FF88);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void jackRender(DrawContext ctx, int mx, int my, float d, CallbackInfo ci) {
        Screen s = (Screen)(Object)this;
        int w = s.width;
        int h = s.height;
        var tr = s.getTextRenderer();
        int green = 0xFF00FF88;
        int pad = 6, len = 70, th = 2;

        // Рамки по углам
        ctx.fill(pad, pad, pad + len, pad + th, green);
        ctx.fill(pad, pad, pad + th, pad + len, green);
        ctx.fill(w - pad - len, pad, w - pad, pad + th, green);
        ctx.fill(w - pad - th, pad, w - pad, pad + len, green);
        ctx.fill(pad, h - pad - th, pad + len, h - pad, green);
        ctx.fill(pad, h - pad - len, pad + th, h - pad, green);
        ctx.fill(w - pad - len, h - pad - th, w - pad, h - pad, green);
        ctx.fill(w - pad - th, h - pad - len, w - pad, h - pad, green);

        // Тексты
        ctx.drawCenteredTextWithShadow(tr, Text.literal("§a§lJ A C K C L I E N T"), w / 2, 14, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(tr, Text.literal("§7v1.6  §8•  §7Fabric 1.21.4"), w / 2, 28, 0xFFFFFF);
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
