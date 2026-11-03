# Quickstart Validation: Drawer Filters, Donation Move and Update Check

This guide proves the feature end-to-end. It contains no implementation code; details live in
[spec.md](./spec.md), [plan.md](./plan.md), [data-model.md](./data-model.md) and
[contracts/](./contracts/).

## Prerequisites

- JDK 17. No local Android SDK is required; builds and tests run in CI.
- For manual checks, install the debug/personal APK.

## Automated Verification (quality gate)

```bash
./gradlew test assembleDebug
```

New tests:

- `VersionComparisonTest` — numeric major/minor/patch comparison, leading `v`, build metadata,
  missing components, tag extraction (`personal-WallhavenClient-0.5.0-16` → compares `0.5.0`), and
  non-numeric tags.
- `UpdateRepositoryTest` — MockWebServer: newer/older version, APK asset selection (prefers
  `WallhavenClient-latest.apk`), release-page fallback when no APK, error on non-200, and the
  once-per-day throttle.

## Manual Scenarios

### D1 — Collapsible filters (US1, FR-001–FR-003)

1. Open the drawer: only a single "Filters" entry is visible, the toggles are hidden.
2. Tap "Filters": the toggles appear; the entry shows the expanded state.
3. Change a toggle; collapse and reopen the drawer: the group is collapsed again and the filter value
   is preserved.
4. Apply a filter change and confirm the gallery/search behave exactly as before.

### D2 — Donation moved (US2, FR-004–FR-005)

1. Open the drawer: no "Buy me a coffee" entry.
2. Open Settings → About: the donation button is present and opens the same link.

### D3 — Manual update check (US3, FR-006–FR-012)

1. Open Settings: the installed version is shown.
2. Tap "Check for updates":
   - with a newer release published, the dialog shows the new version and "Later"/"Download";
   - with no newer release, a "no updates" message appears;
   - offline/API error shows a "could not check" message.
3. Tap "Download": the APK asset (or the release page when there is no APK) opens in the browser.

### D4 — Automatic check (FR-013, SC-005)

1. On a fresh day, start the app with a newer release available: the update dialog appears without
   opening Settings.
2. Start the app again the same day: no automatic check runs.
3. Use the manual check the same day: it still runs.

### D5 — Localization (FR-014, Principle IV)

1. Switch the app to English and then Italian: every new string (drawer, Settings, dialog, messages)
   is translated and no hardcoded text appears.

## Expected Outcomes

- All JVM tests pass and the APK assembles in CI.
- The drawer shows one collapsible Filters entry; the donation entry is gone but remains in Settings.
- Manual and automatic update checks follow the reference app's criteria, with a working
  Later/Download dialog and no secrets sent or logged.
