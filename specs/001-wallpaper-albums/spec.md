# Feature Specification: Wallpaper Albums

**Feature Branch**: `001-wallpaper-albums`

**Created**: 2026-10-13

**Status**: Draft

**Input**: User description: "Aggiungi nel menù laterale la voce \"Album\", con la possibilità di creare e leggere album. Quando apro un'immagine che sia in cerca o nella home, aggiungi in basso un tasto \"Add to album\" con la possibilità di aggiungerlo ad un album esistente o di crearne uno nuovo e aggiungerlo a quello. Questi album devono essere esportabili e importabili, con un JSON che contenga l'identificativo di tutte le immagini che lo compongono. Un album deve avere sempre in cache locale le immagini che contiene"

## Clarifications

### Session 2026-10-13

- Q: When a user imports an album JSON whose name already exists locally, or imports the very same file twice, what should the app do? → A: Always create a new album; duplicate names and repeated imports produce separate albums.
- Q: On the album list, what should each album's "preview of its contents" show? → A: A single thumbnail of the first member in album order.
- Q: When should the app try to resolve imported wallpapers that were not available locally? → A: Automatically on app start and whenever an album is opened, while online.
- Q: When a wallpaper no longer belongs to any album, what should happen to its cached image and any imported placeholder record? → A: Delete the cached image; delete the wallpaper record only if it is an unresolved imported placeholder that is not favorited or browsed; never delete browsed or favorited wallpapers.
- Q: When storage is full or write access is denied during an album export or image caching, how should the app behave? → A: Fail only the affected operation with a clear error; the album and memberships stay intact; failed caching is marked FAILED and retried later.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create and browse albums (Priority: P1)

The user opens the side menu, selects the new "Album" entry, sees the list of their albums, creates a
new album by giving it a name, and opens an album to view the wallpapers it contains.

**Why this priority**: This is the foundation of the feature. Without the ability to create an album
and open it, none of the other capabilities (adding wallpapers, export/import, offline viewing) can
deliver value.

**Independent Test**: From a fresh state, open the side menu, open the Album section, create a
named album, and confirm it appears in the album list and opens as an empty album.

**Acceptance Scenarios**:

1. **Given** the app is open, **When** the user opens the side menu, **Then** an "Album" entry is
   shown alongside the existing sections.
2. **Given** the Album section is open, **When** the user creates an album with a non-empty name,
   **Then** the album appears in the album list.
3. **Given** at least one album exists, **When** the user opens an album, **Then** the album screen
   shows the wallpapers it contains.
4. **Given** an album contains no wallpapers, **When** the user opens it, **Then** an empty-state
   message is shown.

---

### User Story 2 - Add a wallpaper to an album from its detail (Priority: P1)

While viewing a wallpaper detail opened from the home gallery or from search, the user finds an
"Add to album" button at the bottom of the screen. Tapping it lets the user add the wallpaper to an
existing album or to a newly created album.

**Why this priority**: This is the primary way to populate albums. Without it, albums can only be
created empty.

**Independent Test**: Open any wallpaper detail from the home gallery and from search, tap
"Add to album", add it to an existing album, and verify it appears in that album.

**Acceptance Scenarios**:

1. **Given** a wallpaper detail is open (from home or from search), **When** the user taps
   "Add to album", **Then** a chooser lists the existing albums and offers creating a new one.
2. **Given** the chooser is open, **When** the user selects an existing album, **Then** the
   wallpaper is added to that album and a confirmation is shown.
3. **Given** the chooser is open, **When** the user creates a new album with a name, **Then** the
   wallpaper is added to the newly created album.
4. **Given** the wallpaper is already in the selected album, **When** the user adds it again,
   **Then** no duplicate entry is created.

---

### User Story 3 - Album images are always available offline (Priority: P2)

Every album keeps the images it contains in local cache, so an album remains fully viewable without
a network connection.

**Why this priority**: The app is offline-first for browsed and favorited wallpapers; albums extend
that guarantee and would be unreliable if they required connectivity.

**Independent Test**: Add wallpapers to an album while online, then disable connectivity and verify
the album and its images still open and render.

**Acceptance Scenarios**:

1. **Given** wallpapers were added to an album while online, **When** the device is offline,
   **Then** the album still lists those wallpapers and renders their images.
2. **Given** a wallpaper is added to an album, **Then** a local copy of its image content is kept
   for that album without requiring browsing the wallpaper again.
3. **Given** a wallpaper is added to an album, **Then** the album keeps a local copy of the
   full-resolution image content for that wallpaper.

---

### User Story 4 - Export and import an album (Priority: P3)

The user exports an album to a portable JSON file that lists the identifiers of the images it
contains, and later imports that file to recreate or restore the album, possibly on another device.

**Why this priority**: Portability makes albums useful beyond a single install, but albums are
already valuable without it.

**Independent Test**: Export an album, delete it, import the exported file, and verify the album is
recreated with the same set of wallpapers.

**Acceptance Scenarios**:

1. **Given** an album exists, **When** the user exports it, **Then** a JSON file is produced that
   contains the album name and the identifiers of all its images.
2. **Given** a valid album export file, **When** the user imports it, **Then** a new album is
   created from the file's name and wallpapers, and no existing album is modified.
3. **Given** an imported album references wallpapers, **Then** those wallpapers become available in
   the album and are cached locally according to the offline requirement.
4. **Given** an invalid, unreadable, or malformed file, **When** the user imports it, **Then** an
   error is shown and no album or partial data is created.

---

### Edge Cases

