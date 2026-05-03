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

# [Android] `ImageStorageService.import` — copy URI to internal storage (no compression yet)

**Lane:** Android
**PRD section:** Decision D4 (app internal storage)
**API contract section:** N/A

## Why

Both gallery (#08) and camera (#13) flows need a single, testable place to copy image bytes from a content URI into app internal storage. This issue lays the interface and the simplest possible impl. Compression is added in #05.

## Implementation steps

1. Create `app/src/main/java/com/mindnote/core/storage/ImageStorageService.kt` with:
   - `interface ImageStorageService { suspend fun import(uri: Uri): ImportResult; suspend fun delete(path: String) }`
   - `data class ImportResult(val path: String, val wasResized: Boolean)`
2. Create `ImageStorageServiceImpl(private val context: Context)` in the same file or sibling.
3. Implement `import`:
   - Open `context.contentResolver.openInputStream(uri)`.
   - Generate a unique filename: `images/${UUID.randomUUID()}.jpg`.
   - Ensure `context.filesDir.resolve("images").mkdirs()`.
   - Stream bytes to the destination file.
   - Return `ImportResult(file.absolutePath, wasResized = false)`.
4. Stub `delete` to `TODO()` — implemented in #06.
5. Wire the service into the existing DI module (Hilt or whatever `core/di/` uses).
6. Add unit test in `app/src/test/.../ImageStorageServiceTest.kt`:
   - Use a fake `ContentResolver` returning a known byte stream.
   - Assert `import(uri).path` exists, file contents match input, `wasResized == false`.

## Files to touch

- `app/src/main/java/com/mindnote/core/storage/ImageStorageService.kt` — create
- `app/src/main/java/com/mindnote/core/di/...` — modify (add binding)
- `app/src/test/java/com/mindnote/core/storage/ImageStorageServiceTest.kt` — create

## Acceptance criteria

- [ ] Calling `import` with a content URI copies bytes to `filesDir/images/...` and returns the path.
- [ ] Returned file exists on disk and contents byte-equal the source stream.
- [ ] Unit test `import_copiesBytesToInternalStorage` passes.
- [ ] All file I/O runs off the main thread (`suspend` + `Dispatchers.IO`).

## Blocked by

Nothing — independently grabbable.

## Notes

- Don't add compression here. That's #05 and will keep this PR small.
- Inspect `core/di/` to match the project's DI style; don't introduce a new framework.
