# UI Contracts: Light and Dark Theme Presets

## Settings — Appearance section

- A new **Appearance** section is shown in Settings (near the existing About/sorting sections).
- **Mode**: three selectable options — "Follow system", "Light", "Dark". Exactly one is active.
- **Light theme**: at least two selectable presets, shown with their names.
- **Dark theme**: at least two selectable presets, shown with their names.
- Changing any option applies immediately; no confirmation button and no restart.
- The current selection is visibly highlighted.
- All labels exist in both English and Italian.

## Theme application contract

`WallhavenTheme(colorScheme: ColorScheme, darkTheme: Boolean, content: @Composable () -> Unit)`

- Applies the given Material 3 `ColorScheme` to the whole composition.
- Sets the status/navigation bar icon appearance from `darkTheme` (light icons in dark mode, dark icons
  in light mode).
- `MainActivity` obtains the effective `(colorScheme, darkTheme)` from `ThemeViewModel` and wraps
  `MainScreen`.
- Every screen, dialog, snackbar and the full-screen viewer inherits the theme; no screen overrides
  colors.

## Preset contract

- Presets have stable ids (`default_light`, `latte`, `default_dark`, `mocha`, …), a localized label and
  a `ColorScheme`.
- At least two light and two dark presets exist.
- Unknown ids resolve to `default_light` / `default_dark`.
- Every preset satisfies WCAG AA contrast for body text (≥ 4.5:1) and large text/icons (≥ 3:1).

## Persistence contract

`ThemePreferences` persists:

- `appearance_mode` (`SYSTEM` default),
- `theme_light_preset` (`default_light` default),
- `theme_dark_preset` (`default_dark` default).

`ThemeViewModel` exposes the effective theme and reacts to every stored change.

## System behavior

- `SYSTEM` follows the device setting and reacts to a live change.
- `LIGHT`/`DARK` ignore the device setting.
- No crash on unknown persisted values.

## Localization

All new user-visible text (section title, mode labels, preset names/descriptions) exists in
`values/` and `values-it/`.
