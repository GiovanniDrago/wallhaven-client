# Implementation Plan: Light and Dark Theme Presets

**Branch**: `006-theme-presets` | **Date**: `2026-10-23` | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/006-theme-presets/spec.md`

## Summary

Add an "Appearance" section to Settings with at least two light and two dark theme presets, an
appearance mode (Follow system / Light / Dark), and local persistence. The selected preset is applied
to the whole app through the existing `WallhavenTheme`, including dialogs and system bars. Presets are
built on the Material 3 color-scheme roles, with the Material 3 baseline as the default and modern
palettes (Catppuccin Latte for light, Mocha for dark) as the additional choices.

Because dynamic color (Android 12+) would override the selected preset, it is disabled in favour of
the explicit presets; this is the only intentional visual behavior change.

## Technical Context

**Language/Version**: Kotlin 2.4.10, JVM 17

**Primary Dependencies**: Jetpack Compose (Material 3), Hilt, AndroidX DataStore, core-ktx
(`WindowCompat`). No new dependencies.

**Storage**: DataStore Preferences (new keys: appearance mode, light preset id, dark preset id). No
Room changes.

**Testing**: JVM unit tests (JUnit4) for WCAG contrast of every preset, appearance-mode resolution and
unknown-id fallback. Run by `./gradlew test assembleDebug`.

**Target Platform**: Android, minSdk 26, targetSdk/compileSdk 37

**Performance Goals**: applying a theme is immediate (recomposition), with no restart and no visible
flicker beyond normal recomposition.

**Constraints**: presets must meet WCAG AA contrast; all strings EN/IT; no secrets; system bars follow
the effective theme; defaults preserve the current appearance.

**Scale/Scope**: one Appearance section, a persisted settings model, preset definitions, theme
application at the activity root, tests.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Layered Architecture & Dependency Direction | Theme definitions and composition in `ui/theme`; persistence in `data/prefs`; mode/settings model in `domain`; Settings accesses them through its ViewModel. | PASS |
| II. Room as Single Source of Truth (Offline-First & Paging) | Not affected: appearance lives in DataStore; no wallpaper data or paging change. | PASS |
| III. Secrets & Privacy by Default | No secrets involved. | PASS |
| IV. Localization Completeness | New section labels, mode labels and preset descriptions added to `values/` and `values-it/`. | PASS |
| V. Verification & Quality Gate | Preset contrast, mode resolution and fallback are JVM-tested; CI gate. | PASS |

No violations. Complexity Tracking is not required.

**Post-Design Re-Check (after Phase 1)**: Still PASS. The design keeps the theme in the UI layer,
persists only identifiers in DataStore, sends no secret, localizes new text and keeps the testable
color/mode logic in pure code.

## Project Structure

### Documentation (this feature)

```text
specs/006-theme-presets/
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
├── domain/ThemeSettings.kt          # NEW: AppearanceMode + ThemeSettings + isDark resolution
├── data/prefs/ThemePreferences.kt   # NEW: DataStore mode + preset ids
├── ui/theme/ThemePresets.kt         # NEW: light/dark preset definitions + id resolution
├── ui/theme/Theme.kt                # UPDATE: colorScheme-driven theme + system bar appearance
├── ui/theme/ThemeViewModel.kt       # NEW: combines preferences into the effective theme
├── MainActivity.kt                  # UPDATE: apply the effective theme at the root
├── ui/settings/SettingsViewModel.kt # UPDATE: expose/set appearance settings
└── ui/settings/SettingsScreen.kt    # UPDATE: Appearance section (mode + presets)

app/src/main/res/values/strings.xml       # UPDATE: appearance strings (EN)
app/src/main/res/values-it/strings.xml    # UPDATE: matching IT strings

app/src/test/java/dev/giovannidrago/wallhaven/
├── ThemePresetTest.kt               # NEW: WCAG contrast for every preset
└── ThemeSettingsTest.kt             # NEW: mode resolution + fallback
```

**Structure Decision**: Single Android application module. The theme is applied at the activity root
via a Hilt `ThemeViewModel`, so every screen (and dialogs) inherits it. Preset identifiers (not colors)
are persisted, so theme definitions can evolve without a migration.

## Complexity Tracking

No constitution violations; this section is intentionally empty.
