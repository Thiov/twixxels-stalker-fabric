# Twixxel's Stalker — Fabric 26.1.2 port

A Fabric port of **[Twixxel's Stalker](https://www.curseforge.com/minecraft/mc-mods/twixxels-stalker)** by **MrPerkasc** (original idea by Twixxel_lesgo, designs by LCrandom), brought from Forge 1.20.1 to **Fabric on Minecraft 26.1.2**.

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

## Requirements

- Minecraft **26.1.2**
- **Fabric Loader** ≥ 0.19.2
- **Fabric API** ≥ 0.146.1
- **Java 25**

## Building

There is no committed Gradle wrapper distribution; the included `gradlew` will download Gradle 9.4
on first run. Build with **JDK 25**:

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

The mod jar is produced in `build/libs/`.

## Credits & license

- Original mod and assets: **MrPerkasc** / Twixxel_lesgo / LCrandom.
- Fabric 1.26 port: **Thiov**.

Licensed under the **MIT License** (matching the original). See [LICENSE](LICENSE).

This is an unofficial, fan-made port for personal use and is not affiliated with the original author.
