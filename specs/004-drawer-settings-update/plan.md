# Implementation Plan: Drawer Filters, Donation Move and Update Check

**Branch**: `004-drawer-settings-update` | **Date**: 2026-10-15 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/004-drawer-settings-update/spec.md`

## Summary

Reorganize the side drawer and extend Settings:

1. **Collapsible filters**: the drawer shows a single "Filters" entry that expands/collapses the
   existing toggles (NSFW, sketchy, people, anime, general). The toggles keep their current behaviour
   and persistence.
2. **Donation move**: remove "Buy me a coffee" from the drawer; the existing Settings donation button
   remains the only entry point.
3. **Update check**: Settings shows the installed version and a "Check for updates" action. The check
   mirrors `home_inventory`'s `UpdateService`: query the GitHub "latest release" endpoint with a
   `User-Agent` (no auth token), compare numeric major/minor/patch versions ignoring a leading `v` and
   build metadata, and offer the first APK asset (else the release page) in a "Later/Download" dialog.
   A silent check runs at most once per calendar day on app start; manual checks always run. Errors
   show a message and never block the app.

The change stays inside the existing layers: `ui/` for the drawer/settings, `data/` (api/dto,
prefs, repository) for the update checker, and `domain/` for the pure version comparison. No new
dependencies: the GitHub API uses the existing Retrofit + kotlinx.serialization + OkHttp stack, and the
last-check date uses the existing DataStore.

## Technical Context

**Language/Version**: Kotlin 2.4.10, JVM 17

**Primary Dependencies**: Jetpack Compose (Material 3), Hilt, Retrofit 3 + kotlinx.serialization,
OkHttp 5, AndroidX DataStore. No new dependencies.

**Storage**: DataStore Preferences for the last automatic-check date (new key in the existing
store). No Room changes.

**Testing**: JVM unit tests with JUnit4, MockK, OkHttp MockWebServer and kotlinx-coroutines-test:
version comparison, GitHub release parsing, asset selection, once-per-day throttling. Run by
`./gradlew test assembleDebug`.

**Target Platform**: Android, minSdk 26 (java.time available), targetSdk/compileSdk 37

**Project Type**: mobile-app (single Android application module `app/`)

**Performance Goals**: a manual check shows its result within normal network latency; the silent check
is fire-and-forget and never blocks the UI.

**Constraints**: no secrets (no tokens/API keys in the GitHub request or logs); all new strings EN/IT;
existing drawer toggles behave identically.

**Scale/Scope**: drawer + settings UI plus one small repository/DTO/preferences set; one external API.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Layered Architecture & Dependency Direction | Drawer/settings changes in `ui/`; GitHub DTOs in `data/api/dto`, preferences in `data/prefs`, fetching in `data/repository`; UI uses ViewModels/repository only. | PASS |
| II. Room as Single Source of Truth (Offline-First & Paging) | Not affected: update state lives in DataStore, not the wallpaper DB; no Paging changes. | PASS |
| III. Secrets & Privacy by Default | The update check sends no token or API key and logs no secret; only the public release endpoint is queried. | PASS |
| IV. Localization Completeness | Every new string is added to `values/` and `values-it/`. | PASS |
| V. Verification & Quality Gate | Version comparison + release parsing + throttling are JVM-tested; CI gate `./gradlew test assembleDebug`. | PASS |

No violations. Complexity Tracking is not required.

**Post-Design Re-Check (after Phase 1)**: Still PASS. The design keeps the GitHub API behind a
repository, adds no dependencies, stores only a date in DataStore, sends no secret, localizes all new
text, and isolates the testable version logic in `domain/`.

## Project Structure

### Documentation (this feature)

```text
specs/004-drawer-settings-update/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   ├── github-release.schema.md
│   └── ui-contracts.md
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
app/src/main/java/dev/giovannidrago/wallhaven/
├── ui/
│   ├── components/DrawerContent.kt   # UPDATE: collapsible Filters, remove donation entry
│   ├── MainScreen.kt                 # UPDATE: drawer expand state, remove onDonate, silent check/dialog
│   ├── MainViewModel.kt              # UPDATE: once-per-day silent update check + update event
│   ├── settings/SettingsScreen.kt    # UPDATE: "Check for updates" + version + update dialog/snackbars
│   └── settings/SettingsViewModel.kt # UPDATE: manual check state/events
├── data/
│   ├── api/GithubApi.kt              # NEW: GET releases/latest
│   ├── api/dto/GithubReleaseDto.kt   # NEW: tag_name, html_url, assets
│   ├── prefs/UpdatePreferences.kt    # NEW: last automatic-check date in DataStore
│   └── repository/UpdateRepository.kt# NEW: fetch, compare, asset selection, throttling
├── domain/
│   └── VersionComparison.kt          # NEW: pure numeric major/minor/patch comparison
├── di/NetworkModule.kt               # UPDATE: provide GithubApi (api.github.com)
└── (BuildConfig.VERSION_NAME is the installed version)

app/src/main/res/values/strings.xml       # UPDATE: new EN strings
app/src/main/res/values-it/strings.xml    # UPDATE: new IT strings

app/src/test/java/dev/giovannidrago/wallhaven/
├── VersionComparisonTest.kt          # NEW
└── UpdateRepositoryTest.kt           # NEW (MockWebServer)
```

**Structure Decision**: Single Android application module. The GitHub integration is a normal
data-layer repository with DTOs and preferences, consumed by `MainViewModel` (silent) and
`SettingsViewModel` (manual); the pure version comparison lives in `domain/` so it is JVM-testable and
kept out of the UI.

## Complexity Tracking

No constitution violations; this section is intentionally empty.
