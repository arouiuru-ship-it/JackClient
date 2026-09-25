package com.jack.visual.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoDrawer.class)
public class LogoDrawerMixin {
    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void jackRemoveLogo(DrawContext context, int screenWidth, float alpha, CallbackInfo ci) {
        ci.cancel();
    }
}
