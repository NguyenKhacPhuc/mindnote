---
type: open-questions
feature: image-attachments
created: 2026-05-03
tags:
  - inception/open-questions
  - feature/image-attachments
---

# Open questions

> [!question]
> The Inception phase ends when this file is empty (or only contains items the mob explicitly deferred). Each unresolved question carries a `[DRIVER GUESS]` so the mob has a starting point.

## Open

### Q15 — How do we measure success of the image-attachments feature?

- **Why it matters:** PRD requires at least one success metric. We have no analytics layer in the app today, so any answer either ships measurement-blind or mandates a small instrumentation issue.
- **[DRIVER GUESS]:** Driver said "leave for now". Best guess is to ship without measurement and revisit if adoption is unclear after a few weeks.
- **[ASKED OF]:** Product / All

### Q-extra — Confirmation on image deletion?

- **Why it matters:** User taps trash icon → image is removed and file is deleted. Should we show a confirmation dialog ("Remove image?") or treat tap-trash as immediate?
- **[DRIVER GUESS]:** No confirmation in v1 — keeps the UX fast and the bar to undo low. If the mob wants undo, file a small follow-up.
- **[ASKED OF]:** Android / Product

## Resolved

(none yet — this is the first iteration)
