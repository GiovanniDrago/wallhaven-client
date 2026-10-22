---

description: "Task list for Drawer Filters, Donation Move and Update Check"
---

# Tasks: Drawer Filters, Donation Move and Update Check

**Input**: Design documents from `/specs/004-drawer-settings-update/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included. Constitution Principle V requires JVM verification, so the pure version
comparison and the update repository are covered by unit tests.

**Organization**: Tasks are grouped by user story (US1 filter group, US2 donation move, US3 update
check). US1 and US3 are P1; US2 is P2.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3) on user-story phase tasks only
- Every task includes an exact file path

## Path Conventions

Single Android application module: app code under
`app/src/main/java/dev/giovannidrago/wallhaven/`, tests under
`app/src/test/java/dev/giovannidrago/wallhaven/`, resources under `app/src/main/res/`.

---

## Phase 1: Setup (Shared Infrastructure)

- [X] T001 [P] Add the new English strings to `app/src/main/res/values/strings.xml` (filters expand/collapse content descriptions, check for updates, update available title, later, download, no updates, update check error, download error)
- [X] T002 [P] Add the same keys with Italian translations to `app/src/main/res/values-it/strings.xml`
- [X] T003 Verify no new Gradle dependencies are needed: keep `app/build.gradle.kts` and `gradle/libs.versions.toml` unchanged (Retrofit, kotlinx.serialization, OkHttp and DataStore are already present)

---

## Phase 2: User Story 1 - Collapsible filter group in the drawer (Priority: P1) 🎯 MVP

**Goal**: The drawer shows a single "Filters" entry that expands/collapses the existing toggles.

**Independent Test**: Open the drawer, confirm only "Filters" is visible, tap to reveal the toggles,
change one, collapse, and confirm the value is applied.

- [X] T004 [US1] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/MainScreen.kt` hoist a `filtersExpanded` state, reset it to `false` when the drawer closes, and pass it plus `onToggleFilters` to `DrawerContent`
- [X] T005 [US1] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/DrawerContent.kt` replace the flat filter list with a single clickable "Filters" header (with an expand/collapse indicator) that shows the existing toggles only when expanded; keep the toggles' behaviour unchanged

**Checkpoint**: US1 works independently.

---

## Phase 3: User Story 2 - Donation entry lives only in Settings (Priority: P2)

**Goal**: "Buy me a coffee" disappears from the drawer and remains in Settings.

**Independent Test**: Open the drawer (no donation entry); open Settings (donation button works).

- [X] T006 [US2] Remove the donation item and the `onDonate` parameter from `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/DrawerContent.kt` and `app/src/main/java/dev/giovannidrago/wallhaven/ui/MainScreen.kt`, and delete the unused `drawer_donate` key from `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml`

**Checkpoint**: US1 and US2 both work independently.

---

## Phase 4: User Story 3 - Check for a new version from Settings (Priority: P1)

**Goal**: Settings shows the installed version and a "Check for updates" action; a silent check runs
at most once per day on app start; a newer release offers a Later/Download dialog.

**Independent Test**: Tap "Check for updates" with a newer release (dialog), with none (message), and
offline (error); confirm Download opens the APK asset.

### Tests for User Story 3

- [X] T007 [P] [US3] Write `app/src/test/java/dev/giovannidrago/wallhaven/VersionComparisonTest.kt`: tag extraction (`personal-WallhavenClient-0.5.0-16` → `0.5.0`), leading `v`, build metadata, missing components, `1.10.0 > 1.9.9`, and non-numeric tags
- [X] T008 [P] [US3] Write `app/src/test/java/dev/giovannidrago/wallhaven/UpdateRepositoryTest.kt` with OkHttp MockWebServer: newer/older version, asset selection (prefers `WallhavenClient-latest.apk`), release-page fallback when no APK, non-200 error, and the once-per-day throttle (MockK for preferences)

### Implementation for User Story 3

- [X] T009 [P] [US3] Create `app/src/main/java/dev/giovannidrago/wallhaven/domain/VersionComparison.kt` with pure `extractVersionFromTag(tag)` (first `\d+(\.\d+){0,2}` group) and `isNewerVersion(latest, current)` (strip leading `v`, drop `+metadata`, numeric major/minor/patch, missing = 0)
- [X] T010 [P] [US3] Create `app/src/main/java/dev/giovannidrago/wallhaven/data/api/dto/GithubReleaseDto.kt` with `@Serializable` fields `tag_name`, `html_url` and `assets[]` (`name`, `browser_download_url`)
- [X] T011 [P] [US3] Create `app/src/main/java/dev/giovannidrago/wallhaven/data/api/GithubApi.kt` with `GET repos/GiovanniDrago/wallhaven-client/releases/latest` and `Accept`/`User-Agent` headers (no auth)
- [X] T012 [P] [US3] Create `app/src/main/java/dev/giovannidrago/wallhaven/data/prefs/UpdatePreferences.kt` storing `last_update_check` as `yyyy-MM-dd` in the existing DataStore
- [X] T013 [US3] Create `app/src/main/java/dev/giovannidrago/wallhaven/data/repository/UpdateRepository.kt`: `check(currentVersion)` returning `UpToDate`/`UpdateAvailable(version, downloadUrl)`/`Error`, with APK asset selection (`WallhavenClient-latest.apk` → first `.apk` → `html_url`) and silent-check throttling helpers (depends on T009–T012)
- [X] T014 [US3] Update `app/src/main/java/dev/giovannidrago/wallhaven/di/NetworkModule.kt` (provide `GithubApi` for `https://api.github.com/` with `User-Agent`) and `app/src/main/java/dev/giovannidrago/wallhaven/di/AppModule.kt` (provide `currentVersion` from `BuildConfig.VERSION_NAME`)
- [X] T015 [P] [US3] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/UpdateAvailableDialog.kt` with the new version and "Later"/"Download" actions
- [X] T016 [US3] Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/settings/SettingsViewModel.kt` (manual check state/events) and `app/src/main/java/dev/giovannidrago/wallhaven/ui/settings/SettingsScreen.kt` (installed version, "Check for updates", dialog, up-to-date/error snackbars)
- [X] T017 [US3] Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/MainViewModel.kt` (once-per-day silent check + one-shot update event) and `app/src/main/java/dev/giovannidrago/wallhaven/ui/MainScreen.kt` (show `UpdateAvailableDialog` for the silent result)

**Checkpoint**: All three user stories are independently functional.

---

## Phase 5: Polish & Cross-Cutting Concerns

- [X] T018 [P] Verify string parity between `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml` and that no hardcoded user-visible text remains (Principle IV)
- [ ] T019 Run the quality gate `./gradlew test assembleDebug` (CI `Test` workflow) and execute the manual scenarios D1–D5 in `specs/004-drawer-settings-update/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **US1 (Phase 2)**: Depends on Setup; edits `MainScreen.kt` and `DrawerContent.kt`.
- **US2 (Phase 3)**: Depends on US1 (same two files) and Setup.
- **US3 (Phase 4)**: Depends on Setup; the data layer (T009–T014) is independent of the drawer.
- **Polish (Phase 5)**: Depends on all stories.

