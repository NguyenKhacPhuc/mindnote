---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 3
estimate: 45m
blocked-by:
  - "[[14-render-images-in-note-detail]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/3
---

# [Android] Missing-file placeholder on broken image paths

**Lane:** Android
**PRD section:** Story 3 acceptance — "missing/corrupt image file shows a placeholder (no crash)"
**API contract section:** N/A

## Why

Files can disappear: user clears app data partially, restored from backup, race with delete. Without a placeholder, Coil returns a transparent box and the layout silently breaks. With one, the user understands the image is gone.

## Implementation steps

1. Add a vector drawable `app/src/main/res/drawable/ic_image_broken.xml` (a simple "broken image" glyph; reuse Material Icons if a project convention exists in `design/`).
2. In `NoteDetailScreen.kt` (from #14), update each `AsyncImage`:
   ```kotlin
   AsyncImage(
     model = File(path),
     contentDescription = null,
     placeholder = painterResource(R.drawable.ic_image_broken),
     error = painterResource(R.drawable.ic_image_broken),
     fallback = painterResource(R.drawable.ic_image_broken),
     ...
   )
   ```
3. UI test: render a note with one valid image path and one bogus path. Assert both image slots are present (no crash); the error painter is shown for the bogus one.

## Files to touch

- `app/src/main/res/drawable/ic_image_broken.xml` — create
- `app/src/main/java/com/mindnote/features/notedetail/NoteDetailScreen.kt` — modify
- UI test under `app/src/androidTest/...` — create or modify

## Acceptance criteria

- [ ] Bogus path → placeholder drawable is rendered, no crash.
- [ ] Valid path → image is rendered (placeholder may briefly flash; acceptable).
- [ ] UI test `noteDetail_missingImage_showsPlaceholder` passes.

## Blocked by

- #14 — we need image rendering to attach the placeholder to.

## Notes

- Don't add retry / refetch logic. If the file's gone, it's gone.
- Don't auto-clean the dead path from `note.images` here. That's a separate cleanup concern outside v1.
