# Implementation Plan: Wallpaper Albums

**Branch**: `001-wallpaper-albums` | **Date**: 2026-10-13 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-wallpaper-albums/spec.md`

## Summary

Add user-created wallpaper albums to the Wallhaven client. An "Album" entry in the side drawer opens
a list of albums where the user can create, open and delete albums. The shared wallpaper detail
screen (used by both the home gallery and search) gains an "Add to album" action at the bottom that
adds the current wallpaper to an existing album or to a newly created one. Albums can be exported to
and imported from a portable JSON file that contains the album name and the identifiers of its
wallpapers. Every album keeps the full-resolution image content of its wallpapers in local app
storage so albums stay fully viewable offline.

The technical approach reuses the existing stack without new dependencies: albums are persisted in
the existing Room database and exposed through Paging 3; the full-resolution cache is an
app-private, reference-counted image store fed by the existing OkHttp client; export/import uses
kotlinx.serialization and the Android Storage Access Framework; the UI follows the existing
Compose + Hilt + ViewModel layering.

## Technical Context

**Language/Version**: Kotlin 2.4.10, JVM 17

**Primary Dependencies**: Jetpack Compose (Material 3, Compose BOM 2026.09.00), Hilt 2.60.1,
Room 2.8.5 + Paging 3.5.1, Retrofit 3.0.0 + kotlinx.serialization 1.11.0, OkHttp 5.5.0, Coil 2.7.0,
Navigation Compose 2.10.1, AndroidX DataStore. No new dependencies are introduced.

**Storage**: Room (SQLite) remains the single source of truth; new `albums` and `album_entries`
tables plus JSON columns. Full-resolution album images are stored as files in app-private storage
(`filesDir`), tracked by the database.

**Testing**: JVM unit tests with JUnit4, MockK, OkHttp MockWebServer and kotlinx-coroutines-test;
run by `./gradlew test assembleDebug` (the `Test` CI workflow). No device/emulator is required.

**Target Platform**: Android, minSdk 26, targetSdk/compileSdk 37

**Project Type**: mobile-app (single Android application module `app/`)

**Performance Goals**: album and gallery grids scroll smoothly; adding a wallpaper to an album
returns an immediate confirmation and downloads the full-resolution image in the background; cached
album images open instantly with no network; album list paging stays coherent after every write.

**Constraints**: offline-capable after first add; must not introduce secrets into exports, logs or
URLs; must not destroy existing favorites on database upgrade; must keep strings localized in
`values/` and `values-it/`.

**Scale/Scope**: personal, single-user app; expected tens of albums with up to a few hundred
wallpapers each. Full-resolution image caching can consume significant storage, so the cache is
reference-counted and cleaned when an image is no longer referenced by any album.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Layered Architecture & Dependency Direction | Albums logic lives in `data/db`, `data/repository`, `data/album` (export/import + image cache) and `ui/albums`; UI talks only to ViewModels and the repository; export/import models stay out of `ui/`; Hilt provides all dependencies. | PASS |
| II. Room as Single Source of Truth (Offline-First & Paging) | Albums, membership and image-cache metadata are persisted in Room; album contents are exposed through a Room-backed `PagingSource`; writes keep the Paging cache coherent; full-resolution images are cached locally for offline viewing. | PASS |
| III. Secrets & Privacy by Default | The API key is never written to album records, never included in export JSON, and is sent only through the existing `X-API-Key` header; album logs contain identifiers only. | PASS |
| IV. Localization Completeness | Every new string is added to both `values/strings.xml` and `values-it/strings.xml`; no hardcoded UI text. | PASS |
| V. Verification & Quality Gate | New logic (album repository, image cache, export/import codec, unknown-identifier resolution) is covered by JVM tests with MockK / MockWebServer / coroutines-test; the CI quality gate `./gradlew test assembleDebug` must pass. | PASS |

No violations. Complexity Tracking is not required.

**Post-Design Re-Check (after Phase 1)**: Still PASS. The design keeps all album data in Room with a
Room-backed `PagingSource` (Principle II), stores full-resolution images in app-private storage
(Principle II/III), adds an explicit `Migration(1, 2)` instead of a destructive fallback (Principle
II), keeps the API key out of album records and export JSON (Principle III), localizes all strings
(Principle IV), and verifies new logic with JVM-only tests (Principle V). No new dependencies were
introduced.

## Project Structure

### Documentation (this feature)

```text
specs/001-wallpaper-albums/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   ├── album-export.schema.json
│   ├── album-export.example.json
│   └── ui-contracts.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created here)
```

### Source Code (repository root)

```text
app/src/main/java/dev/giovannidrago/wallhaven/
├── data/
│   ├── api/                       # existing; no new endpoints required
│   ├── db/
│   │   ├── AlbumEntities.kt       # NEW: AlbumEntity, AlbumEntryEntity, AlbumImageEntity
│   │   ├── AlbumDao.kt            # NEW: album CRUD + PagingSource + image-cache queries
│   │   └── WallhavenDatabase.kt   # UPDATE: register entities, add Migration(1, 2)
│   ├── album/
│   │   ├── AlbumExportCodec.kt    # NEW: kotlinx.serialization JSON encode/decode + validation
│   │   ├── AlbumTransferDto.kt    # NEW: export/import DTOs (data layer only)
│   │   └── AlbumImageCache.kt     # NEW: app-private full-resolution image store (OkHttp)
│   └── repository/
│       ├── AlbumRepository.kt     # NEW: album use cases, add-to-album, export/import, sync
│       └── WallpaperRepository.kt # existing; reuse refreshWallpaper for metadata resolution
├── domain/
│   └── Album.kt                   # NEW: UI-facing album models + cache states
├── ui/
│   ├── navigation/Routes.kt       # UPDATE: AlbumRoute, AlbumDetailRoute
│   ├── components/DrawerContent.kt# UPDATE: Album drawer entry
│   ├── detail/DetailScreen.kt     # UPDATE: bottom "Add to album" action
│   ├── detail/DetailViewModel.kt  # UPDATE: add-to-album state + chooser actions
│   ├── albums/
│   │   ├── AlbumsScreen.kt        # NEW: album list + create
│   │   ├── AlbumsViewModel.kt     # NEW
│   │   ├── AlbumDetailScreen.kt   # NEW: album contents + export/import/delete
│   │   ├── AlbumDetailViewModel.kt# NEW
│   │   └── AddToAlbumSheet.kt     # NEW: existing-album picker + create
│   └── MainScreen.kt              # UPDATE: album routes
├── di/
│   └── AppModule.kt               # UPDATE: provide AlbumDao / image cache paths
└── util/
    └── (existing helpers reused)

app/src/main/res/values/strings.xml       # UPDATE: English album strings
app/src/main/res/values-it/strings.xml    # UPDATE: Italian album strings

app/src/test/java/dev/giovannidrago/wallhaven/
├── AlbumExportCodecTest.kt        # NEW
├── AlbumRepositoryTest.kt         # NEW
├── AlbumImageCacheTest.kt         # NEW (MockWebServer)
└── AlbumImportResolutionTest.kt   # NEW
```

**Structure Decision**: Single Android application module (`app/`). New album code follows the
existing `data/` (db, repository, plus a focused `data/album` package for the export/import codec
and image cache), `domain/`, `ui/`, and `di/` boundaries already used by the project. No new modules
or dependencies are introduced.

## Complexity Tracking

No constitution violations; this section is intentionally empty.
