---

description: "Task list for Wallpaper Albums implementation"
---

# Tasks: Wallpaper Albums

**Input**: Design documents from `/specs/001-wallpaper-albums/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included. Constitution Principle V requires every change to be verified by the repository
JVM test suite (`./gradlew test assembleDebug`), so test tasks are generated for each story.

**Organization**: Tasks are grouped by user story from spec.md to allow independent implementation
and testing.

**Clarifications folded in (Session 2026-10-13)**: import always creates a new album (FR-010);
album list shows a single first-member thumbnail (FR-002); unresolved imports are retried on app
start and album open (FR-016); reference-counted cleanup (FR-017); storage-failure isolation
(FR-018).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: User story label (US1, US2, US3, US4) on user-story phase tasks only
- Every task includes an exact file path

## Path Conventions

Single Android application module. App code under `app/src/main/java/dev/giovannidrago/wallhaven/`,
tests under `app/src/test/java/dev/giovannidrago/wallhaven/`, resources under `app/src/main/res/`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the localized strings and confirm the existing stack needs no new dependencies.

- [X] T001 [P] Add all album string resources (e.g. `drawer_albums`, `albums_title`, `album_new`, `album_name_hint`, `album_create`, `album_delete`, `album_delete_confirm`, `album_empty`, `album_item_count`, `album_add_to`, `album_add_to_title`, `album_add_new`, `album_added`, `album_export`, `album_import`, `album_export_success`, `album_import_success`, `album_import_error`, `album_remove`, `album_cache_pending`, `album_cache_failed`, `cd_add_to_album`, `cd_album_preview`) to `app/src/main/res/values/strings.xml`
- [X] T002 [P] Add the exact same string keys with Italian translations to `app/src/main/res/values-it/strings.xml`
- [X] T003 Verify Hilt, Room + Paging 3, OkHttp, Coil and kotlinx.serialization are already present so `app/build.gradle.kts` and `gradle/libs.versions.toml` stay unchanged (no new dependencies)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Data layer, DI and navigation that every user story depends on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T004 [P] Create UI-facing domain models `Album`, `AlbumSummary` (name, member count, single first-member preview thumbnail), `AlbumItem`, `AlbumImageStatus` (enum: `PENDING`, `DOWNLOADING`, `CACHED`, `FAILED`) in `app/src/main/java/dev/giovannidrago/wallhaven/domain/Album.kt`
- [X] T005 [P] Create Room entities in `app/src/main/java/dev/giovannidrago/wallhaven/data/db/AlbumEntities.kt`: `AlbumEntity` (`id` PK UUID, `name` non-blank, `createdAt`, `updatedAt`), `AlbumEntryEntity` (composite PK `albumId`+`wallpaperId`, FK `albumId`→`albums.id` CASCADE, `wallpaperId`→`wallpapers.id` CASCADE, `position`, `addedAt`, index on `wallpaperId`) and `AlbumImageEntity` (`wallpaperId` PK, `localPath: String?`, `status` value in the four enum names, `bytes: Long?`, `cachedAt: Long?`, `lastError: String?`); also add `metadataResolved: Boolean = true` to `WallpaperEntity`
- [X] T006 [P] Define the `AlbumItemEntity` read projection (an `@Embedded WallpaperEntity` plus `position`, `localPath`, `cacheStatus`) in `app/src/main/java/dev/giovannidrago/wallhaven/data/db/AlbumEntities.kt`, used as the album `PagingSource` row type
- [X] T007 [P] Create `app/src/main/java/dev/giovannidrago/wallhaven/data/db/AlbumDao.kt`: album insert/update/delete, `observeAlbums` (ordered by `updatedAt` DESC), `observeAlbum(id)`, `albumItemsPagingSource(albumId): PagingSource<Int, AlbumItemEntity>` (LEFT JOIN `album_entries`/`wallpapers`/`album_images` ordered by `position`), membership insert with `OnConflictStrategy.IGNORE` (idempotent) and `MAX(position)+1`, membership removal, count of albums referencing a wallpaper, and image-cache upsert/observe
- [X] T008 Update `app/src/main/java/dev/giovannidrago/wallhaven/data/db/WallhavenDatabase.kt`: register `AlbumEntity`, `AlbumEntryEntity`, `AlbumImageEntity`; bump version to 2; add `Migration(1, 2)` creating the album tables, indices and the `metadataResolved` column (`DEFAULT 1`); remove `fallbackToDestructiveMigration(dropAllTables = true)` from the normal upgrade path
- [X] T009 Create `app/src/main/java/dev/giovannidrago/wallhaven/data/repository/AlbumRepository.kt` with core operations: `albums()`, `album(id)`, `albumItems(id)`, `createAlbum(name)` (trim, reject blank per FR-004), `deleteAlbum(id)` (retain browsed/favorited wallpapers per FR-015), `addToAlbum(albumId, wallpaperId)` (idempotent per FR-008), `removeFromAlbum(...)`, and internal reference-count cleanup hooks (FR-017)
- [X] T010 Update `app/src/main/java/dev/giovannidrago/wallhaven/di/AppModule.kt` to provide `AlbumDao` from `WallhavenDatabase`
- [X] T011 Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/navigation/Routes.kt`: add `@Serializable data object AlbumRoute`, `@Serializable data class AlbumDetailRoute(val albumId: String)` and an `ALBUMS` entry in `AppSection`
- [X] T012 Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/DrawerContent.kt`: add the "Album" drawer item and its `labelRes()`/`icon()` mappings
- [X] T013 Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/MainScreen.kt`: add `AlbumRoute`/`AlbumDetailRoute` composables and include `ALBUMS` in `navigateTo`

