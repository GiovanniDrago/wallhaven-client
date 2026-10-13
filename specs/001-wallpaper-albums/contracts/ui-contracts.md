# UI Contracts: Wallpaper Albums

These contracts describe the navigation, screen responsibilities and observable state for the album
feature. They are the boundary the UI layer exposes; all data access happens through ViewModels and
the `AlbumRepository` (Constitution Principle I).

## Navigation Routes

Added to the typed routes in `ui/navigation/Routes.kt`:

| Route | Arguments | Purpose |
|-------|-----------|---------|
| `AlbumRoute` (object) | none | Album list screen |
| `AlbumDetailRoute` | `albumId: String` | Contents of a single album |

`AppSection` gains an `ALBUMS` entry shown in `DrawerContent` between favorites and settings.

## Screen Contracts

### AlbumsScreen (`AlbumRoute`)

- **Shows**: a list of albums (name, member count, and a single thumbnail of the first member in
  album order), a create action, and an empty state when there are no albums.
- **Actions**: create album (name dialog, non-blank validation), open album, delete album (with
  confirmation).
- **State** (`AlbumsViewModel`): `albums: StateFlow<List<AlbumSummary>>`, `empty: Boolean`,
  one-shot user events (created, deleted, error) for snackbars.
- **Emptiness**: shows `empty_albums` message when the list is empty.

### AlbumDetailScreen (`AlbumDetailRoute`)

- **Shows**: the album name; a paged grid of member wallpapers ordered by `position`; placeholder
  tiles for unresolved imported wallpapers; the cached full-resolution image when available and the
  remote thumbnail otherwise.
- **Actions**: export album to a JSON document; import a JSON document, which always creates a new
  album and never modifies an existing one; remove a wallpaper from the album; delete the album;
  open a wallpaper detail.
- **State** (`AlbumDetailViewModel`): `album: StateFlow<Album?>`,
  `items: Flow<PagingData<AlbumItemEntity>>`, `transferState` (`Idle`, `Working`, `Exported(uri)`,
  `Imported(count)`, `Failed(reason)`) consumed for snackbars.
- **Offline**: opening an album triggers resolution of any placeholder members; cached images render
  with no network.

### AddToAlbumSheet (modal bottom sheet from `DetailScreen`)

- **Shows**: existing albums (name + count), a "New album" option, and the currently viewed
  wallpaper's membership state.
- **Actions**: add the current wallpaper to a selected album; create a new album and add the current
  wallpaper to it; dismiss.
- **State**: `albums: List<AlbumSummary>`, `addedToAlbumId: String?`, `newAlbumName: String`.

### DetailScreen (updated)

- Adds an **"Add to album"** button at the bottom of the content, below the existing download/favorite
  row. Tapping it opens `AddToAlbumSheet`. After a successful add, a confirmation snackbar is shown.
- The action is available regardless of whether the detail was opened from the gallery or search,
  because both use this screen.

## Data Access Contract

ViewModels depend on `AlbumRepository` and `WallpaperRepository` only. The repository exposes:

| Operation | Signature (conceptual) | FR |
|-----------|------------------------|----|
| Observe album list | `fun albums(): Flow<List<AlbumSummary>>` | FR-002 |
| Observe album | `fun album(id: String): Flow<Album?>` | FR-005 |
| Observe album contents (paged) | `fun albumItems(id: String): Flow<PagingData<AlbumItemEntity>>` | FR-005 |
| Create album | `suspend fun createAlbum(name: String): Album` | FR-003, FR-004 |
| Delete album | `suspend fun deleteAlbum(id: String)` | FR-015 |
| Add wallpaper | `suspend fun addToAlbum(albumId: String, wallpaperId: String)` | FR-006..FR-008 |
| Remove wallpaper | `suspend fun removeFromAlbum(albumId: String, wallpaperId: String)` | edge cases |
| Export album | `suspend fun exportAlbum(id: String): AlbumExport` | FR-009 |
| Import album | `suspend fun importAlbum(document: AlbumExport)` | FR-010, FR-011, FR-016 |

The repository, not the UI, owns the JSON codec (`data/album/AlbumExportCodec`), the image cache
(`data/album/AlbumImageCache`) and DB writes. Export/import DTOs never cross into `ui/`.

## Error and Feedback Contract

- Empty album name: inline validation, no album created (FR-004).
- Malformed import file: user-facing error, no album created or modified (FR-011).
- Image download failure: the member stays in the album; the tile shows a retry/`FAILED` indicator;
  the download is retried later. A full disk or denied write fails only the affected operation and
  marks the cache `FAILED` (FR-018).
- Album preview: the list shows a single thumbnail of the first member, or no thumbnail when empty
  (FR-002).
- Import: always creates a new album; duplicate names and repeated imports yield separate albums
  (FR-010).
- Duplicate add: treated as success (idempotent), no duplicate row (FR-008).

All user-visible text comes from string resources present in both `values/` and `values-it/`
(Principle IV).
