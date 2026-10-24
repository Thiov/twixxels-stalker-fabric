# Twixxel's Stalker — Fabric 1.20.1

Self-contained Fabric build of **[Twixxel's Stalker](https://www.curseforge.com/minecraft/mc-mods/twixxels-stalker)** (original by **MrPerkasc**, idea by Twixxel_lesgo, designs by LCrandom) for **Minecraft 1.20.1**. See the [main README](../README.md) for the feature list, the other supported versions and the sibling NeoForge/Forge project.

Official Mojang mappings (Loom 1.13.6). No render states or submit pipeline on 1.20.1: the billboard is drawn straight from `EntityRenderer.render`.

## Requirements

- Minecraft **1.20.1**
- **Fabric Loader** ≥ 0.15.0
- **Fabric API** ≥ 0.92.0+1.20.1
- **Java 17**

## Building

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build   # Gradle 9.7.1 is downloaded by the wrapper
# Output: build/libs/twixxels_stalker-1.0.0-fabric-1.20.1.jar
```

Gradle itself must run on JDK 25 (the 1.21.x / 1.20.1 targets compile with `--release 17`).

## Credits & license

Original mod and assets: **MrPerkasc** / Twixxel_lesgo / LCrandom. Fabric port: **Thiov**. MIT License, see [LICENSE](LICENSE). Unofficial fan port, not affiliated with the original author.
