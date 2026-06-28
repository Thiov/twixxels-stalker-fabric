package com.thestalker.events;

import com.thestalker.StalkerConfig;
import com.thestalker.entity.StalkerEntity;
import com.thestalker.registry.ModEntities;
import com.thestalker.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Server-side scheduler that drives all stalker behaviour. Ported from the original mod's Forge
 * {@code StalkerEvents}. Hooked up to Fabric server lifecycle/tick events in {@link com.thestalker.TheStalker}.
 */
public class StalkerScheduler {
    private long serverStartTick = -1L;
    private long lastStalkTick = Long.MIN_VALUE / 2;
    private UUID spawnedStalkUUID = null;
    private boolean stalkActive = false;
    private long lastBehindScareTick = Long.MIN_VALUE / 2;
    private long lastChaseTick = Long.MIN_VALUE / 2;
    private UUID chaseEntityUUID = null;
    private long lastBreakEventTick = Long.MIN_VALUE / 2;

    private boolean isBadWeather(ServerLevel level) {
        return level.isRaining() || level.isThundering();
    }

    private long effectiveCooldown(long base, ServerLevel level) {
        return this.isBadWeather(level) ? base / 2L : base;
    }

    public void onServerStarted(MinecraftServer server) {
        long now = server.getTickCount();
        this.serverStartTick = now;
        this.lastStalkTick = now;
        this.lastBehindScareTick = now;
        this.lastChaseTick = now;
        this.lastBreakEventTick = now;
        this.spawnedStalkUUID = null;
        this.chaseEntityUUID = null;
        this.stalkActive = false;
    }

    public void onServerStopping() {
        this.serverStartTick = -1L;
    }