**Checkpoint**: Foundation ready - all user stories can now begin.

---

## Phase 3: User Story 1 - Create and browse albums (Priority: P1) 🎯 MVP

**Goal**: A drawer entry opens an album list where albums can be created and opened (empty state
when empty).

**Independent Test**: From a fresh state, open the drawer, open Album, create a named album, confirm
it appears and opens as an empty album; confirm a blank name is rejected.

### Tests for User Story 1

- [X] T014 [US1] Write `app/src/test/java/dev/giovannidrago/wallhaven/AlbumRepositoryTest.kt`: `createAlbum` trims and rejects blank names (FR-004), and `albums()` returns summaries ordered by `updatedAt` descending (MockK fake `AlbumDao`, `kotlinx-coroutines-test`)

### Implementation for User Story 1

- [X] T015 [P] [US1] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/albums/AlbumsViewModel.kt` exposing `albums`, `empty` and create/delete actions plus one-shot events
- [X] T016 [US1] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/albums/AlbumsScreen.kt`: album list with name, member count and a **single thumbnail of the first member** (none when empty) per FR-002, create dialog with blank-name validation, delete with confirmation, empty state (`album_empty`)
- [X] T017 [P] [US1] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/albums/AlbumDetailViewModel.kt` exposing `album` and `items: Flow<PagingData<AlbumItemEntity>>` via `cachedIn`
- [X] T018 [US1] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/albums/AlbumDetailScreen.kt`: paged grid of members ordered by `position`, empty state, and a back affordance (renders thumbnail for now; full-res caching arrives in US3)

**Checkpoint**: US1 fully functional and independently testable (MVP).

---

## Phase 4: User Story 2 - Add a wallpaper to an album (Priority: P1)

**Goal**: The shared wallpaper detail screen has a bottom "Add to album" button that adds the
wallpaper to an existing or newly created album.

**Independent Test**: Open a wallpaper from the home gallery and from search, tap "Add to album",
add it to an existing album and to a new one, and confirm no duplicates.

### Tests for User Story 2

- [X] T019 [US2] Extend `app/src/test/java/dev/giovannidrago/wallhaven/AlbumRepositoryTest.kt` with `addToAlbum` cases: inserting a membership, and adding the same wallpaper twice yields exactly one entry (FR-008)

### Implementation for User Story 2

