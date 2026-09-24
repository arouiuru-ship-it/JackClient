package com.jack.visual.mixin;

import com.jack.visual.VisualModule;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void onRenderItem(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        // Масштаб руки
        matrices.scale(VisualModule.handScale, VisualModule.handScale, VisualModule.handScale);
        // Волновая анимация
        float wave = (float)Math.sin(System.currentTimeMillis() * 0.005 * VisualModule.handSwingSpeed) * VisualModule.handWaveIntensity * 0.1f;
        matrices.translate(0, wave, 0);
    }
}