- What happens when the user tries to create an album with an empty or whitespace-only name?
- What happens when two albums have the same name?
- What happens when a wallpaper is added to multiple albums?
- What happens when an exported album is empty (no images)?
- What happens when the same album file is imported twice? Each import creates a separate new album;
  no existing album is modified.
- What happens when an imported file references wallpaper identifiers that are not present in the
  local library and the device is offline?
- What happens when an imported file is not valid JSON, is missing required fields, or has an
  unsupported structure?
- What happens when an album is deleted, or a wallpaper is removed from an album, while another
  album still references the same wallpaper? The cached image is kept while any album references it
  and cleaned only when the last reference is removed (FR-017).
- What happens when storage is full or write access is denied during export or caching? Only the
  affected operation fails: the album and memberships remain, export shows an error, and image
  caching is marked FAILED and retried later (FR-018).
- How does the system behave with very large albums (hundreds of wallpapers) for listing, caching,
  and import?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The side menu MUST include an "Album" entry that opens the Album section.
- **FR-002**: The Album section MUST list all albums, each showing its name and a preview thumbnail
  of the first member in album order (no thumbnail when the album is empty).
- **FR-003**: Users MUST be able to create a new album and give it a name.
- **FR-004**: The system MUST reject album names that are empty or contain only whitespace.
- **FR-005**: Users MUST be able to open an album and browse the wallpapers it contains, with a clear
  empty state when it contains none.
- **FR-006**: A wallpaper detail screen, opened from the home gallery or from search, MUST show an
  "Add to album" action.
- **FR-007**: The "Add to album" action MUST let the user add the current wallpaper to an existing
  album or to a new album created on the spot.
- **FR-008**: The system MUST NOT create duplicate entries when the same wallpaper is added to the
  same album more than once.
- **FR-009**: Users MUST be able to export an album to a JSON file that contains the album name and
  the identifiers of all the images it contains.
- **FR-010**: Users MUST be able to import an album from a JSON file produced by the export, and the
  system MUST always create a new album from the file's name and referenced wallpapers without
  modifying any existing album; duplicate names and repeated imports yield separate albums.
- **FR-011**: The system MUST reject an import file that is malformed or missing the required
  structure, showing an error and making no changes to existing albums.
- **FR-012**: Every album MUST keep the full-resolution image content of the wallpapers it contains
  in local cache so the album is fully viewable without a network connection.
- **FR-013**: Albums and their membership MUST be persisted locally and survive app restarts.
- **FR-014**: All new user-visible text MUST be provided in both English and Italian.
- **FR-015**: Deleting an album MUST NOT delete the wallpapers from the wallpaper library or from
  favorites.
- **FR-016**: When an imported album references wallpapers that are not yet in the local library, the
  system MUST still import the album immediately, showing the not-yet-available wallpapers as
  placeholders, and MUST attempt to resolve their metadata and cache their full-resolution image
  content on app start and whenever the album is opened, while online.
- **FR-017**: When a wallpaper no longer belongs to any album, the system MUST delete its cached
  full-resolution image; it MUST delete the wallpaper record only when that record is an unresolved
  imported placeholder that is neither favorited nor part of the browsed library, and MUST retain
  browsed or favorited wallpapers (FR-015).
- **FR-018**: If storage is full or write access is denied, the system MUST fail only the affected
  export or caching operation with a user-visible error, MUST NOT alter album memberships, and MUST
  mark the affected image caching as FAILED so it can be retried later.

### Key Entities *(include if feature involves data)*

- **Album**: A user-named collection of wallpapers, with a name, an ordered set of member
  wallpapers, and metadata such as creation time and member count.
- **Album membership**: The association between an album and a wallpaper, ordered within the album so
  the album displays consistently.
- **Album export file**: A portable JSON document containing the album name and the list of image
  identifiers that make up the album.
- **Wallpaper**: An existing entity (identifier, image URLs, metadata) reused by albums; favorites
  and albums are independent collections over the same wallpaper library.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can create a named album in under 15 seconds from opening the side menu.
- **SC-002**: A user can add the currently viewed wallpaper to an album in at most 2 taps after
  tapping "Add to album", including creating a new album.
- **SC-003**: 100% of wallpapers added to an album while online are viewable within that album when
  the device is offline.
- **SC-004**: An export-then-import round trip reproduces an album with exactly the same set of
  wallpapers as the original.
- **SC-005**: 100% of the new user-visible strings are available in both English and Italian.
- **SC-006**: Importing a malformed file never creates a partial, empty, or corrupted album.
- **SC-007**: Adding the same wallpaper to the same album repeatedly results in exactly one entry.

## Assumptions

- The "Add to album" action is placed in the existing wallpaper detail screen, which is shared by the
  home gallery and search, so it automatically covers both entry points.
- The export file is a JSON document containing the album name and the list of image identifiers;
  additional metadata (such as creation time or item ordering) may be included but is not required
  for correctness.
- Export and import use the platform file picker / share mechanism, so the user controls where the
  file is stored; no cloud service or account is involved.
- Album names are free text and are not required to be unique; if names collide, they are treated as
  distinct albums.
- Deleting albums and removing individual wallpapers from an album are included as reasonable
  defaults for basic album management, even though not explicitly requested.
- Albums are local to the device and are not synced across devices except through manual
  export/import.
- Offline caching for albums stores the full-resolution image content of each member wallpaper, so
  opening any album wallpaper works without a network connection.
- The wallpaper library, favorites, and paging behavior continue to work as they do today; albums are
  an additional collection layered on the same local wallpaper data.
- The Android backup and device-transfer exclusions for secrets remain in effect; album data
  contains no secrets.
