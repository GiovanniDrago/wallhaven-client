# UI Contracts: Drawer Filters, Donation Move and Update Check

## Drawer

- **Filters entry** (`DrawerContent`): a single clickable row labelled "Filters" with an
  expand/collapse indicator. While collapsed only the header is visible; tapping it reveals the
  existing toggles (NSFW, sketchy, people, anime, general). Tapping again hides them.
- **Reset rule**: the group starts collapsed every time the drawer opens. The state lives in
  `MainScreen` and is reset when the drawer closes.
- **Donation**: there is no "Buy me a coffee" entry in the drawer. The action remains in Settings and
  opens the same URL.
- Toggles keep their current behaviour and persistence (`FilterPreferences`).

## Settings — update section

- Shows the installed version (`BuildConfig.VERSION_NAME`).
- A "Check for updates" action runs a manual check (always, regardless of the last automatic check).
- Manual outcomes:
  - **Update available**: a dialog titled with the new version and actions "Later" / "Download".
    "Download" opens the APK asset (or the release page) in the browser; a failure shows a message.
  - **Up to date**: a message says no updates are available.
  - **Error** (offline/API/parse): a message says the check could not be completed; the app stays
    usable.
- The update dialog is shared with the automatic check so both look and behave the same.

## Automatic check

- Runs once on app start, at most once per calendar day (`last_update_check` date in DataStore).
- If a newer release exists, the same dialog is shown without user action.
- If the silent check fails, nothing is shown and the date is not stored, so it retries on a later
  start.

## Repository / data contract

`UpdateRepository.check(currentVersion: String): UpdateCheckResult`

- Calls `GithubApi.latestRelease(userAgent)` (`GET releases/latest`, no auth).
- Maps the DTO to `LatestRelease` (version from the tag, download URL per the asset rules).
- Returns `UpToDate`, `UpdateAvailable(version, downloadUrl)` or `Error`.
- `shouldRunSilentCheck()` / `markCheckedToday()` implement the once-per-day rule using
  `UpdatePreferences`.

## Localization

All new user-visible text (check action, dialog title, Later, Download, no updates, check error,
download error, filter expand/collapse descriptions) MUST exist in both `values/` and `values-it/`.
The unused `drawer_donate` key is removed from both files.
