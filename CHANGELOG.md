# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.1] - 2026-10-11

### Fixed

- Raised the natural-spawn Mixin priority to `1000` for compatibility with Hodgepodge 2.7.196's priority-`900` overwrite.

### Changed

- Added configuration schema version `1`.
- Added automatic normalization of boolean and block-light threshold values.
- Added automatic cleanup of obsolete properties and categories, including nested categories.
- Added downgrade protection: an older mod version never rewrites a configuration created by a newer schema.
- Added tests for configuration upgrades, downgrade safety, light-level boundaries, weather, and NEI F7 behavior.

## [1.0.0] - 2026-10-11

### Added

- Deterministic block-light threshold for ordinary natural `EntityMob` spawning.
- Preservation of vanilla skylight, day/night, and thunder checks.
- Optional matching NEI F7 overlay on clients.
- Server-optional client installation through `acceptableRemoteVersions = "*"`.
