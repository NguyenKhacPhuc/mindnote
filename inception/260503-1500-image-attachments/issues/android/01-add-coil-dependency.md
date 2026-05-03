---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 0
estimate: 15m
blocked-by: []
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/0
---

# [Android] Add Coil dependency

**Lane:** Android
**PRD section:** Constraints — "No Coil today; adding it as a dependency is part of foundation."
**API contract section:** N/A

## Why

Coil is the Compose-standard image loader. Issue #14 (display in detail) needs it. Pulling the dependency in as its own atomic change keeps the diff readable and makes a build break here easy to revert.

## Implementation steps

1. `gradle/libs.versions.toml` — add `coil` version (latest stable Compose-compatible release) and a `coil-compose` library entry under `[libraries]`.
2. `app/build.gradle.kts` — add `implementation(libs.coil.compose)` to `dependencies`.
3. Run `./gradlew :app:assembleDebug` to verify the dependency resolves.

## Files to touch

- `gradle/libs.versions.toml` — modify
- `app/build.gradle.kts` — modify

## Acceptance criteria

- [ ] `./gradlew :app:assembleDebug` succeeds.
- [ ] In any Kotlin file under `:app`, `import coil.compose.AsyncImage` resolves without IDE error.
- [ ] No other behavior changes.

## Blocked by

Nothing — independently grabbable.

## Notes

- Use the version catalog (`libs.versions.toml`) — root `build.gradle.kts` shows the project already uses `libs.plugins.*` aliases.
- Don't import or use Coil yet; that's issue #14.
