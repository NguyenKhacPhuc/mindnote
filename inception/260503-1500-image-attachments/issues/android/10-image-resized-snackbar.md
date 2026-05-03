---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 2
estimate: 45m
blocked-by:
  - "[[05-add-5mb-downscale-to-import]]"
  - "[[08-plumb-picked-uris-through-storage-service]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/2
---

# [Android] "Image resized" snackbar when downscale occurs

**Lane:** Android
**PRD section:** Story 1 acceptance — "snackbar/toast says 'image resized'"; Decision D3
**API contract section:** N/A

## Why

Per D3, silent downscaling violates user trust. Users need to know an image was modified, even if they don't need to act on it.

## Implementation steps

1. `CaptureContract.kt` — add a one-shot effect type if the project uses MVI effects (e.g., `sealed interface CaptureEffect { object ImageResized : CaptureEffect }`) and a `Channel<CaptureEffect>` in state, OR use the existing effect mechanism in `core/mvi/`.
2. `CaptureViewModel.kt` — after import (added in #08), if any `ImportResult.wasResized == true`, emit `CaptureEffect.ImageResized`. Emit once per import batch, not once per image.
3. `CaptureScreen.kt` — collect effects with `LaunchedEffect`; on `ImageResized`, call `snackbarHostState.showSnackbar("Image resized to fit 5MB limit")`.
4. Unit test: stub service to return one `wasResized = true` and one `wasResized = false`; assert exactly one effect emitted.

## Files to touch

- `app/src/main/java/com/mindnote/features/capture/CaptureContract.kt` — modify
- `app/src/main/java/com/mindnote/features/capture/CaptureViewModel.kt` — modify
- `app/src/main/java/com/mindnote/features/capture/CaptureScreen.kt` — modify
- `app/src/test/java/com/mindnote/features/capture/CaptureViewModelTest.kt` — modify

## Acceptance criteria

- [ ] When at least one imported image was downscaled, a snackbar appears once after import completes.
- [ ] When no imported image was downscaled, no snackbar.
- [ ] Snackbar message is finalized as a string resource, not hardcoded — `res/values/strings.xml`.
- [ ] Unit test `emitsResizedEffect_whenAnyImportWasResized` passes.

## Blocked by

- #05 (downscale + `wasResized` flag), #08 (import flow).

## Notes

- Inspect `core/mvi/` to match the project's effect-handling style.
- Don't show the snackbar per-image — that becomes spammy when multi-picking.
