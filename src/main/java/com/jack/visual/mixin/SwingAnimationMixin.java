package com.jack.visual.mixin;

import com.jack.visual.VisualModule;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class SwingAnimationMixin {

    @Inject(method = "applySwingOffset", at = @At("TAIL"))
    private static void jackSwingOffset(MatrixStack matrices, Arm arm, float swingProgress, CallbackInfo ci) {
        if (!VisualModule.swingAnimation || VisualModule.swingStyle.equals("None")) return;

        float swing = (float) Math.sin(Math.sqrt(swingProgress) * Math.PI);
        float speed = VisualModule.swingSpeed;
        boolean isRight = arm == Arm.RIGHT;

        matrices.scale(VisualModule.handScale, VisualModule.handScale, VisualModule.handScale);
        matrices.translate(VisualModule.handOffsetX, VisualModule.handOffsetY, VisualModule.handOffsetZ);

        switch (VisualModule.swingStyle) {
            case "Smooth":
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-swing * 20f * speed * 0.6f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swing * 10f * speed * 0.6f));
                break;
            case "Exhibition":
                matrices.translate(0f, 0f, -swing * 0.25f * speed);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swing * 25f * speed));
                break;
            case "Spin":
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swing * 360f * speed));
                break;
            case "Slide":
                float side = isRight ? -1f : 1f;
                matrices.translate(side * swing * 0.18f * speed, 0f, -swing * 0.12f * speed);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * swing * 25f * speed));
                break;
            case "No Swing":
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-swing * 20f * speed));
                break;
        }
    }
}