### Within US3

- T007/T008 (tests) target the math and repository; T009–T012 create the pieces; T013 assembles the
  repository; T014 wires DI; T015 the dialog; T016 Settings; T017 MainScreen/MainViewModel.

### Parallel Opportunities

- T001/T002 (locale files) and T003 are independent.
- T007/T008 (different test files) and T009–T012, T015 (different source files) can run in parallel.
- The drawer work (US1/US2) and the update data layer can proceed in parallel by different workers.

---

## Parallel Example: US3 data layer

```bash
Task: "Create domain/VersionComparison.kt"
Task: "Create data/api/dto/GithubReleaseDto.kt"
Task: "Create data/api/GithubApi.kt"
Task: "Create data/prefs/UpdatePreferences.kt"
Task: "Create ui/components/UpdateAvailableDialog.kt"
```

---

## Implementation Strategy

### MVP First

1. Setup + US1 (collapsible filters).
2. US2 (donation move).
3. US3 (update check): data layer + tests, then Settings and app-start wiring.
4. Polish: localization check and the quality gate.

### Incremental Delivery

1. Setup → strings ready.
2. US1 → collapsible Filters (MVP).
3. US2 → donation only in Settings.
4. US3 → update check + dialog.
5. Polish → EN/IT parity and CI.

---

## Notes

- [P] tasks touch different files and have no incomplete dependencies.
- No new dependencies; the update check uses the existing Retrofit/OkHttp/serialization/DataStore.
- No secrets are sent or logged by the update check (Principle III).
- Release tags are `personal-WallhavenClient-<version>-<run>`; the version is extracted from the tag,
  and a build is only newer when `versionName` is bumped.
- Commit after each task or logical group.
