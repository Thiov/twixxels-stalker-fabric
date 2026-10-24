package com.thestalker.entity;

import com.thestalker.StalkerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Stalker. A silent, invulnerable, camera-facing "image" entity driven entirely by ticks
 * (it has no AI goals). Three behaviours, selected by synched data, are scheduled externally by
 * {@link com.thestalker.events.StalkerScheduler}:
 * <ul>
 *     <li>STALK  - stands still as one of several images, then despawns (or vanishes + Darkness when approached).</li>
 *     <li>BEHIND - a brief jumpscare image spawned just behind a player.</li>
 *     <li>CHASE  - teleports toward its target whenever the target is not looking at it, dealing damage in melee.</li>
 * </ul>
 */
public class StalkerEntity extends PathfinderMob {
    public static final Set<UUID> ALIVE_UUIDS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static final int STALK_LIFETIME_TICKS = 1560;
    public static final int BEHIND_SCARE_TICKS = 100;

    private static final EntityDataAccessor<Byte> IMAGE_INDEX = SynchedEntityData.defineId(StalkerEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> IS_BEHIND_SCARE = SynchedEntityData.defineId(StalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHASE = SynchedEntityData.defineId(StalkerEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private ServerPlayer chaseTarget = null;
    private int timer = 0;
    private int chaseTeleportTimer = 0;

    public StalkerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IMAGE_INDEX, (byte) 1);
        builder.define(IS_BEHIND_SCARE, false);
        builder.define(IS_CHASE, false);
    }

    public int getImageIndex() {
        return this.entityData.get(IMAGE_INDEX);
    }

    public boolean isBehindScare() {
        return this.entityData.get(IS_BEHIND_SCARE);
    }

    public boolean isChase() {
        return this.entityData.get(IS_CHASE);
    }

    public void setupStalk(int imageIndex) {
        this.entityData.set(IMAGE_INDEX, (byte) imageIndex);
        this.entityData.set(IS_BEHIND_SCARE, false);
        this.entityData.set(IS_CHASE, false);
    }

    public void setupBehindScare() {
        this.entityData.set(IMAGE_INDEX, (byte) 0);
        this.entityData.set(IS_BEHIND_SCARE, true);
        this.entityData.set(IS_CHASE, false);
    }

    public void setupChase(ServerPlayer target) {
        this.entityData.set(IMAGE_INDEX, (byte) 5);
        this.entityData.set(IS_BEHIND_SCARE, false);
        this.entityData.set(IS_CHASE, true);
        this.chaseTarget = target;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            ALIVE_UUIDS.add(this.getUUID());
            if (this.entityData.get(IS_CHASE)) {
                this.tickChase();
            } else if (this.entityData.get(IS_BEHIND_SCARE)) {
                this.tickBehindScare();
            } else {
                this.tickStalk();
            }
        }
    }

