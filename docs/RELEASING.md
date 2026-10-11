# Release checklist

This checklist keeps JAR updates, configuration migrations, and GitHub releases reproducible.

## 1. Choose the version

Use Semantic Versioning:

- patch: compatible bug fixes and internal changes;
- minor: backward-compatible features or configuration additions;
- major: intentionally incompatible behavior or API changes.

Update both values in `addon.gradle.kts`:

```kotlin
extra["modVersion"] = "x.y.z"
version = "x.y.z"
```

Add the release to `CHANGELOG.md`. Comparison links may be added once the final GitHub repository URL is known.

## 2. Handle configuration changes

The current schema is `ModConfig.CONFIG_SCHEMA_VERSION`.

If a release changes the set or meaning of configuration entries:

1. Increment the numeric schema version.
2. Read and migrate old values before calling `removeUnknownEntries`.
3. Add every retained key to `KNOWN_GENERAL_PROPERTIES`.
4. Keep the future-schema early return. It prevents a downgrade from deleting settings created by a newer JAR.
5. Add upgrade tests from every schema that needs explicit migration.
6. Add a downgrade-safety test proving that the current release does not rewrite the next schema.

Do not reuse a removed key for a different meaning. Keep the mod ID and `modernmoblighting.cfg` path stable.

## 3. Recheck compatibility

For a new GTNH target, verify the exact pack versions of:

- Hodgepodge and its `SpawnerAnimals.findChunksForSpawning` overwrite;
- CoreTweaks;
- Angelica;
- NotEnoughItems and `WorldOverlayRenderer#getSpawnMode`;
- UniMixins/GTNHLib.

Confirm that Hodgepodge still retains the `EntityLiving.getCanSpawnHere()` call and that its Mixin priority remains below this project's priority `1000`. A change to that method or priority requires another full integration test.

## 4. Test the release

Run these as two separate Gradle invocations:

```text
gradlew.bat clean
gradlew.bat test build
```

Do not combine `clean` and `build` in one invocation while `org.gradle.parallel=true`; dependency/setup tasks may recreate files while `clean` is deleting the build directory.

Then verify:

- all unit tests pass;
- `build/libs/modernmoblighting-x.y.z.jar` exists;
- the JAR manifest contains the Mixin attributes;
- the refmap is present;
- class-file major version is `52`;
- a dedicated server reaches `Done` with the target GTNH compatibility mods;
- an old real configuration is migrated correctly;
- a future-schema configuration remains byte-for-byte unchanged;
- the client starts with NEI and F7 reflects thresholds `0`, `1`, and `15`.

For changes to lighting behavior, manually check representative dimensions on a copy of the pack.

## 5. Publish

1. Commit the source and documentation changes.
2. Tag the exact commit as `vX.Y.Z`.
3. Push the commit and tag.
4. Create a GitHub Release from the tag and attach `build/libs/modernmoblighting-x.y.z.jar` from a clean local or CI build.
5. Download the attached JAR and compare it with the clean local build if reproducibility is required.
6. In the release notes, tell users to stop the game/server and replace the old JAR instead of keeping both versions.

Never publish the `-dev` or `-sources` JAR as the gameplay download.
