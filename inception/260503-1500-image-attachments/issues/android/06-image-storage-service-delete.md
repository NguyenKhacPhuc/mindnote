---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 1
estimate: 20m
blocked-by:
  - "[[04-image-storage-service-import-no-compression]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/1
---

# [Android] `ImageStorageService.delete` — idempotent file removal

**Lane:** Android
**PRD section:** Story 4 (foundation), Decision D4
**API contract section:** N/A

## Why

Issue #17 (trash icon wiring) needs to remove the underlying image file when a user deletes an attachment. Idempotency matters: a missing file should not crash the delete path (e.g., user removes an image, app restarts mid-flow).

## Implementation steps

1. In `ImageStorageServiceImpl.delete(path)`:
   - Construct `File(path)`.
   - If the file exists, call `file.delete()`.
   - If it doesn't exist, return without error.
   - Run on `Dispatchers.IO`.
2. Add unit tests in `ImageStorageServiceTest.kt`:
   - `delete_existingFile_removesIt` — create file, call delete, assert file no longer exists.
   - `delete_missingFile_isNoOp` — call delete with a path that doesn't exist; no exception thrown.

## Files to touch

- `app/src/main/java/com/mindnote/core/storage/ImageStorageService.kt` — modify
- `app/src/test/java/com/mindnote/core/storage/ImageStorageServiceTest.kt` — modify

## Acceptance criteria

- [ ] Both unit tests pass.
- [ ] No exception is thrown for a missing path.
- [ ] No main-thread I/O.

## Blocked by

- #04 — needs the service scaffolding.

## Notes

- Resist the urge to add path-validation guards. Delete is a leaf op; let it be simple.
