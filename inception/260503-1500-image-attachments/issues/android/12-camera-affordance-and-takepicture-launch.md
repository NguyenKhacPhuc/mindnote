---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 1
estimate: 60m
blocked-by:
  - "[[11-configure-fileprovider]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/1
---

# [Android] Camera affordance + `TakePicture` intent launch

**Lane:** Android
**PRD section:** Story 2 — Capture image via system camera
**API contract section:** N/A

## Why

Entry point for the camera flow. System camera intent (per D2) is the simplest path. This issue adds the button and the launch; #13 wires the result through `ImageStorageService`.

## Implementation steps

1. `CaptureContract.kt`:
   - Add `data class CameraCaptured(val uri: Uri) : CaptureIntent`.
2. `CaptureScreen.kt`:
   - Add a "Take photo" affordance next to the gallery one (#07).
   - Hold a `var pendingCameraUri: Uri? by remember { mutableStateOf(null) }`.
   - `val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success) pendingCameraUri?.let { viewModel.onIntent(CameraCaptured(it)) }; pendingCameraUri = null }`.
   - On affordance tap: `pendingCameraUri = imageStorageService.newCameraOutputUri(); launcher.launch(pendingCameraUri!!)`. (The helper from #11 should be exposed via DI or a small composable utility; do NOT inject the service into the composable directly — pass a callback from the ViewModel that produces a URI.)
3. `CaptureViewModel.kt`:
   - Stub handler for `CameraCaptured(uri)` — temporarily store the URI in state. (Real plumbing in #13.)

## Files to touch

- `app/src/main/java/com/mindnote/features/capture/CaptureContract.kt` — modify
- `app/src/main/java/com/mindnote/features/capture/CaptureScreen.kt` — modify
- `app/src/main/java/com/mindnote/features/capture/CaptureViewModel.kt` — modify

## Acceptance criteria

- [ ] "Take photo" affordance is visible next to "Add image".
- [ ] Tap launches the system camera with a writable FileProvider URI.
- [ ] On capture, `CameraCaptured(uri)` reaches `CaptureViewModel`.
- [ ] On cancel, no intent is dispatched, no state change.
- [ ] Unit test: `viewModel.onIntent(CameraCaptured(uri))` updates state with that URI.

## Blocked by

- #11 (FileProvider config + `newCameraOutputUri` helper).

## Notes

- Don't add `android.permission.CAMERA` — `TakePicture` invokes the camera app, which holds the permission itself.
- Keep the URI generation outside the composable's recomposition path; generate per-tap.
