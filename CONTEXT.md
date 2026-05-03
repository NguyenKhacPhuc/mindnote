---
type: context
project: mindnote
tags:
  - context
  - project/mindnote
---

# CONTEXT

> [!note] **Project-wide.** This file lives at the project root and grows across features. Each Inception run *appends* to it.

Shared language for MindNote.

## Domain terms

| Term | Meaning | Replaces |
|---|---|---|
| Attachment | Any non-text content bundled into a note. Today: image only. | "the image attached to the note", "image on a note" |
| Internal image | Image file stored in the app's private internal storage and referenced by a note. | "the image we saved in our app folder" |
| Auto-downscale | Resizing >5MB images so they fit under the 5MB cap. Always paired with a user-visible "image resized" notice. | "shrink the image so it doesn't exceed the limit" |

## Domain entities (data model)

### Note (extended)

- **What it is:** A user-created text record, optionally with attachments.
- **Identifier:** `id: String`
- **Existing fields:** `title`, `preview`, `body`, `tags`, `date`
- **New field for v1:** `images: List<String>` — list of file paths (or URIs) into app internal storage. Empty list when no attachments.
- **Lifecycle:** Created via capture flow; updated as user edits body/tags/images; deleted removes all referenced internal image files.

### InternalImage (new)

- **What it is:** A single image file owned by MindNote and stored in app internal storage.
- **Identifier:** absolute path on app internal storage, or a generated filename.
- **Key fields:** `path: String`, original mime type, byte size on disk.
- **Lifecycle:** Created on import (from gallery picker or camera intent), possibly downscaled on the way in. Deleted when:
  - User taps trash overlay in note detail, OR
  - The owning note is deleted.
- **Relationships:** Each InternalImage is owned by exactly one Note (no sharing across notes in v1).

## Glossary of process terms

None yet. Add when the team coins terms.
