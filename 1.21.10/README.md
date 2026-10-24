# Twixxel's Stalker — Fabric 1.21.10

Self-contained Fabric build of **[Twixxel's Stalker](https://www.curseforge.com/minecraft/mc-mods/twixxels-stalker)** (original by **MrPerkasc**, idea by Twixxel_lesgo, designs by LCrandom) for **Minecraft 1.21.10**. See the [main README](../README.md) for the feature list, the other supported versions and the sibling NeoForge/Forge project.

Official Mojang mappings (Loom 1.15.5). Uses `ResourceLocation` (renamed `Identifier` in 1.21.11) and `RenderType` (`RenderTypes` in 1.21.11).

## Requirements

- Minecraft **1.21.10**
- **Fabric Loader** ≥ 0.16.0
- **Fabric API** ≥ 0.138.0+1.21.10
- **Java 21**

## Building

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build   # Gradle 9.7.1 is downloaded by the wrapper
# Output: build/libs/twixxels_stalker-1.0.0-fabric-1.21.10.jar
```

Gradle itself must run on JDK 25 (the 1.21.x / 1.20.1 targets compile with `--release 21`).

## Credits & license

Original mod and assets: **MrPerkasc** / Twixxel_lesgo / LCrandom. Fabric port: **Thiov**. MIT License, see [LICENSE](LICENSE). Unofficial fan port, not affiliated with the original author.
