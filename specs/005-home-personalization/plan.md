# Implementation Plan: Home Personalization

**Branch**: `005-home-personalization` | **Date**: `2026-10-15` | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/005-home-personalization/spec.md`

## Summary

Add a "Home personalization" drawer entry that opens a dedicated screen with the full search-filter
set adapted to the home (sorting, categories, purity, color, minimum/exact resolution, format). The
home feed uses these settings on every load/refresh. The values are persisted locally; the drawer's
quick filters stay aligned with them but, when changed, become a **session-only override** that wins
over the persisted configuration until the app restarts.

Implementation reuses the existing `WallhavenFilters` model and the `FilterPreferences` DataStore,
extending them with the refinement fields already present in the search options. A new
`HomePersonalizationRepository` combines the persisted baseline with an in-memory session override and
exposes the effective filters to the gallery; the new screen edits the persisted baseline.

## Technical Context

**Language/Version**: Kotlin 2.4.10, JVM 17

**Primary Dependencies**: Jetpack Compose (Material 3), Hilt, Room + Paging 3, Retrofit +
kotlinx.serialization, AndroidX DataStore. No new dependencies.

**Storage**: DataStore Preferences (extend the existing filter store with `sfw`, `color`, `atleast`,
`resolutions`, `ratios`). No Room schema change: the personalized query key produces its own cached
pages through the existing cache tables.

**Testing**: JVM unit tests (JUnit4, MockK, kotlinx-coroutines-test) for the extended filter
model/query/key and the persisted-vs-session merge. Run by `./gradlew test assembleDebug`.

**Target Platform**: Android, minSdk 26, targetSdk/compileSdk 37

**Project Type**: mobile-app (single Android application module `app/`)

**Performance Goals**: changing a setting re-queries the home immediately; paging stays coherent; no
extra network beyond the normal refresh for a new query key.

**Constraints**: existing public behavior preserved for defaults; NSFW still requires the API key; at
least one category stays enabled; EN/IT strings; no secrets.

**Scale/Scope**: one screen, one repository, extended filter model/prefs, drawer wiring, tests.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Layered Architecture & Dependency Direction | Model in `domain/`, persistence in `data/prefs`, merge logic in `data/repository`, UI in `ui/home`; screens use ViewModels/repository only. | PASS |
| II. Room as Single Source of Truth (Offline-First & Paging) | The home still reads from Room through the existing `RemoteMediator`/`PagingSource`; the personalized settings only change the query key. No schema change. | PASS |
| III. Secrets & Privacy by Default | No secrets involved; NSFW gating unchanged. | PASS |
| IV. Localization Completeness | New menu/screen strings added to `values/` and `values-it/`; existing filter labels reused. | PASS |
| V. Verification & Quality Gate | Pure filter/query/key logic and the persisted-vs-session merge are JVM-tested; CI gate. | PASS |

No violations. Complexity Tracking is not required.

**Post-Design Re-Check (after Phase 1)**: Still PASS. The design keeps the home on Room/Paging, adds
no dependency, stores only filter values, sends no secret, localizes new text, and isolates testable
logic in `domain`/`data`.

## Project Structure

### Documentation (this feature)

```text
specs/005-home-personalization/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ui-contracts.md
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
app/src/main/java/dev/giovannidrago/wallhaven/
├── domain/
│   └── WallhavenFilters.kt            # UPDATE: sfw, color, atleast, resolutions, ratios + query/key
├── data/
│   ├── prefs/FilterPreferences.kt     # UPDATE: persist/read the new fields
│   └── repository/HomePersonalizationRepository.kt  # NEW: persisted + session override -> effective
├── ui/
│   ├── navigation/Routes.kt           # UPDATE: HomePersonalizationRoute + AppSection entry
│   ├── components/DrawerContent.kt    # UPDATE: new menu item + label/icon
│   ├── MainScreen.kt                  # UPDATE: route wiring
│   ├── MainViewModel.kt               # UPDATE: effective filters + session quick setters
│   ├── gallery/GalleryViewModel.kt    # UPDATE: gallery driven by effective filters
│   ├── settings/SettingsViewModel.kt  # UPDATE: sorting via the repository (persisted)
│   └── home/
│       ├── HomePersonalizationScreen.kt     # NEW
│       └── HomePersonalizationViewModel.kt  # NEW
├── di/ (existing providers suffice; repository is @Inject)
└── (BuildConfig only where already used)

app/src/main/res/values/strings.xml       # UPDATE: new menu/screen strings (EN)
app/src/main/res/values-it/strings.xml    # UPDATE: matching IT strings

app/src/test/java/dev/giovannidrago/wallhaven/
├── WallhavenFiltersTest.kt            # UPDATE: new fields/query/key
└── HomePersonalizationRepositoryTest.kt # NEW: persisted/session merge
```

**Structure Decision**: Single Android application module. The persisted baseline and the session
override live in one data-layer repository; the UI never computes the merge. The home keeps using the
Room-backed pager, so offline behavior is preserved.

## Complexity Tracking

No constitution violations; this section is intentionally empty.
