# Twixxel's Stalker — Fabric 26.1

Self-contained Fabric build of **[Twixxel's Stalker](https://www.curseforge.com/minecraft/mc-mods/twixxels-stalker)** (original by **MrPerkasc**, idea by Twixxel_lesgo, designs by LCrandom) for **Minecraft 26.1**. See the [main README](../README.md) for the feature list, the other supported versions and the sibling NeoForge/Forge project.

Same code era as 26.2 / 26.1.2; `LivingEntity.knockback(double, double, double)` is still the 3-argument form (26.2 reworked it).

## Requirements

- Minecraft **26.1**
- **Fabric Loader** ≥ 0.19.2
- **Fabric API** ≥ 0.145.1+26.1
- **Java 25**

## Building

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build   # Gradle 9.7.1 is downloaded by the wrapper
# Output: build/libs/twixxels_stalker-1.0.0-fabric-26.1.jar
```

Gradle itself must run on JDK 25 (the 1.21.x / 1.20.1 targets compile with `--release 25`).

## Credits & license

Original mod and assets: **MrPerkasc** / Twixxel_lesgo / LCrandom. Fabric port: **Thiov**. MIT License, see [LICENSE](LICENSE). Unofficial fan port, not affiliated with the original author.
