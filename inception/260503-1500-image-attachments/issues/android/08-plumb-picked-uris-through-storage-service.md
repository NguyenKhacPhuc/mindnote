---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 1
estimate: 60m
blocked-by:
  - "[[02-extend-note-model-with-images]]"
  - "[[04-image-storage-service-import-no-compression]]"
  - "[[07-gallery-affordance-and-picker-launch]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/1
---

# [Android] Plumb picked URIs through `ImageStorageService` → update note's `images`

**Lane:** Android
**PRD section:** Story 1 — Attach image from gallery
**API contract section:** N/A

## Why

#07 captures URIs into transient state. This issue makes them real attachments: each URI runs through `ImageStorageService.import` and the resulting paths land in `note.images`.

## Implementation steps

1. `CaptureViewModel.kt`:
   - In `PickedImageUris` handler, replace the temporary state-store with: `viewModelScope.launch { val results = uris.map { imageStorageService.import(it) }; updateNoteImages(currentImages + results.map { it.path }) }`.
   - Remove the temporary "picked URIs in state" field added in #07.
2. Make sure `imageStorageService` is injected via the existing DI mechanism.
3. Unit test in `CaptureViewModelTest.kt`:
   - Stub `ImageStorageService` to return predetermined `ImportResult`s.
   - Call `onIntent(PickedImageUris(listOf(uriA, uriB)))`.
   - Assert state's `note.images` contains the two returned paths in order.

## Files to touch

- `app/src/main/java/com/mindnote/features/capture/CaptureViewModel.kt` — modify
- `app/src/main/java/com/mindnote/features/capture/CaptureContract.kt` — modify (remove temp field)
- `app/src/test/java/com/mindnote/features/capture/CaptureViewModelTest.kt` — create or modify

## Acceptance criteria

- [ ] After picking images, `state.note.images` contains the imported file paths.
- [ ] Files exist on disk under `filesDir/images/`.
- [ ] Unit test `pickedImages_areImportedAndAddedToNote` passes.
- [ ] Picking again appends to (not replaces) the existing list.

## Blocked by

- #02 (Note.images field), #04 (ImageStorageService import), #07 (picker wiring).

## Notes

- Don't show the spinner here — #09 handles that.
- Don't show the "image resized" notice — #10 handles that.
