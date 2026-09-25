package com.jack.visual.mixin;

import com.jack.visual.VisualModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class SwingAnimationMixin {

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 2, shift = At.Shift.AFTER))
    private void onApplySwingOffset(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!VisualModule.swingAnimation) return;

        Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();

        // Масштаб руки
        matrices.scale(VisualModule.handScale, VisualModule.handScale, VisualModule.handScale);

        // Позиция руки (смещение)
        matrices.translate(VisualModule.handOffsetX, VisualModule.handOffsetY, VisualModule.handOffsetZ);

        // Наклон руки (вращение)
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(VisualModule.handRotationX));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(VisualModule.handRotationY));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(VisualModule.handRotationZ));

        // Скорость и сила замаха
        float swing = (float) Math.sin(Math.sqrt(swingProgress) * Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-swing * 20.0f * VisualModule.swingSpeed));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swing * 10.0f * VisualModule.swingSpeed));
    }
}
