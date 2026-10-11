# ModernMobLighting-GTNH

A small quality-of-life mod for **GT New Horizons 2.9.0-beta-3**, Minecraft 1.7.10, and Forge 10.13.4.1614.

ModernMobLighting applies a deterministic block-light limit to ordinary natural spawning for hostile mobs that use the standard `EntityMob` light check. It preserves the vanilla skylight, day/night, and thunder behavior. When the same JAR is installed on the client, the NEI F7 spawn overlay uses the configured block-light limit as well.

## Features

- Natural hostile spawning uses a configurable saved block-light threshold (`1` by default).
- Vanilla skylight, time-of-day, and weather checks are preserved.
- Spawners, commands, spawn eggs, rituals, scripted spawns, bosses, and structure spawns are not changed.
- The server accepts clients without the mod (`acceptableRemoteVersions = "*"`).
- An optional client installation adjusts the standard NEI F7 overlay.
- Old configuration values are migrated and obsolete entries are removed automatically.
- A newer configuration schema is never overwritten by an older JAR.
- The mod stores no data in worlds, chunks, or NBT.

## Installation

### Server

1. Stop the server.
2. Put the release JAR in the server's `mods` directory.
3. Remove any older copy of the mod. Do not keep two differently named versions in `mods`.
4. Start the server. The configuration file is created automatically at `config/modernmoblighting.cfg`.

The JAR is not required on connecting clients.

### Client (recommended for an accurate F7 overlay)

1. Put the **same JAR** in the client's `mods` directory.
2. Set `maxHostileSpawnBlockLight` to the same value used by the server.

Configuration is not synchronized over the network. Without the client installation, joining still works, but NEI F7 keeps its original `block light < 8` rule.

GT New Horizons 2.9.0-beta-3 already supplies the required Mixin loader through UniMixins. A standalone Forge installation must provide a compatible Mixin loader separately.

## Configuration

`config/modernmoblighting.cfg`:

```cfg
~CONFIG_VERSION: 1

general {
    B:enableModernHostileLighting=true
    I:maxHostileSpawnBlockLight=1
}
```

`maxHostileSpawnBlockLight` is inclusive and is limited to `0..15`. With the default value of `1`, saved block light `0` and `1` pass the artificial-light part of the natural hostile spawn check, while `2` and above fail it.

When `enableModernHostileLighting=false`, the server falls through to the original `EntityMob.isValidLightLevel()` implementation and the client F7 overlay returns to NEI's original `block light < 8` condition.

An example file is available at [`example-config/modernmoblighting.cfg`](example-config/modernmoblighting.cfg).

## Updating and removing the mod

To update, stop the game or server and **replace** the old JAR with the new one. Never leave two versions in `mods` at the same time.

The mod ID and configuration path are stable. The mod has no world data or network protocol, so replacing or removing its JAR does not require save conversion. Forge may log that a world was saved with an older mod version; that message is harmless for this mod.

Configuration maintenance is automatic:

- valid values from an older configuration are preserved;
- integers are normalized to `0..15`, and malformed boolean/integer values return to safe defaults;
- removed properties and categories, including nested categories, are deleted;
- if an older JAR sees a newer schema, it reads known settings but does not rewrite the file or delete future settings;
- if the file has invalid Forge configuration syntax, Forge preserves it as `*.errored` and creates a clean configuration.

Normal updates never require manually deleting stale configuration entries. Maintainers should follow [`docs/RELEASING.md`](docs/RELEASING.md) whenever the configuration schema changes.

## Behavior

### Natural hostile spawning

`MixinSpawnerAnimalsNaturalContext` redirects exactly one call:

```text
SpawnerAnimals.findChunksForSpawning(...)
  -> EntityLiving.getCanSpawnHere()
```

The redirect marks only the entity currently being checked by natural spawning. `MixinEntityMobLightLevel` changes `EntityMob.isValidLightLevel()` only while that exact entity is in this context. Every other call uses the original method.

With the feature enabled, the light check keeps the following conditions:

1. The first vanilla gate: `saved sky light <= random.nextInt(32)`.
2. The time/weather-adjusted skylight gate: `max(0, saved sky light - sky darkening) <= random.nextInt(8)`. Vanilla uses sky darkening `10` during thunder.
3. A deterministic artificial-light gate: `saved block light <= maxHostileSpawnBlockLight`.

This prevents a block-light-zero surface from becoming valid in daylight while still allowing an open surface at night to pass the usual probabilistic skylight checks. The implementation does not hard-code dimension IDs or assume an Overworld provider; it uses the light values supplied by the current world.

The spawning redirect uses Mixin priority `1000`. Hodgepodge 2.7.196 overwrites `SpawnerAnimals.findChunksForSpawning` at priority `900`; the higher priority allows this redirect to wrap the `getCanSpawnHere()` call retained by Hodgepodge. This combination was verified in a full dedicated-server startup.

