---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 0
estimate: 30m
blocked-by: []
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/0
---

# [Android] Extend `Note` domain model with `images: List<String>`

**Lane:** Android
**PRD section:** Constraints — "`Note` domain model has no image field today"
**API contract section:** N/A

## Why

Every other issue in this feature (attach, display, remove) reads or writes `Note.images`. Adding the field to the domain model first, with a safe default, lets every other issue compile against it.

## Implementation steps

1. `app/src/main/java/com/mindnote/domain/model/Note.kt` — add `val images: List<String> = emptyList()` to the `Note` data class.
2. Run `./gradlew :app:compileDebugKotlin`. Existing `Note(...)` constructor calls without `images` should still compile thanks to the default value.
3. If any callsite uses positional arguments past the new field's position, fix them to use named arguments. Otherwise leave callers untouched.

## Files to touch

- `app/src/main/java/com/mindnote/domain/model/Note.kt` — modify
- Any file flagged by the compiler with positional-argument breakage — modify

## Acceptance criteria

- [ ] `Note.images: List<String>` exists and defaults to empty.
- [ ] `./gradlew :app:compileDebugKotlin` succeeds with no new warnings.
- [ ] No existing test fails.

## Blocked by

Nothing — independently grabbable.

## Notes

- `String` (file path) is intentional; richer typing (e.g., `Uri`) would force consumers to depend on `android.net`. Keep the domain layer pure.
- Persistence layer support comes in issue #03.
