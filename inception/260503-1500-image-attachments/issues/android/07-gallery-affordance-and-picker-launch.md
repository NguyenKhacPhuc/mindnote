---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 0
estimate: 60m
blocked-by: []
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/0
---

# [Android] Gallery picker affordance + launch — capture URIs into ViewModel state

**Lane:** Android
**PRD section:** Story 1 — Attach image from gallery
**API contract section:** N/A

## Why

User-visible entry point for the gallery flow. This issue adds the button + wires `PickMultipleVisualMedia`. It does NOT yet plumb the URIs through `ImageStorageService` — that's #08, kept separate so this PR is purely a UI + intent-plumbing change.

## Implementation steps

1. `app/src/main/java/com/mindnote/features/capture/CaptureContract.kt`:
   - Add `data class PickedImageUris(val uris: List<Uri>) : CaptureIntent` (or whatever the existing intent sealed type is).
   - Optionally add a state field to surface picked URIs for now (will be removed in #08 once import is wired).
2. `app/src/main/java/com/mindnote/features/capture/CaptureScreen.kt`:
   - Add an "Add image" affordance (icon button) in the capture UI.
   - Use `rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems = Int.MAX_VALUE)) { uris -> viewModel.onIntent(PickedImageUris(uris)) }`.
   - On affordance tap: `launcher.launch(PickVisualMediaRequest(ImageOnly))`.
3. `CaptureViewModel.kt`:
   - Handle `PickedImageUris`: temporarily store `uris` in state. (Will be replaced by import call in #08.)

## Files to touch

- `app/src/main/java/com/mindnote/features/capture/CaptureContract.kt` — modify
- `app/src/main/java/com/mindnote/features/capture/CaptureScreen.kt` — modify
- `app/src/main/java/com/mindnote/features/capture/CaptureViewModel.kt` — modify

## Acceptance criteria

- [ ] On the capture screen, an "Add image" affordance is visible.
- [ ] Tapping it opens the system photo picker.
- [ ] Selecting one or more images closes the picker and results in `CaptureViewModel.state` containing the picked URIs.
- [ ] Cancelling the picker leaves state unchanged.
- [ ] Unit test: `viewModel.onIntent(PickedImageUris(listOf(uriA, uriB)))` updates state to contain those two URIs.
- [ ] Build still compiles.

## Blocked by

Nothing — independently grabbable.

## Notes

- `PickMultipleVisualMedia` is the modern photo picker, back-compat to API 19 via the AndroidX shim. No `READ_MEDIA_IMAGES` permission needed.
- Don't call `ImageStorageService` here — that's #08. Keep this PR strictly UI + intent.
