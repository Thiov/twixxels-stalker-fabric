package com.thestalker.registry;

import com.thestalker.entity.StalkerEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final EntityType<StalkerEntity> STALKER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation("stalker", "stalker"),
            EntityType.Builder.<StalkerEntity>of(StalkerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.7F)
                    .clientTrackingRange(64)
                    .build("stalker"));

    public static void init() {}
}
