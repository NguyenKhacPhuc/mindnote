---
type: issue
feature: image-attachments
lane: android
status: ready
wave: 0
estimate: 45m
blocked-by: []
tags:
  - inception/issue
  - lane/android
  - feature/image-attachments
  - status/ready
  - wave/0
---

# [Android] Configure FileProvider for camera output URIs

**Lane:** Android
**PRD section:** Story 2 — Capture image via system camera
**API contract section:** N/A

## Why

`ActivityResultContracts.TakePicture` needs a `Uri` writable by the system camera app. Modern Android forbids `file://` URIs across processes; FileProvider is the standard escape hatch.

## Implementation steps

1. `app/src/main/AndroidManifest.xml` — add a `<provider>` inside `<application>`:
   ```xml
   <provider
       android:name="androidx.core.content.FileProvider"
       android:authorities="${applicationId}.fileprovider"
       android:exported="false"
       android:grantUriPermissions="true">
     <meta-data android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths"/>
   </provider>
   ```
2. `app/src/main/res/xml/file_paths.xml` — create:
   ```xml
   <paths>
     <files-path name="images" path="images/"/>
   </paths>
   ```
3. Add a small helper in `core/storage/` (or extend `ImageStorageService`): `fun newCameraOutputUri(): Uri` that creates an empty file under `filesDir/images/cam-${UUID}.jpg` and returns a `FileProvider.getUriForFile(...)`.
4. Unit test (Robolectric or instrumentation): call `newCameraOutputUri()`, assert the returned URI's scheme is `content` and authority matches `${applicationId}.fileprovider`.

## Files to touch

- `app/src/main/AndroidManifest.xml` — modify
- `app/src/main/res/xml/file_paths.xml` — create
- `app/src/main/java/com/mindnote/core/storage/...` — modify (helper)
- `app/src/test/java/com/mindnote/core/storage/...` — create test

## Acceptance criteria

- [ ] App builds; manifest merger does not flag conflicts.
- [ ] `newCameraOutputUri()` returns a `content://` URI under the project's authority.
- [ ] No `FileUriExposedException` when using the URI in #12.
- [ ] Unit test passes.

## Blocked by

Nothing — independently grabbable.

## Notes

- Authority `${applicationId}.fileprovider` keeps it unique without hardcoding the package.
- `<files-path>` maps to `context.filesDir`, which is consistent with D4.
