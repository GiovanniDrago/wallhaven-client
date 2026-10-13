# Quickstart Validation: Wallpaper Albums

This guide proves the feature works end-to-end. It contains no implementation code; details live in
[spec.md](./spec.md), [plan.md](./plan.md), [data-model.md](./data-model.md) and
[contracts/](./contracts/).

## Prerequisites

- JDK 17 (project requires JVM 17).
- No local Android SDK is required on the development VM; compilation and tests run in CI.
- For manual checks, install the debug APK produced by the `Test` CI workflow.

## Automated Verification (quality gate)

Run the repository quality gate:

```bash
./gradlew test assembleDebug
```

This is the same gate the `Test` workflow runs on every push (Constitution Principle V). The new
album tests are:

- `AlbumExportCodecTest` — JSON round-trip; rejects malformed documents; tolerates unknown fields.
- `AlbumRepositoryTest` — create/validate name, add idempotency, delete reference-counting, export
  and import orchestration using MockK fakes.
- `AlbumImageCacheTest` — download success/failure and file naming against OkHttp MockWebServer.
- `AlbumImportResolutionTest` — placeholder creation then later resolution with MockWebServer and
  `kotlinx-coroutines-test`.

## Manual Scenarios

### S1 — Create and browse albums (US1, FR-001..FR-005)

1. Open the side drawer; confirm an **Album** entry is present.
2. Open it; confirm the empty state is shown.
3. Create an album named "Nature"; confirm it appears in the list.
4. Try creating an album with a blank name; confirm it is rejected.

### S2 — Add a wallpaper to an album (US2, FR-006..FR-008)

1. Open a wallpaper from the **home gallery** detail; confirm an **Add to album** button at the
   bottom.
2. Tap it; add to "Nature"; confirm the confirmation snackbar and membership.
3. Repeat from a wallpaper opened from **search**; add to the same album twice; confirm no duplicate.
4. Create a new album from the picker and add the wallpaper; confirm it lands in the new album.

### S3 — Offline album images (US3, FR-012)

1. While online, add a few wallpapers to an album and wait for caching to finish.
2. Enable airplane mode.
3. Open the album; confirm every member renders (full-resolution when opened) with no network.

### S4 — Export / import round trip (US4, FR-009..FR-011, FR-016)

1. Export "Nature" to a JSON file; inspect it and confirm it matches
   [contracts/album-export.schema.json](./contracts/album-export.schema.json).
2. Delete the album, then import the file; confirm a **new** album is created with the same
   wallpapers and that no existing album was modified. Import the same file again and confirm a
   second, separate album is created (FR-010).
3. Import a malformed file; confirm an error is shown and no album is created.
4. Import a file referencing identifiers not present locally while offline; confirm the album is
   created with placeholders. Restore connectivity, reopen the album, and confirm placeholders
   resolve and images cache.

### S5 — Localization (Principle IV, FR-014)

1. Switch the device language to English and then Italian.
2. Confirm every new album string is translated in both locales and no user-visible text is
   hardcoded.

### S6 — Cleanup and failure isolation (FR-017, FR-018)

1. Add a wallpaper to an album, confirm its full-resolution image is cached, then delete the album.
2. Confirm a browsed or favorited wallpaper is still present in the library/favorites, while the
   cached image of a wallpaper no longer referenced by any album is removed (FR-017).
3. With storage nearly full, export an album and add a wallpaper to an album; confirm only the
   affected operation fails with a message and the album membership is unchanged (FR-018).

## Expected Outcomes

- All JVM tests pass and the debug APK assembles in CI.
- Album grids and gallery remain smooth; cached album images open instantly.
- Existing favorites and browsed wallpapers survive the database upgrade (no destructive migration).
- Export files never contain the wallhaven API key or any secret.
