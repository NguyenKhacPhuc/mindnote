---
type: feature-index
feature: image-attachments
status: draft
created: 2026-05-03
tags:
  - inception/index
  - feature/image-attachments
  - status/draft
---

# Image Attachments — feature index

> [!info] **Status:** Draft / awaiting mob review
> Generated from Inception phase v0.1. Update this file as issues land.

## Quick links

- PRD: [[PRD]]
- API contract: [[api-contract]] (no BE work)
- Decisions: [[decisions]]
- Open questions: [[open-questions]]
- Out of scope: [[out-of-scope]]
- Project-wide context: [[CONTEXT]]

---

## Parallel work plan

The 17 Android issues group into **5 waves** of dependency. Issues in the same wave can be picked up simultaneously by different devs.

### 🟢 Wave 0 — start here (no blockers, ~3.5h total work, 5 parallel slots)

| Issue | Estimate | Touches |
|---|---|---|
| [[01-add-coil-dependency]] | 15m | Gradle |
| [[02-extend-note-model-with-images]] | 30m | domain/model |
| [[04-image-storage-service-import-no-compression]] | 60m | core/storage |
| [[07-gallery-affordance-and-picker-launch]] | 60m | features/capture |
| [[11-configure-fileprovider]] | 45m | manifest, res/xml |

> [!tip]
> **Two devs:** dev A grabs `01` + `02` + `07`; dev B grabs `04` + `11`. Day-1 done in parallel.
> **Three devs:** split 5 issues 2/2/1.

### 🟡 Wave 1 — unlocked once wave 0 lands (~4h total, 5 parallel slots)

| Issue | Estimate | Blocked by |
|---|---|---|
| [[03-persistence-supports-note-images]] | 60m | `02` |
| [[05-add-5mb-downscale-to-import]] | 60m | `04` |
| [[06-image-storage-service-delete]] | 20m | `04` |
| [[08-plumb-picked-uris-through-storage-service]] | 60m | `02`, `04`, `07` |
| [[12-camera-affordance-and-takepicture-launch]] | 45m | `11` |

### 🟠 Wave 2 (~2.5h, 3 parallel slots)

| Issue | Estimate | Blocked by |
|---|---|---|
| [[09-blocking-spinner-during-import]] | 45m | `08` |
| [[10-image-resized-snackbar]] | 45m | `05`, `08` |
| [[14-render-images-in-note-detail]] | 75m | `01`, `02`, `03` |

### 🔵 Wave 3 (~2.5h, 3 parallel slots)

| Issue | Estimate | Blocked by |
|---|---|---|
| [[13-plumb-camera-uri-through-storage-service]] | 60m | `08`, `09`, `10`, `12` |
| [[15-missing-file-placeholder]] | 45m | `14` |
| [[16-trash-icon-overlay-ui]] | 45m | `14` |

### 🟣 Wave 4 — final integration

| Issue | Estimate | Blocked by |
|---|---|---|
| [[17-wire-trash-tap-to-remove-and-delete]] | 60m | `06`, `16`, `03` |

---

## All issues

```
issues/android/
├── 01-add-coil-dependency
├── 02-extend-note-model-with-images
├── 03-persistence-supports-note-images
├── 04-image-storage-service-import-no-compression
├── 05-add-5mb-downscale-to-import
├── 06-image-storage-service-delete
├── 07-gallery-affordance-and-picker-launch
├── 08-plumb-picked-uris-through-storage-service
├── 09-blocking-spinner-during-import
├── 10-image-resized-snackbar
├── 11-configure-fileprovider
├── 12-camera-affordance-and-takepicture-launch
├── 13-plumb-camera-uri-through-storage-service
├── 14-render-images-in-note-detail
├── 15-missing-file-placeholder
├── 16-trash-icon-overlay-ui
└── 17-wire-trash-tap-to-remove-and-delete
```

> [!note] **How to update this index as work lands**
> When an issue's status changes, update its frontmatter (`status: ready` → `in-progress` → `done`). The Properties panel and tag pane will reflect it. Use Obsidian's search `path:issues/android tag:#status/ready` to see what's grabbable.

---

## Definition of done (whole feature)

- [ ] All 17 issues have status `done` in their frontmatter.
- [ ] [[open-questions]] has zero unresolved items.
- [ ] [[PRD]] success-metric question is answered or explicitly deferred.
- [ ] Image attach + view + remove flows run end-to-end on a real device.
