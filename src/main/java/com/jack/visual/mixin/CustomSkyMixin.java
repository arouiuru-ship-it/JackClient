package com.jack.visual.mixin;

import com.jack.visual.VisualModule;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class CustomSkyMixin {
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void jackCustomSky(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (VisualModule.customSky) {
            ci.cancel();
        }
    }
}
