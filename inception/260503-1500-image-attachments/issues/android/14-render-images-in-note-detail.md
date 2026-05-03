---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 2
estimate: 75m
blocked-by:
  - "[[01-add-coil-dependency]]"
  - "[[02-extend-note-model-with-images]]"
  - "[[03-persistence-supports-note-images]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/2
---

# [Android] Render `note.images` in note detail with Coil

**Lane:** Android
**PRD section:** Story 3 — View attached images in a note; Decision D5
**API contract section:** N/A

## Why

Without rendering, the prior import work is invisible to the user. Per D5, images appear in note detail only — not in the list/preview.

## Implementation steps

1. Inspect `app/src/main/java/com/mindnote/features/notedetail/` to find the screen composable (likely `NoteDetailScreen.kt`).
2. Below the existing body content, add a section that renders `note.images` if the list is non-empty:
   ```kotlin
   if (note.images.isNotEmpty()) {
     Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
       note.images.forEach { path ->
         AsyncImage(
           model = File(path),
           contentDescription = null,
           modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
           contentScale = ContentScale.FillWidth,
         )
       }
     }
   }
   ```
3. If `note.images` is empty, no image section renders (no headers, no padding).
4. UI / instrumentation test:
   - Note with two image paths → both `AsyncImage`s present in semantic tree.
   - Note with empty images → no `AsyncImage` in tree.

## Files to touch

- `app/src/main/java/com/mindnote/features/notedetail/NoteDetailScreen.kt` — modify

## Acceptance criteria

- [ ] Note detail with images shows them in a vertical column, in attach order.
- [ ] Note detail without images shows nothing in the image area (no empty container).
- [ ] List/home/preview screens are unchanged — verified by visual diff or a screenshot test on `features/notes/` and `features/home/`.
- [ ] UI test passes.

## Blocked by

- #01 (Coil dep), #02 (Note.images field), #03 (persistence) so saved notes round-trip.

## Notes

- Tap-to-zoom is OUT of scope — basic display only.
- Missing-file placeholder is #15.
- Trash overlay is #16, layered on top of this rendering.
