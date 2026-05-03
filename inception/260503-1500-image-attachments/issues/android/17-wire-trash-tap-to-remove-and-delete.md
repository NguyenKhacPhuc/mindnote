---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 4
estimate: 60m
blocked-by:
  - "[[06-image-storage-service-delete]]"
  - "[[16-trash-icon-overlay-ui]]"
  - "[[03-persistence-supports-note-images]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/4
---

# [Android] Wire trash-tap → remove from note + delete file

**Lane:** Android
**PRD section:** Story 4 — Remove an attached image
**API contract section:** N/A

## Why

#16 added the visual affordance. This issue makes it real: a tap removes the path from `note.images`, persists the note, and deletes the underlying file via `ImageStorageService.delete`.

## Implementation steps

1. Inspect `app/src/main/java/com/mindnote/features/notedetail/` for the existing contract / view model — `NoteDetailContract.kt` / `NoteDetailViewModel.kt`.
2. `NoteDetailContract.kt` — add `data class RemoveImage(val path: String) : NoteDetailIntent`.
3. `NoteDetailViewModel.kt`:
   ```kotlin
   is RemoveImage -> viewModelScope.launch {
     val updated = state.note.copy(images = state.note.images - intent.path)
     noteRepository.save(updated)
     imageStorageService.delete(intent.path)
     updateState { copy(note = updated) }
   }
   ```
4. `NoteDetailScreen.kt` — replace the stub `onClick` from #16 with `viewModel.onIntent(RemoveImage(path))`.
5. Unit test in `NoteDetailViewModelTest.kt`:
   - Stub repository + storage service.
   - Pre-load a note with two images.
   - Dispatch `RemoveImage(pathA)`.
   - Assert: state's `note.images == [pathB]`; repository.save called with the updated note; storage service `.delete(pathA)` called.

## Files to touch

- `app/src/main/java/com/mindnote/features/notedetail/NoteDetailContract.kt` — modify
- `app/src/main/java/com/mindnote/features/notedetail/NoteDetailViewModel.kt` — modify
- `app/src/main/java/com/mindnote/features/notedetail/NoteDetailScreen.kt` — modify
- `app/src/test/java/com/mindnote/features/notedetail/NoteDetailViewModelTest.kt` — create or modify

## Acceptance criteria

- [ ] Tapping the trash icon removes the image from the visible list immediately.
- [ ] The underlying file is deleted from internal storage (verified via storage-service spy in the test).
- [ ] The note is persisted with the updated `images`.
- [ ] Removing one image leaves the others untouched.
- [ ] Removing the last image returns the note to the no-image state (no empty image section per #14).
- [ ] Unit test `removeImage_removesFromListAndDeletesFile` passes.

## Blocked by

- #06 (`ImageStorageService.delete`), #16 (trash icon UI), #03 (persistence).

## Notes

- No confirmation dialog or undo per current decisions. If the mob ratifies Q-extra differently, that becomes a follow-up issue.
- If `noteRepository.save` is async with an existing pattern (e.g., emits via flow), match that — don't introduce a new persistence pattern.
