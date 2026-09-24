package com.jack.visual.mixin;

import com.jack.visual.VisualModule;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightmapTextureManager.class)
public class FullbrightMixin {
    @ModifyVariable(method = "update", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyGamma(float gamma) {
        if (VisualModule.fullbright) return 10.0f;
        return gamma;
    }
}
