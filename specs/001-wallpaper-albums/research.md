# Phase 0 Research: Wallpaper Albums

This document resolves the open technical questions behind the plan. Each decision records what was
chosen, why, and what alternatives were rejected.

## R1. Album persistence and paging

- **Decision**: Store albums, membership and image-cache metadata in the existing Room database.
  Add `albums`, `album_entries` and `album_images` tables. Expose album contents through a Room-backed
  `PagingSource` (`PagingSource<Int, WallpaperEntity>`) and drive the album screen with Paging 3,
  exactly like favorites.
- **Rationale**: Constitution Principle II makes Room the single source of truth and requires all
  paging to use Paging 3 with a coherent cache. Favorites already use this pattern, so albums reuse
  proven code and stay observable: any membership write invalidates the Room `PagingSource`
  automatically.
- **Alternatives considered**: Keeping albums in DataStore/JSON (rejected: not queryable, no paging,
  no relational integrity, violates Room-as-SoT); a separate database (rejected: unnecessary and
  splits the offline cache).

## R2. Full-resolution offline image cache

- **Decision**: Cache full-resolution image bytes as files in app-private storage
  (`filesDir/album_images/<wallpaperId>.<ext>`), downloaded with the already-injected OkHttp client.
  A per-wallpaper row in `album_images` records `localPath`, `status` (`PENDING`, `DOWNLOADING`,
  `CACHED`, `FAILED`), `bytes` and `cachedAt`. The cache is keyed by wallpaper id, not by album, and
  reference-counted through `album_entries`: when no album references a wallpaper, its cached file is
  deleted. A wallpaper record is deleted only when it is an unresolved import placeholder that is
  neither favorited nor browsed. Album grids and the album detail view load images from the local
  file with Coil when the file is `CACHED`, otherwise from the network thumbnail and a placeholder.
- **Rationale**: The spec requires full-resolution images to remain available offline (FR-012).
  Coil's disk cache is not addressable by key and can be evicted, so it cannot guarantee presence.
  Keying by wallpaper id avoids duplicate downloads when a wallpaper belongs to several albums.
  App-private storage needs no runtime permissions and keeps album data out of the media gallery
  (which the existing "Download" action, not albums, targets).
