package com.thestalker;

import com.mojang.logging.LogUtils;
import com.thestalker.command.StalkerCommand;
import com.thestalker.entity.StalkerEntity;
import com.thestalker.events.StalkerScheduler;
import com.thestalker.registry.ModEntities;
import com.thestalker.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;

public class TheStalker implements ModInitializer {
    public static final String MOD_ID = "stalker";
    public static final Logger LOG = LogUtils.getLogger();

    private final StalkerScheduler scheduler = new StalkerScheduler();

    @Override
    public void onInitialize() {
        StalkerConfig.load();
        ModSounds.init();
        ModEntities.init();

        FabricDefaultAttributeRegistry.register(ModEntities.STALKER, StalkerEntity.createAttributes().build());

        ServerLifecycleEvents.SERVER_STARTED.register(scheduler::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> scheduler.onServerStopping());
        ServerTickEvents.END_SERVER_TICK.register(scheduler::onServerTick);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                StalkerCommand.register(dispatcher, scheduler));
    }
}
