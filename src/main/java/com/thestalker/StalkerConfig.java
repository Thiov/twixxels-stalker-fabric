package com.thestalker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON-file config (Fabric has no ForgeConfigSpec). Mirrors the original mod's options and defaults.
 * Minute/second values are stored as configured and converted to ticks via the helper methods.
 */
public class StalkerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("twixxels_stalker.json");

    // stalk
    public static int STALK_ROLL_INTERVAL_MINUTES = 15;
    public static double DISTANT_STALK_DISTANCE = 30.0;
    public static double CLOSE_STALK_DISTANCE = 10.0;
    public static double STALK_PROXIMITY_DESPAWN = 3.0;

    // behind scare
    public static int BEHIND_SCARE_COOLDOWN_MINUTES = 25;

    // chase
    public static int CHASE_COOLDOWN_MINUTES = 36;
    public static int CHASE_DURATION_SECONDS = 60;
    public static double CHASE_SPAWN_DISTANCE = 20.0;
    public static double CHASE_TELEPORT_STEP = 10.0;
    public static int CHASE_TELEPORT_INTERVAL_SECONDS = 2;
    public static double CHASE_ATTACK_RANGE = 2.0;
    public static double CHASE_DAMAGE = 10.0;
    public static double CHASE_FOV_THRESHOLD = 60.0;

    // break events
    public static int BREAK_EVENT_INTERVAL_MINUTES = 10;

    public static long stalkRollIntervalTicks() {
        return (long) STALK_ROLL_INTERVAL_MINUTES * 60L * 20L;
    }

    public static long behindScareCooldownTicks() {
        return (long) BEHIND_SCARE_COOLDOWN_MINUTES * 60L * 20L;
    }

    public static long chaseCooldownTicks() {
        return (long) CHASE_COOLDOWN_MINUTES * 60L * 20L;
    }

    public static long chaseDurationTicks() {
        return (long) CHASE_DURATION_SECONDS * 20L;
    }

    public static long chaseIntervalTicks() {
        return (long) CHASE_TELEPORT_INTERVAL_SECONDS * 20L;
    }

    public static long breakEventIntervalTicks() {
        return (long) BREAK_EVENT_INTERVAL_MINUTES * 60L * 20L;
    }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                save();
                return;
            }
            JsonObject root = JsonParser.parseString(Files.readString(CONFIG_PATH)).getAsJsonObject();
            STALK_ROLL_INTERVAL_MINUTES = getInt(root, "stalk_roll_interval_minutes", STALK_ROLL_INTERVAL_MINUTES);
            DISTANT_STALK_DISTANCE = getDouble(root, "distant_stalk_distance", DISTANT_STALK_DISTANCE);
            CLOSE_STALK_DISTANCE = getDouble(root, "close_stalk_distance", CLOSE_STALK_DISTANCE);
            STALK_PROXIMITY_DESPAWN = getDouble(root, "stalk_proximity_despawn_distance", STALK_PROXIMITY_DESPAWN);
            BEHIND_SCARE_COOLDOWN_MINUTES = getInt(root, "behind_scare_cooldown_minutes", BEHIND_SCARE_COOLDOWN_MINUTES);
            CHASE_COOLDOWN_MINUTES = getInt(root, "chase_cooldown_minutes", CHASE_COOLDOWN_MINUTES);
            CHASE_DURATION_SECONDS = getInt(root, "chase_duration_seconds", CHASE_DURATION_SECONDS);
            CHASE_SPAWN_DISTANCE = getDouble(root, "chase_spawn_distance", CHASE_SPAWN_DISTANCE);
            CHASE_TELEPORT_STEP = getDouble(root, "chase_teleport_step_blocks", CHASE_TELEPORT_STEP);
            CHASE_TELEPORT_INTERVAL_SECONDS = getInt(root, "chase_teleport_interval_seconds", CHASE_TELEPORT_INTERVAL_SECONDS);
            CHASE_ATTACK_RANGE = getDouble(root, "chase_attack_range_blocks", CHASE_ATTACK_RANGE);
            CHASE_DAMAGE = getDouble(root, "chase_damage", CHASE_DAMAGE);
            CHASE_FOV_THRESHOLD = getDouble(root, "chase_fov_threshold_degrees", CHASE_FOV_THRESHOLD);
            BREAK_EVENT_INTERVAL_MINUTES = getInt(root, "break_event_interval_minutes", BREAK_EVENT_INTERVAL_MINUTES);
        } catch (IOException e) {
            com.thestalker.TheStalker.LOG.error("Failed to read twixxels_stalker config", e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("stalk_roll_interval_minutes", STALK_ROLL_INTERVAL_MINUTES);
            root.addProperty("distant_stalk_distance", DISTANT_STALK_DISTANCE);
            root.addProperty("close_stalk_distance", CLOSE_STALK_DISTANCE);
            root.addProperty("stalk_proximity_despawn_distance", STALK_PROXIMITY_DESPAWN);
            root.addProperty("behind_scare_cooldown_minutes", BEHIND_SCARE_COOLDOWN_MINUTES);
            root.addProperty("chase_cooldown_minutes", CHASE_COOLDOWN_MINUTES);
            root.addProperty("chase_duration_seconds", CHASE_DURATION_SECONDS);
            root.addProperty("chase_spawn_distance", CHASE_SPAWN_DISTANCE);
            root.addProperty("chase_teleport_step_blocks", CHASE_TELEPORT_STEP);
            root.addProperty("chase_teleport_interval_seconds", CHASE_TELEPORT_INTERVAL_SECONDS);
            root.addProperty("chase_attack_range_blocks", CHASE_ATTACK_RANGE);
            root.addProperty("chase_damage", CHASE_DAMAGE);
            root.addProperty("chase_fov_threshold_degrees", CHASE_FOV_THRESHOLD);
            root.addProperty("break_event_interval_minutes", BREAK_EVENT_INTERVAL_MINUTES);
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(root));
        } catch (IOException e) {
            com.thestalker.TheStalker.LOG.error("Failed to write twixxels_stalker config", e);
        }
    }

    private static int getInt(JsonObject o, String k, int d) {
        JsonElement e = o.get(k);
        return e != null && e.isJsonPrimitive() ? e.getAsInt() : d;
    }

    private static double getDouble(JsonObject o, String k, double d) {
        JsonElement e = o.get(k);
        return e != null && e.isJsonPrimitive() ? e.getAsDouble() : d;
    }
}
