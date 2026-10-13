# Phase 1 Data Model: Wallpaper Albums

All persistent state lives in the existing Room database (`wallhaven.db`), which remains the single
source of truth (Constitution Principle II). Full-resolution image bytes live in app-private storage
and are indexed by the database.

## Entity Relationship Overview

```text
Album 1 ──< AlbumEntry >── 1 Wallpaper
Wallpaper 1 ── 0..1 AlbumImage
```

- An `Album` has many `AlbumEntry` rows (ordered membership).
- An `AlbumEntry` links one `Album` to one `Wallpaper`.
- A `Wallpaper` may belong to many albums (many-to-many through `AlbumEntry`) and has at most one
  `AlbumImage` cache record (keyed by wallpaper id, reference-counted by album membership).

## Entities

### AlbumEntity (`albums`)

| Field | Type | Notes |
|-------|------|-------|
| `id` | `String` (PK) | UUID generated on creation |
| `name` | `String` | User-provided, trimmed; MUST be non-blank |
| `createdAt` | `Long` | Epoch millis |
| `updatedAt` | `Long` | Epoch millis, bumped on membership/name changes |

Validation: `name` MUST NOT be empty or whitespace-only (FR-004). Names are not required to be
unique (spec Assumptions); duplicate names are distinct albums.

### AlbumEntryEntity (`album_entries`)

| Field | Type | Notes |
|-------|------|-------|
| `albumId` | `String` | Composite PK, FK → `albums.id` `ON DELETE CASCADE` |
| `wallpaperId` | `String` | Composite PK, FK → `wallpapers.id` `ON DELETE CASCADE` |
| `position` | `Int` | Display order within the album; next value is `MAX(position)+1` |
| `addedAt` | `Long` | Epoch millis |

Index: `wallpaperId` (for reference counting and cleanup).

Validation: the composite primary key guarantees uniqueness, so adding the same wallpaper to the same
album is idempotent (FR-008).

### AlbumImageEntity (`album_images`)

| Field | Type | Notes |
|-------|------|-------|
| `wallpaperId` | `String` (PK) | FK → `wallpapers.id` `ON DELETE CASCADE` |
| `localPath` | `String?` | Absolute path under app-private storage once cached |
| `status` | `String` | One of `PENDING`, `DOWNLOADING`, `CACHED`, `FAILED` |
| `bytes` | `Long?` | Cached file size |
| `cachedAt` | `Long?` | Epoch millis of successful cache |
| `lastError` | `String?` | Short failure reason (no secrets) |

Validation: `status` MUST be one of the four values. A row exists for every wallpaper currently
referenced by at least one album (FR-012).

### WallpaperEntity (`wallpapers`, existing — extended)

Add one column:

| Field | Type | Notes |
|-------|------|-------|
| `metadataResolved` | `Boolean` | Default `true`; `false` marks an imported placeholder |

Placeholder rows (created when an imported album references an unknown identifier, FR-016) are
inserted with `metadataResolved = false` and empty display fields. They are only reachable through
albums; gallery and favorites queries are unaffected because those paths filter on
`gallery_entries`/`favorited`. Once the detail endpoint resolves the identifier, the row is updated
with real metadata and `metadataResolved` becomes `true`.

## Database Migration

- `WallhavenDatabase` version **1 → 2**.
- `Migration(1, 2)` creates `albums`, `album_entries`, `album_images`, their indices, and adds the
  `metadataResolved` column to `wallpapers` with `DEFAULT 1`.
- The destructive fallback (`fallbackToDestructiveMigration(dropAllTables = true)`) MUST be removed
  from the normal upgrade path so existing favorites/browsed wallpapers survive (research R5).
- `exportSchema` stays `false` to match the current project configuration.

## State Transitions

### Image cache lifecycle (`AlbumImageEntity.status`)

```text
PENDING ──start──> DOWNLOADING ──success──> CACHED
                      │
                      └──failure──> FAILED ──retry──> DOWNLOADING
CACHED ──(last album reference removed)──> row + file deleted
```

- A wallpaper added to an album enqueues `PENDING` (or reuses an existing `CACHED` record).
- `CACHED` is the guarantee behind FR-012 / US3: the album screen renders from `localPath`.
- When the last `AlbumEntry` referencing a wallpaper is removed (member removed, album deleted, or
  import replaced), the `album_images` row and its file are deleted (reference counting).

### Imported wallpaper resolution (`WallpaperEntity.metadataResolved`)

```text
false (placeholder) ──detail fetch succeeds + image cached──> true
        ▲                                                        │
        └──────────────── retry on album open / app start ───────┘
```

- Import always completes and writes `AlbumEntry` rows (FR-010, FR-016).
- While unresolved and offline, the album shows placeholder tiles.
- Once resolved, metadata lands in `wallpapers`, `metadataResolved` flips to `true`, and the image
  cache lifecycle above runs.

## Derived / Read Models

- **AlbumItemEntity** (query projection, not a table): `@Embedded WallpaperEntity` plus `position`,
  `localPath`, `cacheStatus`. Produced by a `LEFT JOIN` over `album_entries`, `wallpapers` and
  `album_images`, ordered by `position`, and returned by the album `PagingSource`.
- **AlbumSummary**: album plus member count and up-to-four preview stable ids/thumb urls for the list
  screen.

## Validation Rules Summary

| Rule | Source |
|------|--------|
| Album name non-blank | FR-004 |
| No duplicate (album, wallpaper) | FR-008 |
| Album membership persisted across restarts | FR-013 |
| Full-resolution image cached per member wallpaper | FR-012 |
| Export JSON must include album name + all identifiers | FR-009 |
| Malformed import rejected with no changes | FR-011 |
| Deleting an album does not delete library/favorites | FR-015 |
| No API key stored in any album table or export | Principle III |