    public void onServerTick(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null || this.serverStartTick < 0L) {
            return;
        }
        long now = server.getTickCount();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }
        RandomSource random = overworld.getRandom();

        if (now - this.lastBreakEventTick >= StalkerConfig.breakEventIntervalTicks()) {
            this.playBreakEventToAll(players, overworld);
            this.lastBreakEventTick = now;
        }

        if (now - this.lastBehindScareTick >= this.effectiveCooldown(StalkerConfig.behindScareCooldownTicks(), overworld)) {
            ServerPlayer target = this.randomPlayer(players, random);
            if (target != null) {
                this.triggerBehindScare(target, this.getPlayerLevel(target, overworld));
            }
            this.lastBehindScareTick = now;
        }

        if (this.chaseEntityUUID != null) {
            if (!StalkerEntity.ALIVE_UUIDS.contains(this.chaseEntityUUID)) {
                this.chaseEntityUUID = null;
                this.lastChaseTick = now;
            }
        } else if (now - this.lastChaseTick >= this.effectiveCooldown(StalkerConfig.chaseCooldownTicks(), overworld)) {
            ServerPlayer target = this.randomPlayer(players, random);
            if (target != null) {
                UUID uuid = this.triggerChase(target, this.getPlayerLevel(target, overworld));
                if (uuid != null) {
                    this.chaseEntityUUID = uuid;
                }
            }
            this.lastChaseTick = now;
        }

        if (this.stalkActive && this.spawnedStalkUUID != null) {
            if (!StalkerEntity.ALIVE_UUIDS.contains(this.spawnedStalkUUID)) {
                this.spawnedStalkUUID = null;
                this.stalkActive = false;
                this.lastStalkTick = now;
            }
        } else if (this.stalkActive) {
            this.stalkActive = false;
            this.lastStalkTick = now;
        } else if (now - this.lastStalkTick >= this.effectiveCooldown(StalkerConfig.stalkRollIntervalTicks(), overworld)) {
            if (random.nextBoolean()) {
                ServerPlayer target = this.randomPlayer(players, random);
                if (target != null) {
                    double distance = random.nextBoolean() ? StalkerConfig.DISTANT_STALK_DISTANCE : StalkerConfig.CLOSE_STALK_DISTANCE;
                    UUID uuid = this.spawnStalk(target, this.getPlayerLevel(target, overworld), distance);
                    if (uuid != null) {
                        this.spawnedStalkUUID = uuid;
                        this.stalkActive = true;
                    }
                }
            }
            this.lastStalkTick = now;
        }
    }

    public void triggerDistantStalk(ServerPlayer player, ServerLevel level) {
        this.spawnStalk(player, level, StalkerConfig.DISTANT_STALK_DISTANCE);
    }

    public void triggerCloseStalk(ServerPlayer player, ServerLevel level) {
        this.spawnStalk(player, level, StalkerConfig.CLOSE_STALK_DISTANCE);
    }

    public void triggerBehindScare(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle();
        double spawnX = player.getX() - look.x;
        double spawnZ = player.getZ() - look.z;
        StalkerEntity stalker = ModEntities.STALKER.create(level, EntitySpawnReason.EVENT);
        if (stalker != null) {
            stalker.setupBehindScare();
            stalker.snapTo(spawnX, player.getY(), spawnZ, level.getRandom().nextFloat() * 360.0F, 0.0F);
            BlockPos pos = BlockPos.containing(spawnX, player.getY(), spawnZ);
            stalker.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
            level.addFreshEntity(stalker);
            StalkerEntity.ALIVE_UUIDS.add(stalker.getUUID());
        }
    }

    public UUID triggerChase(ServerPlayer player, ServerLevel level) {
        double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
        double dist = StalkerConfig.CHASE_SPAWN_DISTANCE;
        double spawnX = player.getX() + Math.cos(angle) * dist;
        double spawnZ = player.getZ() + Math.sin(angle) * dist;
        BlockPos pos = this.findGround(level, spawnX, player.getY(), spawnZ);
        if (pos == null) {
            return null;
        }
        StalkerEntity stalker = ModEntities.STALKER.create(level, EntitySpawnReason.EVENT);
        if (stalker == null) {
            return null;
        }
        stalker.setupChase(player);
        stalker.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
        stalker.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
        level.addFreshEntity(stalker);
        StalkerEntity.ALIVE_UUIDS.add(stalker.getUUID());
        return stalker.getUUID();
    }

    public void triggerBreakEvent(ServerPlayer player, ServerLevel level) {
        SoundEvent chosen = randomBreakSound(level.getRandom());
        level.playSound(null, player.getX(), player.getY(), player.getZ(), chosen, SoundSource.AMBIENT, 1.0F, 1.0F);
    }

    private void playBreakEventToAll(List<ServerPlayer> players, ServerLevel overworld) {
        SoundEvent chosen = randomBreakSound(overworld.getRandom());
        for (ServerPlayer player : players) {
            ServerLevel level = this.getPlayerLevel(player, overworld);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), chosen, SoundSource.AMBIENT, 1.0F, 1.0F);
        }
    }

    private static SoundEvent randomBreakSound(RandomSource random) {
        SoundEvent[] options = new SoundEvent[]{
                ModSounds.BREAK_EVENT_1, ModSounds.BREAK_EVENT_2, ModSounds.BREAK_EVENT_3, ModSounds.BREAK_EVENT_4
        };
        return options[random.nextInt(options.length)];
    }

    private ServerPlayer randomPlayer(List<ServerPlayer> players, RandomSource random) {
        return players.isEmpty() ? null : players.get(random.nextInt(players.size()));
    }

    private ServerLevel getPlayerLevel(ServerPlayer player, ServerLevel fallback) {
        return player.level() instanceof ServerLevel sl ? sl : fallback;
    }

    private UUID spawnStalk(ServerPlayer player, ServerLevel level, double distance) {
        double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
        double spawnX = player.getX() + Math.cos(angle) * distance;
        double spawnZ = player.getZ() + Math.sin(angle) * distance;
        level.getChunk((int) spawnX >> 4, (int) spawnZ >> 4);
        BlockPos pos = this.findGround(level, spawnX, player.getY(), spawnZ);
        double spawnY = pos != null ? pos.getY() : player.getY();
        double finalX = pos != null ? pos.getX() + 0.5 : spawnX;
        double finalZ = pos != null ? pos.getZ() + 0.5 : spawnZ;
        StalkerEntity stalker = ModEntities.STALKER.create(level, EntitySpawnReason.EVENT);
        if (stalker == null) {
            return null;
        }
        stalker.setupStalk(level.getRandom().nextInt(6) + 1);
        stalker.snapTo(finalX, spawnY, finalZ, level.getRandom().nextFloat() * 360.0F, 0.0F);
        BlockPos finalPos = BlockPos.containing(finalX, spawnY, finalZ);
        stalker.finalizeSpawn(level, level.getCurrentDifficultyAt(finalPos), EntitySpawnReason.EVENT, null);
        level.addFreshEntity(stalker);
        StalkerEntity.ALIVE_UUIDS.add(stalker.getUUID());
        return stalker.getUUID();
    }

    private BlockPos findGround(ServerLevel level, double x, double startY, double z) {
        BlockPos base = BlockPos.containing(x, startY, z);
        for (int dy = 0; dy <= 16; dy++) {
            BlockPos pos = base.below(dy);
            if (!level.isEmptyBlock(pos.below()) && level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())) {
                return pos;
            }
        }
        for (int dyx = 1; dyx <= 16; dyx++) {
            BlockPos pos = base.above(dyx);
            if (!level.isEmptyBlock(pos.below()) && level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())) {
                return pos;
            }
        }
        return null;
    }
}
