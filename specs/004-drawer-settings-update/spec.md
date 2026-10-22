# Feature Specification: Drawer Filters, Donation Move and Update Check

**Feature Branch**: `004-drawer-settings-update`

**Created**: 2026-10-15

**Status**: Draft

**Input**: User description: "Riorganizza il menù laterale per avere i filtri collassati sotto un'unica voce di menù che mostra le varie opzioni quando cliccata. Sposta il buy me a coffee nei settings. Aggiungi ai settings la possibilità di fare check di una versione nuova disponibile per il download, usa gli stessi criteri dell' app cheap_cheap o home_inventory che trovi su questa VM"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Collapsible filter group in the drawer (Priority: P1)

The side menu shows a single "Filters" entry instead of a long list of toggles. Tapping it expands the
group to reveal the filter options (NSFW, sketchy, people, anime, general); tapping it again collapses
them. The filter toggles behave exactly as they do today.

**Why this priority**: This is the main reorganization of the drawer and the biggest visible change.

**Independent Test**: Open the drawer, confirm only "Filters" is shown, tap it to reveal the toggles,
change one, collapse it, and confirm the change is applied.

**Acceptance Scenarios**:

1. **Given** the drawer is open, **When** it is displayed with the group collapsed, **Then** a single
   "Filters" entry is shown and the individual toggles are hidden.
2. **Given** the "Filters" entry is collapsed, **When** the user taps it, **Then** the filter options
   are revealed below it and the entry indicates the expanded state.
3. **Given** the group is expanded, **When** the user taps "Filters" again, **Then** the options are
   hidden and the collapsed state is restored.
4. **Given** the options are visible, **When** the user changes any toggle, **Then** the filters apply
   exactly as before (same behaviour and persistence).
5. **Given** the drawer is closed and reopened, **Then** the group keeps a predictable state (see
   Assumptions) without losing the user's filter values.

---

### User Story 2 - Donation entry lives only in Settings (Priority: P2)

"Buy me a coffee" is no longer in the side menu; it remains available in Settings.

**Why this priority**: A small consistency cleanup; the action already exists in Settings.

**Independent Test**: Open the drawer and confirm the donation entry is gone; open Settings and confirm
the donation action is still present and works.

**Acceptance Scenarios**:

1. **Given** the drawer is open, **When** it is displayed, **Then** there is no "Buy me a coffee" entry.
2. **Given** Settings is open, **When** the user reaches the About section, **Then** the donation action
   is available and opens the same donation link as today.

---

### User Story 3 - Check for a new version from Settings (Priority: P1)

In Settings the user can see the installed version and check whether a newer release is available. A
silent check also runs at most once per day when the app starts. When a newer version exists, a dialog
offers to download it (opened in the browser) or postpone; when there is none, a manual check reports
that the app is up to date.

**Why this priority**: The user explicitly asked for update checking, and it needs the same behaviour
as the reference app.

**Independent Test**: Open Settings and tap "Check for updates"; verify the out-of-date dialog appears
when a newer release exists, or the "no updates" message when it does not, and that Download opens the
release asset.

**Acceptance Scenarios**:

1. **Given** Settings is open, **When** the user taps "Check for updates", **Then** the app queries the
   latest published release and compares its version with the installed version.
2. **Given** a newer release exists, **When** the check completes, **Then** a dialog shows the new
   version with "Later" and "Download" actions.
3. **Given** the update dialog is shown, **When** the user taps "Download", **Then** the download URL of
   the release asset (or the release page when there is no asset) opens in the browser.
4. **Given** no newer release exists, **When** the user runs a manual check, **Then** a message says no
   updates are available.
5. **Given** the check fails (offline, API error), **When** the user runs a manual check, **Then** an
   error message is shown and the app keeps working normally.
6. **Given** the app starts on a new calendar day, **When** the silent check finds a newer release,
   **Then** the update dialog is shown without the user asking.