    private void tickStalk() {
        Player nearby = this.level().getNearestPlayer(this, StalkerConfig.STALK_PROXIMITY_DESPAWN);
        if (nearby != null) {
            nearby.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false), this);
            this.discard();
        } else {
            this.timer++;
            if (this.timer >= STALK_LIFETIME_TICKS) {
                this.discard();
            }
        }
    }

    private void tickBehindScare() {
        this.timer++;
        if (this.timer >= BEHIND_SCARE_TICKS) {
            this.discard();
        }
    }

    private void tickChase() {
        if (this.chaseTarget == null || !this.chaseTarget.isAlive()) {
            this.discard();
            return;
        }

        this.timer++;
        if (this.timer >= StalkerConfig.chaseDurationTicks()) {
            this.discard();
            return;
        }

        double dist = this.distanceTo(this.chaseTarget);
        if (dist <= StalkerConfig.CHASE_ATTACK_RANGE && this.level() instanceof ServerLevel serverLevel) {
            this.chaseTarget.hurtServer(serverLevel, this.damageSources().generic(), (float) StalkerConfig.CHASE_DAMAGE);
        }

        this.chaseTeleportTimer++;
        if (this.chaseTeleportTimer >= StalkerConfig.chaseIntervalTicks()) {
            this.chaseTeleportTimer = 0;
            if (!this.isInPlayerView(this.chaseTarget)) {
                Vec3 toPlayer = this.chaseTarget.position().subtract(this.position()).normalize();
                double newDist = Math.max(0.0, dist - StalkerConfig.CHASE_TELEPORT_STEP);
                Vec3 newPos = this.chaseTarget.position().subtract(toPlayer.scale(newDist));
                this.breakBlocksAlongPath(this.position(), newPos);
                BlockPos groundPos = this.findGround(newPos.x, newPos.y, newPos.z);
                if (groundPos != null) {
                    this.teleportTo(groundPos.getX() + 0.5, groundPos.getY(), groundPos.getZ() + 0.5);
                } else {
                    this.teleportTo(newPos.x, newPos.y, newPos.z);
                }
            }
        }
    }

    /** True when the target can actually see the stalker (within the FOV cone and with unobstructed line of sight). */
    private boolean isInPlayerView(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 entityCenter = this.position().add(0.0, this.getBbHeight() * 0.5, 0.0);
        Vec3 lookVec = player.getLookAngle().normalize();
        Vec3 toEntity = entityCenter.subtract(eyePos).normalize();
        double dot = lookVec.dot(toEntity);
        double angleDeg = Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0, 1.0)));
        if (angleDeg >= StalkerConfig.CHASE_FOV_THRESHOLD) {
            return false;
        }
        BlockHitResult hit = this.level().clip(new ClipContext(eyePos, entityCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double hitDist = hit.getLocation().distanceTo(eyePos);
        double entityDist = entityCenter.distanceTo(eyePos);
        return hitDist >= entityDist - 0.5;
    }

    private void breakBlocksAlongPath(Vec3 from, Vec3 to) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 dir = to.subtract(from);
        double length = dir.length();
        if (length == 0.0) {
            return;
        }
        float renderedHeight = 4.0F;
        float halfW = 2.0F;
        Vec3 step = dir.normalize().scale(0.5);
        int steps = (int) (length / 0.5) + 1;
        for (int i = 0; i <= steps; i++) {
            Vec3 pos = from.add(step.scale(i));
            for (double dy = 0.0; dy <= renderedHeight; dy += 0.5) {
                for (double dx = -halfW; dx <= halfW; dx += 0.5) {
                    for (double dz = -halfW; dz <= halfW; dz += 0.5) {
                        BlockPos bp = new BlockPos(
                                (int) Math.floor(pos.x + dx),
                                (int) Math.floor(pos.y + dy),
                                (int) Math.floor(pos.z + dz));
                        BlockState state = serverLevel.getBlockState(bp);
                        if (!state.isAir() && state.getFluidState().isEmpty()) {
                            serverLevel.destroyBlock(bp, false);
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private BlockPos findGround(double x, double y, double z) {
        BlockPos base = new BlockPos((int) x, (int) y, (int) z);
        for (int dy = 0; dy <= 8; dy++) {
            BlockPos pos = base.below(dy);
            if (!this.level().isEmptyBlock(pos.below()) && this.level().isEmptyBlock(pos) && this.level().isEmptyBlock(pos.above())) {
                return pos;
            }
        }
        for (int dyx = 1; dyx <= 8; dyx++) {
            BlockPos pos = base.above(dyx);
            if (!this.level().isEmptyBlock(pos.below()) && this.level().isEmptyBlock(pos) && this.level().isEmptyBlock(pos.above())) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide()) {
            ALIVE_UUIDS.remove(this.getUUID());
        }
        super.remove(reason);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // immovable
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("ImageIndex", this.entityData.get(IMAGE_INDEX));
        output.putBoolean("IsBehindScare", this.entityData.get(IS_BEHIND_SCARE));
        output.putBoolean("IsChase", this.entityData.get(IS_CHASE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(IMAGE_INDEX, input.getByteOr("ImageIndex", (byte) 1));
        this.entityData.set(IS_BEHIND_SCARE, input.getBooleanOr("IsBehindScare", false));
        this.entityData.set(IS_CHASE, input.getBooleanOr("IsChase", false));
    }
}
