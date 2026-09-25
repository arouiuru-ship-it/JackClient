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

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applySwingOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 2, shift = At.Shift.AFTER))
    private void onApplySwingOffset(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!VisualModule.swingAnimation || VisualModule.swingStyle.equals("None")) return;

        Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean isRight = arm == Arm.RIGHT;

        float swing = (float) Math.sin(Math.sqrt(swingProgress) * Math.PI);
        float speed = VisualModule.swingSpeed;

        // Масштаб руки
        matrices.scale(VisualModule.handScale, VisualModule.handScale, VisualModule.handScale);

        // Смещение руки
        matrices.translate(VisualModule.handOffsetX, VisualModule.handOffsetY, VisualModule.handOffsetZ);

        // Базовые вращения руки
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(VisualModule.handRotationX));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(VisualModule.handRotationY));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(VisualModule.handRotationZ));

        // Применяем стиль свинга
        switch (VisualModule.swingStyle) {
            case "No Swing":
                // Убираем боковое движение, оставляем вращение предмета
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-swing * 20.0f * speed));
                break;
            case "Smooth":
                // Плавный замедленный свинг
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-swing * 25.0f * speed * 0.5f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swing * 12.0f * speed * 0.5f));
                break;
            case "Exhibition":
                // Рука вытягивается вперед
                matrices.translate(0.0f, 0.0f, -swing * 0.3f * speed);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swing * 15.0f * speed));
                break;
            case "Spin":
                // Рука вращается вокруг своей оси
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swing * 360.0f * speed));
                break;
            case "Slide":
                // Рука скользит вбок и вперед
                float side = isRight ? -1.0f : 1.0f;
                matrices.translate(side * swing * 0.2f * speed, 0.0f, -swing * 0.15f * speed);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * swing * 30.0f * speed));
                break;
        }
    }
}
