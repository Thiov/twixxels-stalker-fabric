package com.thestalker.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thestalker.entity.StalkerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Renders the Stalker as a flat, camera-facing billboard quad (no 3D model), matching the
 * original mod. The image is chosen by {@link StalkerEntity#getImageIndex()}; index 0 is the
 * jumpscare image, 1-6 are the stalk images.
 *
 * <p>1.20.1 has no render states / submit pipeline: the quad is drawn straight from {@code render}.</p>
 */
public class StalkerRenderer extends EntityRenderer<StalkerEntity> {

    public StalkerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(StalkerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        int imageIndex = entity.getImageIndex();
        float height = getHeight(imageIndex);
        float halfH = height / 2.0F;
        float halfW = height / 2.0F;
        ResourceLocation texture = getTextureLocation(imageIndex);

        poseStack.pushPose();
        poseStack.translate(0.0F, halfH, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-this.entityRenderDispatcher.camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(this.entityRenderDispatcher.camera.getXRot()));
        poseStack.translate(0.0F, -halfH, 0.0F);

        Matrix4f pose = poseStack.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(texture));
        quad(vc, pose, -halfW, 0.0F, 0.0F, 1.0F, packedLight);
        quad(vc, pose, halfW, 0.0F, 1.0F, 1.0F, packedLight);
        quad(vc, pose, halfW, height, 1.0F, 0.0F, packedLight);
        quad(vc, pose, -halfW, height, 0.0F, 0.0F, packedLight);

        poseStack.popPose();
    }

    private static void quad(VertexConsumer vc, Matrix4f pose, float x, float y, float u, float v, int light) {
        vc.vertex(pose, x, y, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(StalkerEntity entity) {
        return getTextureLocation(entity.getImageIndex());
    }

    public ResourceLocation getTextureLocation(int index) {
        if (index == 0) {
            return new ResourceLocation("stalker", "textures/entity/stalkerjumpscare.png");
        }
        return new ResourceLocation("stalker", "textures/entity/stalker" + index + ".png");
    }

    private static float getHeight(int index) {
        return switch (index) {
            case 0 -> 5.0F;
            case 1 -> 4.0F;
            case 2 -> 7.0F;
            case 3 -> 4.0F;
            case 4 -> 4.0F;
            case 5 -> 5.0F;
            case 6 -> 5.0F;
            default -> 3.0F;
        };
    }
}
