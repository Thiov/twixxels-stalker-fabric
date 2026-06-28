package com.thestalker.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent BREAK_EVENT_1 = register("break_event_1");
    public static final SoundEvent BREAK_EVENT_2 = register("break_event_2");
    public static final SoundEvent BREAK_EVENT_3 = register("break_event_3");
    public static final SoundEvent BREAK_EVENT_4 = register("break_event_4");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("stalker", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {}
}
