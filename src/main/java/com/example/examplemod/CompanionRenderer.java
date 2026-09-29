package com.example.examplemod;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CompanionRenderer extends MobRenderer<CompanionEntity, HumanoidModel<CompanionEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    public CompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CompanionEntity entity) {
        return TEXTURE;
    }
}
