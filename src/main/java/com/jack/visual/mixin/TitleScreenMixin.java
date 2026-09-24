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
    @Inject(method = "render", at = @At("TAIL"))
    private void jackRender(DrawContext ctx, int mx, int my, float d, CallbackInfo ci) {
        Screen s = (Screen)(Object)this;
        int w = s.width;
        int h = s.height;
        var tr = s.getTextRenderer();
        int c = 0xFF00FF88;
        int dark = 0x66000000;
        int len = 70, th = 2, pad = 6;

        // Затемнение сверху и снизу для красоты
        ctx.fill(0, 0, w, 55, dark);
        ctx.fill(0, h - 40, w, h, dark);

        // Зелёные рамки по 4 углам
        ctx.fill(pad, pad, pad + len, pad + th, c);
        ctx.fill(pad, pad, pad + th, pad + len, c);

        ctx.fill(w - pad - len, pad, w - pad, pad + th, c);
        ctx.fill(w - pad - th, pad, w - pad, pad + len, c);

        ctx.fill(pad, h - pad - th, pad + len, h - pad, c);
        ctx.fill(pad, h - pad - len, pad + th, h - pad, c);

        ctx.fill(w - pad - len, h - pad - th, w - pad, h - pad, c);
        ctx.fill(w - pad - th, h - pad - len, w - pad, h - pad, c);

        // Заголовок JackClient сверху по центру
        ctx.drawCenteredTextWithShadow(tr, Text.literal("§a§lJ A C K C L I E N T"), w / 2, 14, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(tr, Text.literal("§7v1.5  §8•  §7Fabric 1.21.4"), w / 2, 28, 0xFFFFFF);

        // Hello, Admin в правом верхнем углу
        String hello = "§a● §fHello, §aAdmin";
        int hw = tr.getWidth(hello);
        ctx.drawTextWithShadow(tr, Text.literal(hello), w - hw - 16, 14, 0xFFFFFF);

        // Маленькая строчка в левом верхнем углу
        ctx.drawTextWithShadow(tr, Text.literal("§8jacked §7in §8/ §7ready"), 16, 14, 0xFFFFFF);

        // Внизу слева версия, внизу справа копирайт
        ctx.drawTextWithShadow(tr, Text.literal("§8JackClient §7build §f#dev"), 16, h - 22, 0xFFFFFF);
        String cr = "§8made by §aJack §8& §aFox";
        int cw = tr.getWidth(cr);
        ctx.drawTextWithShadow(tr, Text.literal(cr), w - cw - 16, h - 22, 0xFFFFFF);
    }
}
