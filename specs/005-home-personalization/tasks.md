---

description: "Task list for Home Personalization"
---

# Tasks: Home Personalization

**Input**: Design documents from `/specs/005-home-personalization/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included. Constitution Principle V and the AGENTS coverage baseline require JVM tests for
the filter model and the persisted/session merge.

**Organization**: Tasks are grouped by user story (US1 screen, US2 home feed, US3 session overrides).
US1 and US2 are P1; US3 is P2.

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

- [X] T001 [P] Add the new English strings (menu entry, screen title, screen description, reset) to `app/src/main/res/values/strings.xml`
- [X] T002 [P] Add the same keys with Italian translations to `app/src/main/res/values-it/strings.xml`
- [X] T003 Verify no new Gradle dependencies are needed: keep `app/build.gradle.kts` and `gradle/libs.versions.toml` unchanged (DataStore and the existing stack suffice)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Extended model, persistence and the persisted-vs-session repository shared by every story.

- [X] T004 [P] In `app/src/main/java/dev/giovannidrago/wallhaven/domain/WallhavenFilters.kt` add `sfw: Boolean = true`, `color: String? = null`, `atleast: String? = null`, `resolutions: List<String> = emptyList()`, `ratios: List<String> = emptyList()`; update `purity`/`purityValues`/`purityCount` to use `sfw`, keep the default `key` unchanged and append refinement segments only when non-default, and fill `color`/`atleast`/`resolutions`/`ratios` in `toQuery()` (data-model.md)
- [X] T005 [P] Extend `app/src/test/java/dev/giovannidrago/wallhaven/WallhavenFiltersTest.kt` with the new fields, the sfw/sketchy/nsfw mask, the default-vs-personalized key, and `toQuery()` refinement values
- [X] T006 In `app/src/main/java/dev/giovannidrago/wallhaven/data/prefs/FilterPreferences.kt` persist/read `home_sfw`, `home_color`, `home_atleast`, `home_resolutions`, `home_ratios` with the documented defaults, removing keys when a value is cleared
- [X] T007 Create `app/src/main/java/dev/giovannidrago/wallhaven/data/repository/HomePersonalizationRepository.kt` with `persisted: Flow<WallhavenFilters>`, an in-memory `sessionOverride`, `effective: Flow<WallhavenFilters>` (override wins on sorting/categories/sketchy/nsfw only), session quick setters, `updatePersisted(transform)` that clears the affected override, and `reset()`
- [X] T008 [P] Create `app/src/test/java/dev/giovannidrago/wallhaven/HomePersonalizationRepositoryTest.kt` (MockK for `FilterPreferences`): effective merge, session precedence, baseline edit clearing the override, reset, and a new instance losing the override

**Checkpoint**: Model, persistence and merge logic ready; all stories can proceed.

---

## Phase 3: User Story 1 - Open and edit the Home personalization settings (Priority: P1) 🎯 MVP

**Goal**: A drawer entry opens a screen with the full home filter set and a reset action; changes are
persisted.

**Independent Test**: Open the drawer, open Home personalization, change values, leave and reopen; the
values are kept; reset restores defaults.

- [X] T009 [US1] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/navigation/Routes.kt` add `HomePersonalizationRoute` and an `AppSection.HOME_PERSONALIZATION` entry
- [X] T010 [US1] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/DrawerContent.kt` add the "Home personalization" item (label/icon mappings)
- [X] T011 [US1] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/MainScreen.kt` wire `HomePersonalizationRoute` to the screen with the drawer menu button
- [X] T012 [US1] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/home/HomePersonalizationViewModel.kt` exposing the persisted options and persisting each change (sorting, categories, purity, color, resolution, ratios) plus reset
- [X] T013 [US1] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/home/HomePersonalizationScreen.kt` with the control groups from contracts/ui-contracts.md (sorting, categories, purity, color, resolution, format, reset), reusing existing filter labels

**Checkpoint**: US1 works independently.

---

## Phase 4: User Story 2 - The home always uses the personalization (Priority: P1)

**Goal**: The gallery is driven by the effective settings on every load/refresh, defaults reproduce
today's behavior.

**Independent Test**: Save a personalization, return to the home and refresh; the feed matches it; the
defaults match the current app.

- [X] T014 [US2] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/gallery/GalleryViewModel.kt` drive the pager from `HomePersonalizationRepository.effective` instead of `FilterPreferences.filters`
- [X] T015 [US2] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/MainViewModel.kt` expose the effective filters for the drawer display
- [X] T016 [US2] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/settings/SettingsViewModel.kt` route the default-sorting change through `HomePersonalizationRepository.updatePersisted` and show the persisted value

**Checkpoint**: US1 and US2 both work; the home follows the personalization.

---

## Phase 5: User Story 3 - Quick drawer filters are session overrides (Priority: P2)

**Goal**: Drawer quick changes apply only for the session and win over the persisted baseline; a
restart returns to the baseline.

**Independent Test**: Change a quick filter, confirm the home follows it; restart, confirm the home and
the quick filters return to the saved personalization.

- [X] T017 [US3] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/MainViewModel.kt` route `setSorting`, `setSketchy`, `setNsfw` and `setCategory` to the repository's session setters (not `FilterPreferences.update`)
- [X] T018 [US3] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/home/HomePersonalizationViewModel.kt` ensure each persisted edit clears the corresponding session override via the repository, so the quick filters realign

**Checkpoint**: All three user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T019 [P] Verify string parity between `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml` and that no hardcoded user-visible text remains (Principle IV)
- [X] T020 Run the quality gate `./gradlew test assembleDebug` (CI `Test` workflow) and execute the manual scenarios H1–H5 in `specs/005-home-personalization/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup; blocks all stories.
- **US1 (Phase 3)**: Depends on Foundational.
- **US2 (Phase 4)**: Depends on Foundational and the US1 screen (the settings must exist before the
  home can be driven by them).
- **US3 (Phase 5)**: Depends on US2 (the drawer display uses the effective filters).
- **Polish (Phase 6)**: Depends on all stories.

### Within the Work

- Model (`T004`) before persistence (`T006`) and the repository (`T007`).
- Repository before the gallery/drawer wiring (`T014`/`T015`) and the screen ViewModel (`T012`).
- Routes/drawer/MainScreen (`T009`–`T011`) before the screen is reachable.

### Parallel Opportunities

- T001/T002 (locale files) and T003 are independent.
- T004 and T005 (model + test) are different files; T008 (repository test) is independent of the UI.
- The screen files (T012/T013) and the home wiring (T014/T015) touch different files.

---

## Parallel Example: Foundational

```bash
Task: "Extend domain/WallhavenFilters.kt"
Task: "Extend WallhavenFiltersTest.kt"
Task: "Create HomePersonalizationRepositoryTest.kt"
```

---

## Implementation Strategy

### MVP First

1. Setup + Foundational.
2. US1 (screen) → the user can configure and persist.
3. US2 (home) → the home applies the configuration.
4. US3 (session overrides) → quick filters behave as clarified.
5. Polish → localization parity and the quality gate.

---

## Notes

- [P] tasks touch different files and have no incomplete dependencies.
- No new dependencies, no Room schema change; the home stays on Room/Paging (offline preserved).
- The default query key is unchanged; only personalized configurations get new cache keys.
- Quick-filter overrides are intentionally not persisted (session only).
- Commit after each task or logical group, only with explicit approval (AGENTS.md).
