package com.thestalker.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thestalker.entity.StalkerEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Renders the Stalker as a flat, camera-facing billboard quad (no 3D model), matching the
 * original mod. The image is chosen by {@link StalkerEntity#getImageIndex()}; index 0 is the
 * jumpscare image, 1-6 are the stalk images.
 */
public class StalkerRenderer extends EntityRenderer<StalkerEntity, StalkerRenderState> {

    public StalkerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public StalkerRenderState createRenderState() {
        return new StalkerRenderState();
    }

    @Override
    public void extractRenderState(StalkerEntity entity, StalkerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.imageIndex = entity.getImageIndex();
    }

    @Override
    public void submit(StalkerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);

        float height = getHeight(state.imageIndex);
        float halfH = height / 2.0F;
        float halfW = height / 2.0F;
        ResourceLocation texture = getTextureLocation(state.imageIndex);
        int light = state.lightCoords;

        poseStack.pushPose();
        poseStack.translate(0.0F, halfH, 0.0F);
        // CameraRenderState has no yRot/xRot before 26.1: Ry(-yaw) * Rx(pitch) == camera.orientation * Ry(180 deg)
        poseStack.mulPose(camera.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -halfH, 0.0F);

        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(texture), (pose, vc) -> {
            Matrix4f matrix = pose.pose();
            quad(vc, matrix, -halfW, 0.0F, 0.0F, 1.0F, light);
            quad(vc, matrix, halfW, 0.0F, 1.0F, 1.0F, light);
            quad(vc, matrix, halfW, height, 1.0F, 0.0F, light);
            quad(vc, matrix, -halfW, height, 0.0F, 0.0F, light);
        });

        poseStack.popPose();
    }

    private static void quad(VertexConsumer vc, Matrix4f pose, float x, float y, float u, float v, int light) {
        vc.addVertex(pose, x, y, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    public ResourceLocation getTextureLocation(int index) {
        if (index == 0) {
            return ResourceLocation.fromNamespaceAndPath("stalker", "textures/entity/stalkerjumpscare.png");
        }
        return ResourceLocation.fromNamespaceAndPath("stalker", "textures/entity/stalker" + index + ".png");
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
