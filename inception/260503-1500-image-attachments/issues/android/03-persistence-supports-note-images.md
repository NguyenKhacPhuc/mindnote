---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 1
estimate: 60m
blocked-by:
  - "[[02-extend-note-model-with-images]]"
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/1
---

# [Android] Persistence layer supports `Note.images`

**Lane:** Android
**PRD section:** Story 1, 2 — "Images persist across app restarts"
**API contract section:** N/A

## Why

Issue #02 added `images` to the in-memory model. Without persistence support, the field resets on app restart and Stories 1/2 fail their "persist" acceptance criterion.

## Implementation steps

1. Inspect `app/src/main/java/com/mindnote/data/db/` to identify the storage tech (Room, DataStore, SQLDelight, JSON file, etc.).
2. Locate the existing Note storage entity / DTO / serialization site.
3. Add an `images` representation:
   - **Room:** add a column with a `List<String>` ↔ JSON `TypeConverter`. Bump DB version. Add `Migration` that adds the column with default `'[]'`.
   - **JSON / DataStore:** add the field to the serializable schema with default empty list; verify backward read of records without the field.
   - **Other:** equivalent migration with empty-list default for existing rows.
4. Update mapper functions between storage and `domain/model/Note`.

## Files to touch

- `app/src/main/java/com/mindnote/data/db/...` — modify (specific files determined in step 1)
- `app/src/main/java/com/mindnote/data/repository/...` — modify mapper(s)
- New migration file if Room

## Acceptance criteria

- [ ] Save a `Note` with `images = listOf("a", "b")`, restart the app, read it back: `images` equals `["a", "b"]`.
- [ ] Existing notes (created before this change) load with `images = emptyList()` — no crash, no NPE.
- [ ] Unit test (or instrumentation test for Room): `noteDao.insert + noteDao.get` round-trips the images list.
- [ ] `./gradlew :app:assembleDebug` succeeds.

## Blocked by

- #02 — needs `Note.images` field in domain model.

## Notes

- The persistence tech is unknown to this issue — first step is discovery.
- For Room, never modify an existing migration; always add a new one and bump the version.
