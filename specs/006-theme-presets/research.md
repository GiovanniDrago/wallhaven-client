# Phase 0 Research: Light and Dark Theme Presets

## R1. Modern palette references

- **Decision**: Use the Material 3 baseline color roles as the default light/dark presets and add
  **Catppuccin Latte** (light) and **Catppuccin Mocha** (dark) as the modern alternatives. The colors
  are mapped onto the Material 3 `ColorScheme` roles (background, surface, primary, secondary,
  tertiary, error, on-*).
- **Rationale**: The user asked for modern examples. Material 3 is the app's design system; Catppuccin
  is a widely used modern open-source palette with explicit light/dark flavors. Sources consulted:
  `material-foundation/material-color-utilities` (Material 3 tonal palettes / Theme Builder) and the
  Catppuccin README palette tables (Latte base `#EFF1F5`, text `#4C4F69`; Mocha base `#1E1E2E`, text
  `#CDD6F4`, among others).
- **Alternatives considered**: a single generic palette (rejected: the request asks for at least two
  per type); only dynamic/system colors (rejected: not deterministic presets).

## R2. Disable dynamic color

- **Decision**: Build the theme from the selected preset `ColorScheme` and stop using
  `dynamicLightColorScheme`/`dynamicDarkColorScheme` (the current `dynamicColor = true` default).
- **Rationale**: Dynamic color would replace the chosen preset on Android 12+, making the new options
  invisible. Explicit presets are the feature.
- **Alternatives considered**: exposing dynamic color as a third option (rejected: extra scope; can be
  added later).

## R3. Settings model

- **Decision**: `AppearanceMode` (`SYSTEM`, `LIGHT`, `DARK`) and `ThemeSettings` (mode + light preset
  id + dark preset id) live in `domain/`. Preset identifiers are stable strings.
- **Rationale**: The mode resolution (`isDark(systemDark)`) is pure and testable; DataStore stores only
  identifiers, so theme colors can be tuned without migrating persisted data.
- **Alternatives considered**: storing a serialized full theme (rejected: brittle and unnecessary).

## R4. Preset definitions

- **Decision**: `ui/theme/ThemePresets.kt` defines at least two light and two dark `ThemePreset`s, each
  with an id, a label resource and a Material 3 `ColorScheme`. Resolution helpers return the default
  when an id is unknown.
- **Rationale**: The presets need Compose color types, so they belong in the UI theme layer. Stable ids
  keep persistence decoupled from the color objects.
- **Alternatives considered**: putting `ColorScheme` in `domain` (rejected: it would couple the domain
  to the Compose UI).

## R5. Application at the activity root

- **Decision**: A Hilt `ThemeViewModel` combines `ThemePreferences` and `AppearanceMode` into an
  effective `(colorScheme, darkTheme)` state. `MainActivity` collects it and passes it to
  `WallhavenTheme`, which wraps `MainScreen`.
- **Rationale**: Applying the theme once at the root makes every screen, dialog, snackbar and the
  full-screen viewer inherit it; no per-screen changes.
- **Alternatives considered**: applying the theme inside `MainScreen` (same effect but hides the
  activity wiring); a CompositionLocal (unnecessary).

## R6. System bars

- **Decision**: In `WallhavenTheme`, use `WindowCompat.getInsetsController(...)` to set
  `isAppearanceLightStatusBars`/`isAppearanceLightNavigationBars` from the effective dark flag.
- **Rationale**: `enableEdgeToEdge()` defaults to the system mode; forced light/dark would otherwise
  leave unreadable bar icons.
- **Alternatives considered**: relying on the system only (rejected: wrong icons in forced modes).

## R7. Settings UI

- **Decision**: Add an "Appearance" section to Settings with mode chips (Follow system / Light / Dark)
  and two groups of preset chips (light presets, dark presets). `SettingsViewModel` exposes the saved
  settings and setters backed by `ThemePreferences`.
- **Rationale**: Matches the existing settings style and keeps persistence in the ViewModel.
- **Alternatives considered**: a separate screen (rejected: the request says "in Settings").

## R8. Contrast verification

- **Decision**: A JVM test computes WCAG relative luminance and contrast for `onBackground/background`
  and `onSurface/surface` of every preset, asserting at least 4.5:1 (and 3:1 for primary/onPrimary).
  Presets are tuned until they pass.
- **Rationale**: The spec requires accessible contrast and the constitution requires JVM tests.
- **Alternatives considered**: manual visual checks only (rejected: not measurable).

## R9. Fallback

- **Decision**: Unknown persisted ids resolve to the default light/dark presets; an unknown mode
  resolves to `SYSTEM`. No crash.
- **Rationale**: FR-010.
- **Alternatives considered**: clearing preferences on unknown values (rejected: destructive).

## R10. Window background

- **Decision**: Keep the existing `themes.xml` window background; the in-app preset is applied once
  Compose draws.
- **Rationale**: The startup window background is a platform splash concern; changing it per forced
  preset is not reliable. It is a minor, documented limitation.
- **Alternatives considered**: `values-night/themes.xml` (only helps the system mode, not forced
  presets); a transparent window (can cause a white flash).
