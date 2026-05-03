---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 2
estimate: 45m
blocked-by:
  - "[[08-plumb-picked-uris-through-storage-service]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/2
---

# [Android] Blocking spinner during image import

**Lane:** Android
**PRD section:** Story 1 acceptance — "blocking spinner is shown"; Decision D8
**API contract section:** N/A

## Why

Compression can take a few hundred ms per image. Without an overlay, the user can tap save / navigate away mid-import and end up with a partially-imported note. Per D8, a blocking modal spinner is the chosen UX.

## Implementation steps

1. `CaptureContract.kt` — add `val isImporting: Boolean = false` to `CaptureState`.
2. `CaptureViewModel.kt` — wrap the import work added in #08:
   ```kotlin
   updateState { copy(isImporting = true) }
   try { /* import + update images */ }
   finally { updateState { copy(isImporting = false) } }
   ```
3. `CaptureScreen.kt` — when `state.isImporting`, render a modal `Dialog` (or `Box(Modifier.fillMaxSize().background(scrim)).clickable {}`) containing a `CircularProgressIndicator`. The scrim must intercept input.
4. Unit test in `CaptureViewModelTest.kt`:
   - Use a `TestDispatcher` so import can be paused.
   - Assert `isImporting == true` while suspended, `false` after completion.

## Files to touch

- `app/src/main/java/com/mindnote/features/capture/CaptureContract.kt` — modify
- `app/src/main/java/com/mindnote/features/capture/CaptureViewModel.kt` — modify
- `app/src/main/java/com/mindnote/features/capture/CaptureScreen.kt` — modify
- `app/src/test/java/com/mindnote/features/capture/CaptureViewModelTest.kt` — modify

## Acceptance criteria

- [ ] During import, a modal spinner is visible and intercepts taps.
- [ ] Spinner disappears when import completes.
- [ ] Unit test `isImporting_isTrueDuringImport_falseAfter` passes.
- [ ] Spinner appears even on a fast import (don't add a min-display-time hack — keep it simple).

## Blocked by

- #08 — needs the import flow to be present so we can wrap it.

## Notes

- Reuse this state in #13 (camera flow) — same `isImporting` flag.