7. **Given** the app already ran the silent check today, **When** it starts again the same day,
   **Then** it does not perform another automatic check (manual checks still always run).

---

### Edge Cases

- What happens when the latest release tag includes a leading `v` or extra suffixes (e.g. build
  metadata)?
- What happens when the version has fewer than three numeric components (e.g. `1.2`)?
- What happens when the GitHub API is rate-limited or offline?
- What happens when the latest release has no downloadable asset?
- What happens when the user opens Settings while the silent check is still running?
- What happens when the current version cannot be read?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The side menu MUST present the filters as a single collapsible "Filters" entry instead of
  a flat list of toggles.
- **FR-002**: Tapping the "Filters" entry MUST toggle the visibility of the individual filter options,
  and the entry MUST indicate whether it is expanded or collapsed.
- **FR-003**: The filter options themselves (NSFW, sketchy, people, anime, general) MUST keep their
  current behaviour and persistence.
- **FR-004**: The "Buy me a coffee" entry MUST be removed from the side menu.
- **FR-005**: The donation action MUST remain available in Settings, opening the same link as today.
- **FR-006**: Settings MUST show the installed app version.
- **FR-007**: Settings MUST provide a "Check for updates" action.
- **FR-008**: The update check MUST query the latest published release of the app's repository and
  compare its version with the installed version using numeric major/minor/patch comparison, ignoring a
  leading `v` and any build metadata.
- **FR-009**: When a newer version exists, the app MUST show a dialog with the new version and "Later"
  and "Download" actions.
- **FR-010**: "Download" MUST open the release's first downloadable asset in the browser, falling back
  to the release page when no asset exists.
- **FR-011**: When a manual check finds no newer version, the app MUST report that no updates are
  available.
- **FR-012**: When the check fails, the app MUST show an error message and remain usable.
- **FR-013**: A silent update check MUST run on app start at most once per calendar day; manual checks
  MUST always run when requested.
- **FR-014**: All new user-visible text MUST be provided in both English and Italian.
- **FR-015**: The update check MUST NOT send or log any secret (no API key, no credentials).

### Key Entities *(include if feature involves data)*

- **Installed version**: The app version currently running, sourced from the build metadata.
- **Latest release**: The newest published release of the app's repository, with its version tag and a
  download URL (first asset, or the release page).
- **Last check date**: The calendar day of the last automatic check, used to throttle the silent check
  to once per day. Stored locally in the app's preferences (not in the wallpaper database).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of drawers show the filters under a single collapsible entry, with the options
  hidden while collapsed.
- **SC-002**: Changing a filter from the expanded group produces exactly the same result as before.
- **SC-003**: "Buy me a coffee" is absent from the drawer in 100% of openings and still reachable from
  Settings.
- **SC-004**: A manual check reports the correct outcome (update available / up to date / error) in
  every case.
- **SC-005**: At most one automatic update check runs per calendar day.
- **SC-006**: 100% of the new user-visible strings are available in both English and Italian.
- **SC-007**: A newer release is detected when its numeric version is greater than the installed one,
  including `v`-prefixed tags and build metadata.

## Assumptions

- The update checker mirrors the reference app (`home_inventory`) criteria: the GitHub "latest release"
  endpoint, a `User-Agent`, numeric major/minor/patch comparison ignoring a leading `v` and build
  metadata, the first release asset (else the release page) as the download URL, a once-per-day silent
  check on startup, an always-on manual check in Settings, and a "Later"/"Download" dialog that opens
  the browser.
- The repository is the app's own GitHub repository and its releases include an APK asset (the
  Personal Release workflow publishes one). No authentication token is used.
- The update is installed manually from the browser download; there is no in-app APK installation.
- The collapsible filter group starts collapsed each time the drawer is opened, and the selected filter
  values persist as they do today.
- The existing Settings donation button is the destination for the moved action; only the drawer entry
  is removed.
