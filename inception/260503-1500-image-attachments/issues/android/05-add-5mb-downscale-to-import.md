---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 1
estimate: 60m
blocked-by:
  - "[[04-image-storage-service-import-no-compression]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/1
---

# [Android] Add 5MB downscale to `ImageStorageService.import`

**Lane:** Android
**PRD section:** Goal — "Images >5MB are auto-downscaled... user is informed"; Decision D3
**API contract section:** N/A

## Why

Modern phone photos routinely exceed 5MB raw. Without downscaling, students photographing whiteboards will hit storage bloat. Per D3, oversized images must be auto-downscaled and the caller informed (`wasResized = true`) so the UI can show "image resized".

## Implementation steps

1. In `ImageStorageServiceImpl.import`:
   - First, copy bytes to a temp file (existing behavior).
   - If the temp file's size ≤ 5 MB: keep as-is, return `ImportResult(path, wasResized = false)`.
   - If > 5 MB: enter the downscale loop.
2. Downscale loop:
   - Decode bitmap with `BitmapFactory.Options.inSampleSize` starting at 2.
   - Re-encode as JPEG at quality 85 to a new temp file.
   - If still > 5 MB, double `inSampleSize` and retry (max 5 iterations to bound).
   - Write the final bitmap to the destination, delete the original temp.
3. Return `ImportResult(path, wasResized = true)` on the downscale path.
4. Update unit tests in `ImageStorageServiceTest.kt`:
   - `import_smallImage_doesNotResize` — input ≤ 5 MB, assert `wasResized == false`, file size unchanged.
   - `import_largeImage_resizes` — input > 5 MB (use a fixture or generated bitmap), assert `wasResized == true`, output file ≤ 5 MB, file is decodable as a bitmap.

## Files to touch

- `app/src/main/java/com/mindnote/core/storage/ImageStorageService.kt` — modify
- `app/src/test/java/com/mindnote/core/storage/ImageStorageServiceTest.kt` — modify
- `app/src/test/resources/...` — add a small >5MB test fixture if convenient, or generate at runtime

## Acceptance criteria

- [ ] Both new unit tests pass.
- [ ] Output file is always ≤ 5 MB when `wasResized == true`.
- [ ] Output is a decodable JPEG (the test should re-decode and assert dimensions > 0).
- [ ] No main-thread I/O.

## Blocked by

- #04 — needs the import scaffolding.

## Notes

- Using `inSampleSize` keeps memory low (decoder downsamples during decode).
- Quality 85 is the typical sweet spot; don't tune unless tests show a problem.
- HEIC decoding is supported on API 26+; the device decoder handles it transparently via `BitmapFactory`.
