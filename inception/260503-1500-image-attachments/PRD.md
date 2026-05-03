---
type: prd
feature: image-attachments
status: draft
created: 2026-05-03
tags:
  - inception/prd
  - feature/image-attachments
  - status/draft
---

# PRD: Image attachments on notes

> [!info] **Status:** Draft / awaiting mob review · **Driver:** (you) · **Last updated:** 2026-05-03
> See [[_index]] for the parallel-work plan and [[open-questions]] for unresolved items.

## One-line intent

Users can attach images (from gallery or camera) to a note; v1 is local-only on Android.

## Problem

Two segments are blocked by text-only notes:

- **Students** photographing whiteboards — they want to keep the visual context with their written notes instead of in a separate camera roll.
- **Casual users** saving screenshots — they want to drop a screenshot into a note alongside text, today they can't.

## Goals

- [ ] User can attach one or more images to a note from the device gallery.
- [ ] User can capture a new image via the system camera and attach it to a note.
- [ ] Images >5MB are auto-downscaled to fit under 5MB; user is informed via a "image resized" message.
- [ ] Note detail view displays all attached images.
- [ ] User can remove an attached image via a trash icon overlay.
- [ ] All image flows work fully offline.
- [ ] Images persist across app restarts.

## Non-goals

Promoted to `out-of-scope.md`.

## User stories

### Story 1 — Attach image from gallery

**As a** casual user, **I want** to add a screenshot from my gallery to a note, **so that** I can save context alongside text.

**Acceptance criteria:**
- [ ] System photo picker (`PickVisualMedia` / `PickMultipleVisualMedia`) launches from the capture screen.
- [ ] User can pick one or many images.
- [ ] Each selected image is copied to app internal storage.
- [ ] Images >5MB are downscaled before saving; a snackbar/toast says "image resized".
- [ ] During compression a blocking spinner is shown.
- [ ] After import, picked images appear in the note's image list.
- [ ] Works offline.

### Story 2 — Capture image via system camera

**As a** student, **I want** to photograph a whiteboard and add it to a note in one flow.

**Acceptance criteria:**
- [ ] Camera entry point in capture screen launches the system camera (`ActivityResultContracts.TakePicture` with FileProvider URI).
- [ ] Captured photo is saved into app internal storage.
- [ ] Compression rules from Story 1 apply identically.
- [ ] Image attaches to the note on successful capture; cancellation leaves the note unchanged.

### Story 3 — View attached images in a note

**As any** user, **I want** to see my attached images when viewing a note.

**Acceptance criteria:**
- [ ] Note detail screen renders all images for the note in order of attach.
- [ ] Images load from app internal storage via Coil.
- [ ] Images are NOT shown in the note list/preview (only in detail).
- [ ] Missing/corrupt image file shows a placeholder (no crash).

### Story 4 — Remove an attached image

**As any** user, **I want** to remove an image from a note.

**Acceptance criteria:**
- [ ] Each image in note detail has a trash icon overlay.
- [ ] Tapping the trash icon removes the image from the note's image list.
- [ ] The underlying file in app internal storage is deleted.
- [ ] Confirmation prompt? — TBD; assume no confirmation for v1, log if mob disagrees.

## Success metrics

**[ASKED OF MOB]** — see `open-questions.md` Q15. Without analytics in place, we need the mob to decide whether v1 ships measurement-blind or whether we add a minimal counter.

## Constraints

- **Deadline:** ASAP.
- **Max image size after import:** 5 MB. >5MB is auto-downscaled.
- **minSdk = 26.** `PickVisualMedia` is back-compat to API 19 via the AndroidX shim, so no separate legacy code path is required.
- **No new networking.** Local-only feature.
- **No Coil today** — adding it as a dependency is part of Story 3.
- **`Note` domain model has no image field today** — Foundation issue extends it.

## Links

- API contract: `./api-contract.md` (no BE changes)
- Context: `../../CONTEXT.md` (project-wide)
- Issues: `./issues/android/`
- Decisions: `./decisions.md`
- Out of scope: `./out-of-scope.md`
- Open questions: `./open-questions.md`
