---
type: decisions
feature: image-attachments
created: 2026-05-03
tags:
  - inception/decisions
  - feature/image-attachments
---

# Decisions

> [!info]
> ADR-lite log for the image-attachments feature. Each entry is a decision the driver made during Inception that the mob should ratify. See [[PRD]] for what's being built and [[_index]] for the work plan.

---

### D1 — Local-only storage for v1 — 2026-05-03

- **Context:** Image attachments need to be stored somewhere. Three options surfaced: full BE storage, hybrid (local + BE metadata), local-only.
- **Options considered:** Full BE upload (S3 / Railway volume), hybrid (local file + server reference), local-only (device-bound).
- **Decision:** Local-only on Android device. No server work in v1.
- **Why:** Cheapest path to a working feature; ship-fast bias matches the "ASAP" deadline. Cloud sync is a separate, larger concern that benefits from being addressed once we know what users actually do with images.
- **Consequences:** Images are bound to the install — uninstall = data loss; switching devices = no images. Multi-device users will hit this limit. Migration path documented in `out-of-scope.md`.

### D2 — System camera intent, not in-app camera — 2026-05-03

- **Context:** Camera path could be a system intent or an in-app CameraX implementation.
- **Options considered:** System camera intent (cheap), CameraX in-app (more polish, enables future whiteboard helpers).
- **Decision:** System camera intent for v1.
- **Why:** Smallest viable surface; respects ship-ASAP. Whiteboard-specific helpers (perspective correction etc.) are speculative — defer until we know the segment uses the feature.
- **Consequences:** No in-app polish or helpers. Camera UX inherits whatever the user's default camera app provides.

### D3 — Auto-downscale >5MB images and notify — 2026-05-03

- **Context:** 5MB cap was set by the driver. Modern phone photos often exceed it.
- **Options considered:** Reject and re-prompt, silent downscale, downscale + notify.
- **Decision:** Auto-downscale + notify the user ("image resized").
- **Why:** Reject-and-reprompt is hostile UX for a feature whose primary segment (students photographing whiteboards) will routinely produce >5MB photos. Silent downscale violates user trust if quality matters. Notify is the middle path.
- **Consequences:** We need an image processing routine on import. Notification UX needs design (snackbar / toast / inline).

### D4 — App internal storage, not MediaStore — 2026-05-03

- **Context:** Where on disk do attached images live?
- **Options considered:** App internal storage (private), MediaStore (visible to other apps).
- **Decision:** App internal storage.
- **Why:** Privacy by default. Notes are personal — leaking image attachments into the user's Photos roll is surprising. Internal storage also keeps file lifecycle bound to MindNote: uninstall removes everything cleanly.
- **Consequences:** Images aren't backed up to Google Photos automatically. Users who want that need to re-export, or wait for cloud sync.

### D5 — Images shown only in note detail (not list/preview) — 2026-05-03

- **Context:** Where in the app do attached images appear?
- **Options considered:** Inline in note list / preview rows, only in detail, both.
- **Decision:** Detail only.
- **Why:** Note list density and scroll performance matter; thumbnail loading on every list item is the kind of thing that quietly degrades the app over time. Detail is where the user has committed to a note — image rendering is justified.
- **Consequences:** Users won't see "this note has images" in the list. May want a small icon indicator later — not in v1.

### D6 — Trash icon overlay for image removal — 2026-05-03

- **Context:** How does a user remove an image?
- **Options considered:** Long-press menu, trash icon overlay, swipe gesture.
- **Decision:** Trash icon overlay on each image in detail.
- **Why:** Discoverable for casual users (no hidden gesture). Direct (no menu indirection). Standard pattern in Android image-grid UIs.
- **Consequences:** Adds visual chrome to the image grid. Confirmation dialog or undo not yet decided — see open-questions.md.

### D7 — Unlimited images per note — 2026-05-03

- **Context:** Cap or no cap?
- **Options considered:** Hard cap (e.g., 10), soft warn (after N), unlimited.
- **Decision:** Unlimited in v1.
- **Why:** No data yet on how many images users actually attach. Premature limits are easy to add later but harder to remove. Storage on device is not effectively constrained until the user chooses to keep adding.
- **Consequences:** Pathological users could fill internal storage. Acceptable for v1; add monitoring or warnings if it becomes a pattern.

### D8 — Blocking spinner during compression — 2026-05-03

- **Context:** Compression takes time. UX during that time?
- **Options considered:** Blocking modal spinner, inline progress on the image tile, no indicator (assume fast).
- **Decision:** Blocking spinner.
- **Why:** Compression is short but variable. A blocking spinner prevents the user from interacting with a half-imported state and avoids race conditions on note save. Inline progress is more code for marginal UX gain.
- **Consequences:** User can't multi-task in the app during import. Acceptable for short waits; if compression turns out to be slow on low-end devices, revisit.
