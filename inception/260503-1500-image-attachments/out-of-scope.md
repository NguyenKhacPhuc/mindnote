---
type: out-of-scope
feature: image-attachments
created: 2026-05-03
tags:
  - inception/out-of-scope
  - feature/image-attachments
---

# Out of scope

> [!warning]
> Things this feature is explicitly **not** doing in v1. The cheapest argument to prevent is one you wrote down.

- **Cloud sync of images** — Local-only is the v1 decision (see `decisions.md` D1). Tracked as a future feature. Will require BE work, an upload/download contract, and a migration plan for users with existing local images.
- **Cross-device image access** — Direct consequence of "local-only".
- **In-app camera (CameraX)** — Using system camera intent in v1 (D2). Whiteboard-specific perspective correction is not in scope; revisit if the student segment shows usage signal.
- **Image editing / annotation / cropping** — Pure attach + view + delete only.
- **Sharing images out from MindNote** — No "share image" affordance. Users can re-share from their original source if needed.
- **Multi-user image visibility** — App is still single-user demo (`userId=local`). Multi-user is a separate, larger concern.
- **Showing images in note list/preview** — D5 limits image rendering to detail view.
- **Image deletion confirmation dialog / undo** — Tap-trash is immediate in v1. May revisit (see `open-questions.md` Q-extra).
- **Backing up images to Google Photos / external storage** — Internal-storage decision (D4) means images stay private to the app.
- **Hard cap on images per note** — Unlimited in v1 (D7).
- **Analytics / measurement of feature usage** — No success metric instrumented (see Q15).