### NEI F7 overlay

In the tested GTNH release, F7 is implemented by `codechicken.nei.WorldOverlayRenderer#getSpawnMode` from NotEnoughItems 2.8.130-GTNH.

The client-only Mixin changes only NEI's first `8` cutoff:

```text
block light >= 8
```

It becomes the exclusive upper boundary `configured threshold + 1`. Position validity, biome spawn lists, collision/liquid checks, and NEI's standard colors are left intact:

- red: raw sky light is below 8, so the location is dangerous regardless of time;
- yellow: raw sky light is 8 or above, so the location becomes dangerous after sufficient sky darkening;
- no marker: block light is above the configured threshold, or the position is geometrically invalid.

## Explicitly out of scope

The mod does not change:

- vanilla or modded mob spawners;
- Ender IO powered spawners;
- spawn eggs, commands, scripted, ritual, or summon spawns;
- bosses and structure-controlled spawns;
- Forge `CheckSpawn` results set to `ALLOW`, which intentionally bypass `getCanSpawnHere()`;
- modded mobs that completely replace `getCanSpawnHere()` or the light check without calling `EntityMob`;
- NBT, chunks, worlds, or save files.

Removing the JAR restores the standard GTNH behavior.

## Compatibility audit

The following exact versions from GTNH 2.9.0-beta-3 release commit `27e61fbea6e245df80839885460994aa0ef9da9f` were audited:

| Component | Version | Result |
| --- | --- | --- |
| Hodgepodge | 2.7.196 (`9652ffceb2d6c5910beffa4e301b3118fe33c522`) | Its `SpawnerAnimals` overwrite retains `EntityLiving.getCanSpawnHere()`; the priority-1000 redirect applies to the resulting method. |
| CoreTweaks | 0.3.4.7-GTNH (`71f2329a6fed5eea29fce4241b82e1751e6b30e4`) | No Mixin or transformer targets `EntityMob.isValidLightLevel` or the natural-spawn call site. |
| Angelica | 2.2.10 (`61b5301f1af42f2c7dec8266e78e95f66b44d6c8`) | No patch targets these server methods; the client patch changes NEI logic rather than the rendering pipeline. |
| NotEnoughItems | 2.8.130-GTNH (`3f38e6f39bd1e49deef785f78b2daf8b413881a0`) | The F7 class and the order of its block/sky comparisons were confirmed. |
| UniMixins | 0.3.1 | The normal GTNH Mixin config and refmap load successfully. |

Forge's `MobSpawnerBaseLogic` has a separate `EntityLiving.getCanSpawnHere()` call. It is not wrapped in the natural-spawn context and remains unchanged.

## Validation

The automated test suite covers:

- thresholds `0`, `1`, and `15`, including both block-light boundaries;
- threshold clamping to `0..15`;
- open daylight rejection and the vanilla nighttime random gate;
- covered positions and dimensions without skylight;
- thunder sky darkening and the exact inclusive random-gate boundary;
- F7 boundaries and full fallback to the NEI cutoff when the feature is disabled;
- repair of malformed and out-of-range configuration values;
- removal of obsolete properties, categories, and nested categories;
- recognition of a future schema without modifying unknown settings.

Release 1.0.1 was additionally validated with:

- `gradlew test build` (12/12 tests passed);
- production JAR, refmap, and manifest inspection;
- Java 8 bytecode (`major version 52`) on a Java 17 GTNH runtime;
- a clean Forge dedicated server with UniMixins;
- an integration server with the exact Hodgepodge, CoreTweaks, GTNHLib, and UniMixins versions listed above;
- an upgrade from a schema-less 1.0.0-style configuration, preserving user values and removing obsolete entries;
- a downgrade-safety scenario in which a schema-2 configuration remained byte-for-byte unchanged when opened by version 1.0.1.

A manual gameplay pass on a copy of the pack is still recommended for the Overworld, Nether, End, Twilight Forest, and Galacticraft Moon. The code itself contains no dimension IDs, `hasNoSky` checks, or provider-type assumptions.

## Known limitations

- Server configuration is not synchronized to clients. Matching F7 output requires the same threshold on both sides.
- NEI F7 remains a general prediction and cannot model unique restrictions implemented by every mob class.
- Mobs with entirely custom light/spawn checks are intentionally unaffected.

## Building from source

Windows:

```powershell
.\gradlew.bat test build
```

Linux or macOS:

```bash
./gradlew test build
```

The Gradle wrapper is configured to provision its required Java 25 daemon toolchain and compile the mod to Java 8 bytecode. The first build requires internet access to download Gradle, the toolchain, and dependencies.

Release, development, and sources JARs are written to `build/libs`. The installable file is `modernmoblighting-<version>.jar`; do not distribute the `-dev` or `-sources` variants as the gameplay mod.

## License

[MIT](LICENSE)
