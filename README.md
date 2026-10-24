# Twixxel's Stalker — Fabric port (26.3, 26.2, 26.1.2, 26.1, 1.21.11, 1.21.10, 1.20.1)

A Fabric port of **[Twixxel's Stalker](https://www.curseforge.com/minecraft/mc-mods/twixxels-stalker)** by **MrPerkasc** (original idea by Twixxel_lesgo, designs by LCrandom), brought from Forge 1.20.1 to **Fabric**. The repository root is the build for the newest Minecraft version, **26.3**.

> **Looking for another Minecraft version?** The [`26.2/`](26.2/), [`26.1.2/`](26.1.2/), [`26.1/`](26.1/), [`1.21.11/`](1.21.11/), [`1.21.10/`](1.21.10/) and [`1.20.1/`](1.20.1/) subdirectories are self-contained Fabric builds for those Minecraft versions (own `build.gradle`, `gradle.properties` and `src/`). Pick the folder that matches your game version.

> **Playing NeoForge or Forge?** See the sibling project [twixxels-stalker-neoforge](https://github.com/Thiov/twixxels-stalker-neoforge).

> *Something is watching you.* The Stalker is a silent hunter — a flat, unsettling figure that
> appears at the edge of your vision, stands motionless while you look, and closes the distance the
> moment you turn away.

## Features (faithful to the original)

- **The Stalker entity** (`stalker:stalker`) — an invulnerable, camera-facing "image" entity.
- **Stalk events** — a random image appears at a distance (or close), stares, then vanishes. Walk
  too close and it disappears, leaving you with **Darkness**.
- **Behind scares** — a brief jumpscare image materialises right behind you.
- **Chases** — a Stalker spawns and relentlessly teleports toward you *only while you aren't looking
  at it* (FOV cone + line-of-sight check), carving through blocks and dealing damage in melee.
- **Ambient "break" sounds** played to all players on a timer.
- **Bad weather** (rain/thunder) halves the event cooldowns.
- **Config file** (`config/twixxels_stalker.json`) exposing all of the original's tunables
  (distances, cooldowns, chase damage/range/FOV, durations, etc.).
- **Command:** `/stalker trigger <close_stalk|distant_stalk|behind_scare|chase|break_event>`
  (requires permission level 2).

## Supported versions

| Minecraft | Folder | Fabric Loader | Fabric API | Java | Jar |
|---|---|---|---|---|---|
| **26.3** | (this folder) | ≥ 0.20.1 | ≥ 0.165.0+26.3 | 25 | `twixxels_stalker-1.0.0-fabric-26.3.jar` |
| **26.2** | [`26.2/`](26.2/) | ≥ 0.19.2 | ≥ 0.153.0+26.2 | 25 | `twixxels_stalker-1.0.0-fabric-26.2.jar` |
| **26.1.2** | [`26.1.2/`](26.1.2/) | ≥ 0.19.2 | ≥ 0.146.1+26.1.2 | 25 | `twixxels_stalker-1.0.0-fabric-26.1.2.jar` |
| **26.1** | [`26.1/`](26.1/) | ≥ 0.19.2 | ≥ 0.145.1+26.1 | 25 | `twixxels_stalker-1.0.0-fabric-26.1.jar` |
| **1.21.11** | [`1.21.11/`](1.21.11/) | ≥ 0.16.0 | ≥ 0.141.0+1.21.11 | 21 | `twixxels_stalker-1.0.0-fabric-1.21.11.jar` |
| **1.21.10** | [`1.21.10/`](1.21.10/) | ≥ 0.16.0 | ≥ 0.138.0+1.21.10 | 21 | `twixxels_stalker-1.0.0-fabric-1.21.10.jar` |
| **1.20.1** | [`1.20.1/`](1.20.1/) | ≥ 0.15.0 | ≥ 0.92.0+1.20.1 | 17 | `twixxels_stalker-1.0.0-fabric-1.20.1.jar` |

Loader and Fabric API are the minimum versions each jar declares (the builds were compiled against the versions in each folder's `gradle.properties`). The jars are pinned to the Minecraft line they were compiled for (`~26.2`, `~26.1`, exactly `1.21.11` / `1.21.10`, `~1.20.1`) because the render and entity APIs differ between them; the 26.3 build accepts `>=26.3`.

## Requirements (this folder, Minecraft 26.3)

- Minecraft **26.3**
- **Fabric Loader** ≥ 0.20.1
- **Fabric API** ≥ 0.165.0+26.3
- **Java 25**

## Building

The included `gradlew` downloads Gradle 9.7.1 on first run. Build with **JDK 25** (it must be the JDK that runs Gradle, also for the 1.21.x and 1.20.1 folders, which compile with `--release 21` / `--release 17`):

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

The mod jar is produced in `build/libs/` (for example `twixxels_stalker-1.0.0-fabric-26.3.jar`).

## Version notes

- **26.x** is not obfuscated and builds with Loom's `net.fabricmc.fabric-loom` plugin. 26.1 / 26.1.2 / 26.2 / 26.3 share the same source apart from API drift: 26.2+ reworked `LivingEntity.knockback` and 26.3 renamed `PoseStack.mulPose(Quaternionf)` to `rotate(...)`.
- **1.21.10 / 1.21.11** use the official Mojang mappings; 1.21.10 still names `ResourceLocation` (renamed `Identifier` in 1.21.11) and `RenderType` (`RenderTypes` in 1.21.11). Before 26.1 the camera render state only exposes a rotation quaternion, so the billboard uses `orientation * Ry(180°)`, which equals the 26.x `Ry(-yaw) * Rx(pitch)`.
- **1.20.1** has no render states / submit pipeline: the billboard is drawn directly from `EntityRenderer.render`, entity data uses `CompoundTag` and `defineSynchedData()` without a builder.
- The 26.1.2 build keeps the original `SubmitNodeBufferSource` render shim; the other 26.x and 1.21.x builds submit the quad straight through `SubmitNodeCollector.submitCustomGeometry`.

## Credits & license

- Original mod and assets: **MrPerkasc** / Twixxel_lesgo / LCrandom.
- Fabric port: **Thiov**.

Licensed under the **MIT License** (matching the original). See [LICENSE](LICENSE).

This is an unofficial, fan-made port for personal use and is not affiliated with the original author.