- [X] T020 [P] [US2] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/albums/AddToAlbumSheet.kt`: modal bottom sheet listing albums with a "New album" action and a name field
- [X] T021 [US2] Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/detail/DetailViewModel.kt` with add-to-album state (album list, selected/created target, confirmation/error events) delegating to `AlbumRepository`
- [X] T022 [US2] Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/detail/DetailScreen.kt` to render the bottom "Add to album" button (below the existing download/favorite row) and host `AddToAlbumSheet`, showing the confirmation snackbar

**Checkpoint**: US1 and US2 both work independently.

---

## Phase 5: User Story 3 - Album images always available offline (Priority: P2)

**Goal**: Every album keeps the full-resolution image of each member wallpaper in local app storage
so albums render offline.

**Independent Test**: Add wallpapers to an album while online, enable airplane mode, and confirm the
album and its full-resolution images still render.

### Tests for User Story 3

- [X] T023 [P] [US3] Write `app/src/test/java/dev/giovannidrago/wallhaven/AlbumImageCacheTest.kt` using OkHttp MockWebServer: successful download writes a file with the expected name/extension, non-2xx and IO failures surface as `FAILED` (FR-018), and no API key appears in the request URL
- [X] T024 [P] [US3] Extend `app/src/test/java/dev/giovannidrago/wallhaven/AlbumRepositoryTest.kt` with cleanup and retention cases: removing the last album reference deletes the cached image and deletes an unresolved, non-favorited placeholder; a favorited or browsed wallpaper is retained (FR-015, FR-017)

### Implementation for User Story 3

- [X] T025 [P] [US3] Create `app/src/main/java/dev/giovannidrago/wallhaven/data/album/AlbumImageCache.kt`: download full-resolution bytes with the injected `OkHttpClient` into `filesDir/album_images/<wallpaperId>.<ext>`, update `AlbumImageEntity` status (`PENDING`→`DOWNLOADING`→`CACHED`/`FAILED`) through the DAO, and delete a file when its last reference is gone; on disk/write failure set `FAILED` and leave memberships unchanged (FR-018)
- [X] T026 [US3] Update `app/src/main/java/dev/giovannidrago/wallhaven/data/repository/AlbumRepository.kt` to enqueue caching on `addToAlbum`, retry `FAILED` entries, and perform reference-counted cleanup on `removeFromAlbum`/`deleteAlbum`: delete the cached file when no album references the wallpaper, and delete the wallpaper record only when it is an unresolved import placeholder that is neither favorited nor browsed (FR-017)
- [X] T027 [US3] Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/albums/AlbumDetailScreen.kt` to load the cached full-resolution file with Coil when `status == CACHED`, show the remote thumbnail/placeholder otherwise, and reflect `PENDING`/`FAILED` state

**Checkpoint**: Album contents are fully viewable offline.

---

## Phase 6: User Story 4 - Export and import an album (Priority: P3)

**Goal**: Export an album to a JSON file with its name and image identifiers, and import such a file
to create a new album, online or offline.

**Independent Test**: Export an album, import the file, and confirm a **new** album is created with
the same wallpapers; import a malformed file and confirm nothing is created.

### Tests for User Story 4

- [X] T028 [P] [US4] Write `app/src/test/java/dev/giovannidrago/wallhaven/AlbumExportCodecTest.kt`: JSON round-trip preserves name and ordered identifiers, unknown fields are tolerated, malformed/blank-name/unsupported-version documents are rejected, and the encoded document never contains an API key (Principle III)
- [X] T029 [P] [US4] Write `app/src/test/java/dev/giovannidrago/wallhaven/AlbumImportResolutionTest.kt` with MockWebServer + `kotlinx-coroutines-test`: importing identifiers missing locally creates the album with placeholders and never modifies an existing album (FR-010), and a later resolution (app start / album open) fetches `GET w/{id}` metadata and marks the wallpaper resolved (FR-016)

### Implementation for User Story 4