- **Alternatives considered**: Coil disk cache (rejected: eviction breaks the guarantee); copy into
  MediaStore `Pictures/Wallhaven` (rejected: pollutes the user's gallery and requires permissions);
  cache per album (rejected: duplicates bytes for shared wallpapers).

## R3. Export and import file format and mechanism

- **Decision**: Export/import a single UTF-8 JSON document per album using kotlinx.serialization.
  The document contains a schema `version`, the album `name` and an ordered `wallpapers` array of
  wallhaven image identifiers. Files are read/written through the Android Storage Access Framework
  (`ActivityResultContracts.CreateDocument` / `OpenDocument`) so the user chooses the destination and
  no storage permission is required. The codec validates the document (non-blank name, non-blank
  identifiers) and fails cleanly on malformed input without touching existing albums. Import is
  always additive: it creates a new album from the file's name and never modifies an existing album,
  so duplicate names and repeated imports yield separate albums (FR-010).
- **Rationale**: The spec explicitly requires a JSON file containing the identifiers of the images
  (FR-009, FR-010). SAF is the platform-standard, permission-free path on minSdk 26. Putting the
  codec and DTOs in `data/album` keeps serialization models out of the UI layer (Principle I).
- **Alternatives considered**: `Intent.ACTION_SEND` sharing only (rejected: no import counterpart);
  `WRITE_EXTERNAL_STORAGE` + direct file paths (rejected: deprecated and permission-heavy);
  embedding base64 image data in JSON (rejected: spec calls for identifiers, and it would bloat the
  file).

## R4. Importing albums that reference unknown wallpapers

- **Decision**: Import writes the album and its memberships immediately. Wallpapers missing from the
  local `wallpapers` table are represented as placeholder memberships (identifier only). The
  `AlbumRepository` then resolves them through the existing detail endpoint (`GET w/{id}`) with the
  API key header, upserts the resulting metadata into Room, and enqueues the full-resolution image
  download. Resolution is attempted on album open and on app start; while offline the placeholders
  remain and are retried when resolution is next attempted.
- **Rationale**: FR-016 requires an immediate import even offline, with later resolution and caching.
  Reusing `refreshWallpaper`/the existing DTO mapping keeps a single mapping path.
- **Alternatives considered**: Failing the import without connectivity (rejected: contradicts
  FR-016); skipping unknown identifiers (rejected: silently loses data).

## R5. Database migration strategy

- **Decision**: Bump `WallhavenDatabase` to version 2 and add an explicit `Migration(1, 2)` that
  creates the new album tables. Stop using `fallbackToDestructiveMigration(dropAllTables = true)` for
  normal upgrades so existing favorites and browsed wallpapers are preserved.
- **Rationale**: Principle II treats the local database as the offline source of truth. A destructive
  fallback triggered by the version bump would silently wipe favorites on update, which the spec's
  assumptions rule out. Albums are user data and must survive restarts (FR-013).
- **Alternatives considered**: Keeping destructive fallback (rejected: data loss on upgrade);
  `exportSchema = true` plus auto-migrations (viable, but the project currently sets
  `exportSchema = false`; a hand-written migration is simpler and testable).

## R6. UI integration

- **Decision**: Add `AlbumRoute` and `AlbumDetailRoute` to the typed Navigation Compose routes and an
  "Album" item to `DrawerContent`/`AppSection`. The album list screen lists albums with a preview and
  a create action; the album detail screen shows a paged grid plus export/import/delete actions. The
  shared `DetailScreen` gains an "Add to album" button below the existing download/favorite row; it
  opens a modal bottom sheet (`AddToAlbumSheet`) listing existing albums and offering "New album".
- **Rationale**: The detail screen is shared by gallery and search, so a single addition covers both
  entry points (spec assumption). Reusing the existing drawer/navigation and grid components keeps
  the change consistent with the current UI.
- **Alternatives considered**: A separate detail screen per entry point (rejected: duplication);
  a floating action button (rejected: spec asks for the button at the bottom of the detail screen).

## R7. Testing strategy on the JVM

- **Decision**: Cover the new logic with JVM tests: `AlbumExportCodecTest` (JSON round-trip,
  malformed input, unknown-field tolerance), `AlbumRepositoryTest` (add/duplicate handling, delete
  reference-counting, export/import orchestration with MockK fakes), `AlbumImageCacheTest`
  (download success/failure and file naming with OkHttp MockWebServer), and
  `AlbumImportResolutionTest` (placeholder creation and later resolution using MockWebServer and
  `kotlinx-coroutines-test`).
- **Rationale**: Principle V requires JVM-only tests with JUnit4, MockK, MockWebServer and
  coroutines-test. The project has no Robolectric dependency, so Room DAO behaviour is exercised
  through repository-level fakes rather than an in-memory database, keeping the suite device-free.
- **Alternatives considered**: Instrumented Room tests (rejected: need a device/emulator, forbidden
  by Principle V and unavailable on the dev VM); adding Robolectric (rejected: new dependency and
  outside the mandated stack).

## R8. Localization

- **Decision**: Add matching keys for every new string to `values/strings.xml` and
  `values-it/strings.xml`, following existing naming (`albums_title`, `album_add_to`, etc.), and
  reference them via `stringResource`. No hardcoded user-visible text.
- **Rationale**: Principle IV requires completeness in both locales.
- **Alternatives considered**: None; this is a hard requirement.

## R9. Clarified decisions (Session 2026-10-13)

- **Import is additive** (FR-010): always create a new album; never merge into or replace an existing
  one.
- **Album list preview** (FR-002): a single thumbnail of the first member in album order.
- **Resolution trigger** (FR-016): retry unresolved imported wallpapers on app start and on album
  open, while online.
- **Cleanup** (FR-017): delete the cached image when the last album reference is gone; delete the
  wallpaper record only for unresolved placeholders that are neither favorited nor browsed.
- **Failure isolation** (FR-018): a full disk or denied write fails only the affected operation and
  marks caching `FAILED` for retry, leaving memberships unchanged.
