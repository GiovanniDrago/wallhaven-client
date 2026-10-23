---

description: "Task list for Light and Dark Theme Presets"
---

# Tasks: Light and Dark Theme Presets

**Input**: Design documents from `/specs/006-theme-presets/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included. Constitution Principle V requires JVM verification, so preset contrast and mode
resolution are covered by unit tests.

**Organization**: Shared theme infrastructure is foundational; the three user stories add the light
presets, the dark presets and the appearance mode in Settings.

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

- [x] T001 [P] Add the English appearance strings (section title, mode labels, preset names/descriptions) to `app/src/main/res/values/strings.xml`
- [x] T002 [P] Add the same keys with Italian translations to `app/src/main/res/values-it/strings.xml`
- [x] T003 Verify no new Gradle dependencies are needed: keep `app/build.gradle.kts` and `gradle/libs.versions.toml` unchanged (Compose, DataStore and core-ktx already present)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Persisted settings model, theme presets, theme application and the Settings ViewModel
wiring shared by every story.

- [x] T004 [P] Create `app/src/main/java/dev/giovannidrago/wallhaven/domain/ThemeSettings.kt` with `AppearanceMode` (`SYSTEM`, `LIGHT`, `DARK`), `ThemeSettings` (mode + light/dark preset ids, defaults `SYSTEM`, `default_light`, `default_dark`) and `isDark(systemDark)`
- [x] T005 [P] Create `app/src/main/java/dev/giovannidrago/wallhaven/data/prefs/ThemePreferences.kt` persisting `appearance_mode`, `theme_light_preset`, `theme_dark_preset` in the existing DataStore, with unknown values resolving to the defaults
- [x] T006 [P] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/theme/ThemePresets.kt` with at least two light (`default_light`, `latte`) and two dark (`default_dark`, `mocha`) presets, each with a stable id, a label resource and a Material 3 `ColorScheme`; add id resolution helpers that fall back to the defaults. Use Catppuccin Latte (base `#EFF1F5`, text `#4C4F69`, primary `#1E66F5`, secondary `#8839EF`, tertiary `#179299`) and Mocha (base `#1E1E2E`, text `#CDD6F4`, primary `#89B4FA`, secondary `#CBA6F7`, tertiary `#94E2D5`) for the extra presets
- [x] T007 Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/theme/Theme.kt` to take a `ColorScheme` plus `darkTheme`, apply it via `MaterialTheme`, stop using dynamic color, and set the status/navigation bar icon appearance with `WindowCompat`
- [x] T008 Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/theme/ThemeViewModel.kt` combining `ThemePreferences` with `isSystemInDarkTheme()` into the effective `(colorScheme, darkTheme)` state
- [x] T009 Update `app/src/main/java/dev/giovannidrago/wallhaven/MainActivity.kt` to collect `ThemeViewModel` and pass the effective theme to `WallhavenTheme`
- [x] T010 Update `app/src/main/java/dev/giovannidrago/wallhaven/ui/settings/SettingsViewModel.kt` to expose the saved `ThemeSettings` and setters for the mode and both preset ids via `ThemePreferences`
- [x] T011 [P] Create `app/src/test/java/dev/giovannidrago/wallhaven/ThemePresetTest.kt`: at least 2 light and 2 dark presets; WCAG contrast computed from sRGB channels (`onBackground/background`, `onSurface/surface` ≥ 4.5:1; primary/onPrimary ≥ 3:1)

**Checkpoint**: Theme plumbing ready; all stories can proceed.

---

## Phase 3: User Story 1 - Choose a light theme (Priority: P1) 🎯 MVP

**Goal**: An Appearance section in Settings lists at least two light presets and applies the chosen
one immediately.

**Independent Test**: Open Settings, select each light preset, confirm the app changes immediately and
keeps the selection after a restart.

- [x] T012 [US1] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/settings/SettingsScreen.kt` add the Appearance section with the light preset group (selectable chips showing each preset name); selecting one calls the ViewModel setter

**Checkpoint**: US1 works independently.

---

## Phase 4: User Story 2 - Choose a dark theme (Priority: P1)

**Goal**: The same Appearance section lists at least two dark presets and applies the chosen one
immediately.

**Independent Test**: Select each dark preset, confirm the app changes immediately and keeps the
selection after a restart.

- [x] T013 [US2] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/settings/SettingsScreen.kt` add the dark preset group to the Appearance section (selectable chips, same pattern as the light group)

**Checkpoint**: US1 and US2 both work.

---

## Phase 5: User Story 3 - Follow the system or force light/dark (Priority: P2)

**Goal**: The user can choose Follow system / Light / Dark; Follow system uses the selected light or
dark preset and reacts to live device changes.

**Independent Test**: Set Follow system, change the device appearance, confirm the app switches; force
Light/Dark and confirm the device setting is ignored; restart and confirm persistence.

- [x] T014 [US3] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/settings/SettingsScreen.kt` add the mode chips (Follow system, Light, Dark) to the Appearance section and wire them to the ViewModel
- [x] T015 [P] [US3] Create `app/src/test/java/dev/giovannidrago/wallhaven/ThemeSettingsTest.kt`: `isDark` resolution for `SYSTEM`/`LIGHT`/`DARK`, defaults, and unknown preset-id fallback to the default light/dark presets

**Checkpoint**: All three user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [x] T016 [P] Verify string parity between `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml` and that no hardcoded user-visible text remains (Principle IV)
- [ ] T017 Run the quality gate (`./gradlew test assembleDebug` in CI) and execute the manual scenarios T1–T4 in `specs/006-theme-presets/quickstart.md` — **richiede approvazione esplicita per push/CI**

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup; blocks all stories.
- **US1 (Phase 3)**: Depends on Foundational (especially T010 and T012's setter).
- **US2 (Phase 4)**: Depends on US1 (same Appearance section in `SettingsScreen.kt`).
- **US3 (Phase 5)**: Depends on US2 (same section) and Foundational.
- **Polish (Phase 6)**: Depends on all stories.

### Within the Work

- Domain/prefs/presets (`T004`–`T006`) before the theme application (`T007`–`T009`).
- Theme application before the Settings UI.
- `SettingsScreen.kt` tasks (T012–T014) touch the same file and MUST run sequentially.

### Parallel Opportunities

- T001/T002 (locale files) and T003 are independent.
- T004, T005, T006 are different files; T011 (tests) is independent of the UI.
- T015 is independent of the screen edits.

---

## Parallel Example: Foundational

```bash
Task: "Create domain/ThemeSettings.kt"
Task: "Create data/prefs/ThemePreferences.kt"
Task: "Create ui/theme/ThemePresets.kt"
Task: "Create ThemePresetTest.kt"
```

---

## Implementation Strategy

### MVP First

1. Setup + Foundational (theme plumbing + light presets data).
2. US1 (light presets in Settings).
3. US2 (dark presets).
4. US3 (appearance mode).
5. Polish (localization + quality gate).

---

## Notes

- [P] tasks touch different files and have no incomplete dependencies.
- No new dependencies, no Room changes; only identifiers are persisted.
- Dynamic color is intentionally disabled so the selected preset is visible on Android 12+.
- The startup window background stays platform-level; the in-app theme applies once Compose draws.
- Commit after each task or logical group, only with explicit approval (AGENTS.md).
