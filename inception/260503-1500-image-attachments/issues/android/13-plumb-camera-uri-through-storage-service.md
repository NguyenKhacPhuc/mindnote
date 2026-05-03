---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 3
estimate: 60m
blocked-by:
  - "[[08-plumb-picked-uris-through-storage-service]]"
  - "[[09-blocking-spinner-during-import]]"
  - "[[10-image-resized-snackbar]]"
  - "[[12-camera-affordance-and-takepicture-launch]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/3
---

# [Android] Plumb camera URI through `ImageStorageService` → update note's `images`

**Lane:** Android
**PRD section:** Story 2 — Capture image via system camera
**API contract section:** N/A

## Why

#12 captures the camera-result URI into transient state. This issue makes it real: the URI flows through `ImageStorageService.import` (reusing all gallery-flow plumbing — spinner, snackbar, append) so the camera path inherits compression and feedback for free.

## Implementation steps

1. `CaptureViewModel.kt`:
   - In `CameraCaptured(uri)` handler, replace the temporary state-store with the same import flow used in #08:
     ```kotlin
     viewModelScope.launch {
       updateState { copy(isImporting = true) }
       try {
         val result = imageStorageService.import(uri)
         updateNoteImages(currentImages + result.path)
         if (result.wasResized) sendEffect(CaptureEffect.ImageResized)
       } finally { updateState { copy(isImporting = false) } }
     }
     ```
   - Remove the temporary URI field added in #12.
2. Refactor: extract the import-and-update logic from #08 and #13 into a single private helper if it's substantively duplicated. Don't over-abstract — only if the duplication exceeds ~10 lines.
3. Unit test in `CaptureViewModelTest.kt`:
   - Stub service. Call `onIntent(CameraCaptured(uri))`. Assert `note.images` contains the imported path.
   - Sub-test with `wasResized = true`: assert exactly one `ImageResized` effect.

## Files to touch

- `app/src/main/java/com/mindnote/features/capture/CaptureViewModel.kt` — modify
- `app/src/main/java/com/mindnote/features/capture/CaptureContract.kt` — modify (remove temp field)
- `app/src/test/java/com/mindnote/features/capture/CaptureViewModelTest.kt` — modify

## Acceptance criteria

- [ ] After camera capture, `state.note.images` contains the imported file path.
- [ ] During import, the spinner from #09 is visible.
- [ ] If the camera image is downscaled, the snackbar from #10 fires once.
- [ ] Unit tests `cameraCapture_isImported` and `cameraCapture_emitsResizedEffectWhenResized` pass.

## Blocked by

- #08 (gallery plumb), #09 (spinner), #10 (snackbar), #12 (camera launch).

## Notes

- Cancellation is already handled by #12 — no intent dispatched on cancel, so this issue doesn't need cancellation logic.
