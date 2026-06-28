package com.thestalker.registry;

import com.thestalker.entity.StalkerEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final ResourceKey<EntityType<?>> STALKER_KEY =
            ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(),
                    Identifier.fromNamespaceAndPath("stalker", "stalker"));

    public static final EntityType<StalkerEntity> STALKER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            STALKER_KEY,
            EntityType.Builder.<StalkerEntity>of(StalkerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.7F)
                    .clientTrackingRange(64)
                    .build(STALKER_KEY));

    public static void init() {}
}