- [X] T030 [P] [US4] Create `app/src/main/java/dev/giovannidrago/wallhaven/data/album/AlbumTransferDto.kt` with `@Serializable` `AlbumExport(version, name, wallpapers)` matching `contracts/album-export.schema.json` (kept out of `ui/`)
- [X] T031 [P] [US4] Create `app/src/main/java/dev/giovannidrago/wallhaven/data/album/AlbumExportCodec.kt`: encode/decode the export document with validation (version == 1, non-blank name, non-blank unique identifiers) returning a typed success/failure
- [X] T032 [US4] Update `app/src/main/java/dev/giovannidrago/wallhaven/data/repository/AlbumRepository.kt` with `exportAlbum(id)` and `importAlbum(document)`: import **always creates a new album** (never modifies an existing one, FR-010), writes membership immediately (placeholder wallpapers with `metadataResolved = false`), then resolves unknown identifiers via the existing detail endpoint on app start and album open and enqueues image caching (FR-016)
- [X] T033 [US4] Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/albums/AlbumDetailViewModel.kt` and `app/src/main/java/dev/giovannidrago/wallhaven/ui/albums/AlbumDetailScreen.kt` to add export/import via SAF (`ActivityResultContracts.CreateDocument`/`OpenDocument`) and surface `transferState` snackbars/errors (FR-011, FR-018)

**Checkpoint**: All four user stories are independently functional.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Localization completeness, privacy hardening and the quality gate.

- [X] T034 [P] Review `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml` for key parity and confirm no hardcoded user-visible album strings remain in the new Compose code (Principle IV)
- [X] T035 [P] Confirm album entries and exports contain no API key or secret, and that cached-image requests use only the image URL without the `X-API-Key` header (Principle III)
- [X] T036 [P] Update `README.md` features list to document albums, offline album caching and import/export
- [X] T037 Verify FR-013 across the version 1→2 upgrade: assemble in CI and confirm no destructive fallback, then document the manual upgrade check (existing favorites/browsed wallpapers survive) in `specs/001-wallpaper-albums/quickstart.md`
- [X] T038 Run the quality gate `./gradlew test assembleDebug` (CI `Test` workflow) and execute the manual scenarios in `specs/001-wallpaper-albums/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup; BLOCKS all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational.
- **User Story 2 (Phase 4)**: Depends on Foundational; reuses US1 screens/VM for the picker but is
  independently testable from the detail screen.
- **User Story 3 (Phase 5)**: Depends on US1 (album contents) and US2 (adding triggers caching).
- **User Story 4 (Phase 6)**: Depends on Foundational and US1; placeholder resolution reuses the
  image cache from US3 for full offline caching.
- **Polish (Phase 7)**: Depends on all targeted stories.

### Within Each User Story

- Tests before implementation (write failing first).
- Domain/DAO/repository before ViewModels, ViewModels before screens.

### Parallel Opportunities

- T001/T002 (locale files) and T003 run in parallel (Setup).
- T004–T007 are different files and run in parallel; T008 depends on T005.
- After Foundational, US1 tasks marked [P] can run together; test tasks (T014, T019, T023, T024,
  T028, T029) touch different files and can run in parallel where their phases overlap.
- T025 (image cache), T030/T031 (transfer DTO/codec), T034/T035/T036 are independent files.

---

## Parallel Example: Foundational

```bash
# Independent files, launch together:
Task: "Create domain models in domain/Album.kt"
Task: "Create Room entities in data/db/AlbumEntities.kt"
Task: "Define AlbumItemEntity projection in data/db/AlbumEntities.kt"
Task: "Create AlbumDao in data/db/AlbumDao.kt"
```

## Parallel Example: User Story 3

```bash
Task: "Write AlbumImageCacheTest.kt with MockWebServer"
Task: "Extend AlbumRepositoryTest.kt with cleanup/retention cases"
Task: "Create AlbumImageCache.kt in data/album/"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup) and Phase 2 (Foundational).
2. Complete Phase 3 (US1): albums can be created and browsed.
3. **STOP and VALIDATE**: `./gradlew test assembleDebug` passes; create/browse works; blank name
   rejected.
4. Demo the MVP.

### Incremental Delivery

1. Setup + Foundational → foundation ready.
2. US1 → albums list/create (MVP).
3. US2 → add wallpapers to albums.
4. US3 → full-resolution offline album images.
5. US4 → export/import with offline placeholder resolution.
6. Polish → localization/privacy review and the quality gate.

Each increment keeps the previous stories working and adds value without breaking them.

---

## Notes

- [P] tasks touch different files and have no incomplete dependencies.
- Every story ends at a checkpoint that is independently testable.
- The database migration is non-destructive on purpose: existing favorites/browsed wallpapers MUST
  survive (research R5, FR-013).
- No new Gradle dependencies are introduced; the mandated stack is reused as-is.
- Commit after each task or logical group.
