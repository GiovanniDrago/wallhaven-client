# Phase 0 Research: Drawer Filters, Donation Move and Update Check

## R1. Collapsible filters in the drawer

- **Decision**: Hoist a `filtersExpanded` boolean in `MainScreen`, pass it plus `onToggleFilters` to
  `DrawerContent`, and render a single clickable "Filters" header (with an expand/collapse icon) that
  reveals the existing toggles when expanded. Reset it to `false` whenever the drawer is closed
  (`LaunchedEffect(drawerState.isOpen)`), so the group starts collapsed each time the drawer opens.
- **Rationale**: FR-001/FR-002 and the spec assumption (collapsed on each open). Hoisting in
  `MainScreen` gives access to the drawer state; a local `rememberSaveable` inside `DrawerContent`
  would persist across opens.
- **Alternatives considered**: `rememberSaveable` inside `DrawerContent` (rejected: would not reset per
  open); a nested `ModalBottomSheet` (rejected: heavier and not a drawer pattern).

## R2. Move the donation action out of the drawer

- **Decision**: Remove the drawer donation item and the `onDonate` parameter from `DrawerContent` and
  `MainScreen`; keep the existing Settings donation button. Remove the now-unused `drawer_donate`
  string from both locales.
- **Rationale**: FR-004/FR-005; the Settings button already opens the same URL.
- **Alternatives considered**: keeping a hidden drawer entry (rejected: confusing dead code).

## R3. Update source: GitHub latest release

- **Decision**: Add a Retrofit `GithubApi` (`GET repos/GiovanniDrago/wallhaven-client/releases/latest`)
  with base URL `https://api.github.com/`, `Accept: application/vnd.github+json` and
  `User-Agent: wallhaven-client/<installedVersion>`, no auth. Map it to `GithubReleaseDto`
  (`tag_name`, `html_url`, `assets[name, browser_download_url]`) in `data/api/dto`.
- **Rationale**: Mirrors `home_inventory`'s `UpdateService`; reuses the mandated Retrofit +
  kotlinx.serialization + OkHttp stack; no token/secret (Principle III).
- **Alternatives considered**: parsing the GitHub HTML (rejected: brittle); authenticated API
  (rejected: a secret is not needed for a public release).

## R4. Version comparison and tag extraction

- **Decision**: First extract the version from the release tag by taking the first semver-like numeric
  group (`\d+(\.\d+){0,2}`), so a tag like `personal-WallhavenClient-0.5.0-16` yields `0.5.0`.
  Then a pure `isNewerVersion(latest, current)` strips a leading `v`, drops build metadata after `+`,
  splits on `.`, parses each component numerically (non-numeric or missing → 0) and compares major,
  minor, patch in order. If the tag contains no numeric group, treat it as "no update".
- **Rationale**: FR-008/SC-007 and the reference implementation's `_isNewer`, adapted to our release
  naming (`personal-WallhavenClient-<version>-<run>`). Pure and JVM-testable.
- **Release-process note**: `versionName` is bumped manually. A new build that keeps the same
  `versionName` is therefore not reported as newer; bumping `versionName` is the trigger for the update
  notification (the run number is a CI build counter, not a semantic version). This matches the
  reference app's criteria and is recorded as an assumption.
- **Alternatives considered**: lexicographic comparison (rejected: `1.10` vs `1.9`); a SemVer library
  (rejected: new dependency); comparing the CI run number to `versionCode` (rejected: unrelated
  counters).

## R5. Download URL selection

- **Decision**: Among the release assets whose name ends with `.apk`, prefer `WallhavenClient-latest.apk`
  when present, otherwise the first `.apk`; if there is no APK, use the release `html_url`.
- **Rationale**: FR-010; matches `home_inventory`'s asset preference while accounting for our stable
  `latest` asset.
- **Alternatives considered**: always `html_url` (rejected: extra step for the user); the
  `.sha256`/non-APK assets (rejected).

## R6. Once-per-day silent check

- **Decision**: Store `last_update_check` as an ISO local date (`yyyy-MM-dd`) in the existing DataStore
  preferences (`UpdatePreferences`). The silent check runs only when the stored date differs from
  today; after a successful silent check (update or not) the date is written. A failed silent check
  writes nothing (so it can retry next start). Manual checks always run and write the date on success.
- **Rationale**: FR-013/SC-005; keeps the silent check cheap and lets errors retry.
- **Alternatives considered**: in-memory only (rejected: lost on process death); storing in Room
  (rejected: this is a preference, and the constitution keeps the wallpaper DB for wallpapers).

## R7. Result model and error handling

- **Decision**: `UpdateRepository.check(currentVersion)` returns a sealed `UpdateCheckResult`:
  `UpToDate`, `UpdateAvailable(version, downloadUrl)`, `Error`. Silent failures produce no UI; manual
  failures show a localized error message. Manual "no update" shows a message too.
- **Rationale**: FR-011/FR-012; the UI never crashes on network/API errors.
- **Alternatives considered**: throwing exceptions to the UI (rejected: error handling would leak).

## R8. Update dialog and app-start wiring

- **Decision**: A shared `UpdateAvailableDialog` composable (ui/components) showing the new version and
  "Later"/"Download". `MainViewModel` runs the silent check on init and exposes a one-shot update
  event; `MainScreen` shows the dialog. `SettingsViewModel` exposes manual-check state/events and
  `SettingsScreen` shows the same dialog, plus snackbars for up-to-date/error.
- **Rationale**: one dialog implementation, both entry points; keeps ViewModels free of UI.
- **Alternatives considered**: duplicating the dialog per screen (rejected).

## R9. Testable current version

- **Decision**: Provide `currentVersion: String` to `UpdateRepository` via Hilt from
  `BuildConfig.VERSION_NAME`, so tests can inject a known version. The pure comparison stays in
  `domain/VersionComparison`.
- **Rationale**: Principle V (JVM tests) and avoids coupling the repository test to the build value.
- **Alternatives considered**: reading `BuildConfig` directly inside the repository (works, but harder
  to test).

## R10. Localization

- **Decision**: Add EN/IT keys for check-for-updates, update-available title, later, download,
  no-updates, check error, download error, and filter expand/collapse content descriptions. Remove the
  unused `drawer_donate` key from both locales.
- **Rationale**: Principle IV and FR-014.
- **Alternatives considered**: none; localization is mandatory.
