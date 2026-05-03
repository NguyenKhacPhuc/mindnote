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

# [Android] Trash icon overlay on each image tile in note detail

**Lane:** Android
**PRD section:** Story 4 — Remove an attached image; Decision D6
**API contract section:** N/A

## Why

Users need a visible affordance to remove an image. Per D6, the chosen pattern is a trash icon overlaid on each tile. This issue adds the UI; #17 wires the tap.

## Implementation steps

1. In `NoteDetailScreen.kt`, wrap each `AsyncImage` (from #14) in a `Box`:
   ```kotlin
   Box(modifier = Modifier.fillMaxWidth()) {
     AsyncImage(...)
     IconButton(
       onClick = { /* TODO in #17 */ },
       modifier = Modifier
         .align(Alignment.TopEnd)
         .padding(8.dp)
         .size(48.dp)              // hit target ≥48dp per a11y
         .background(scrim, CircleShape),
     ) {
       Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_image))
     }
   }
   ```
2. Add `<string name="remove_image">Remove image</string>` to `app/src/main/res/values/strings.xml`.
3. UI test: render a note with two images; assert two `IconButton`s with the "Remove image" content description are present.

## Files to touch

- `app/src/main/java/com/mindnote/features/notedetail/NoteDetailScreen.kt` — modify
- `app/src/main/res/values/strings.xml` — modify
- UI test — create or modify

## Acceptance criteria

- [ ] Each image tile has a visible trash icon overlay at top-right.
- [ ] The icon's hit target is ≥48dp.
- [ ] The icon has a content description for accessibility ("Remove image").
- [ ] Scrim/background ensures the icon is visible against bright images.
- [ ] No tap handler yet (or stubbed) — tapping does nothing in this PR. Wiring is #17.

## Blocked by

- #14 — needs image rendering to overlay onto.

## Notes

- Use the project's existing icon system if one exists (`design/`); fall back to `Icons.Default.Delete` from `material-icons-extended`.
- Resist the urge to add a confirmation dialog. Open question Q-extra; default is no confirmation.
