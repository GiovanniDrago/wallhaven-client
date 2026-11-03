# Phase 1 Data Model: Light and Dark Theme Presets

No Room schema changes. The appearance configuration is a small persisted preference plus in-code
theme preset definitions.

## Domain model

### AppearanceMode

| Value | Meaning |
|-------|---------|
| `SYSTEM` | Follow the device light/dark setting (default). |
| `LIGHT` | Always use the selected light preset. |
| `DARK` | Always use the selected dark preset. |

### ThemeSettings

| Field | Type | Default | Notes |
|-------|------|---------|-------|
| `mode` | `AppearanceMode` | `SYSTEM` | Appearance mode |
| `lightPresetId` | `String` | `default_light` | Stable preset identifier |
| `darkPresetId` | `String` | `default_dark` | Stable preset identifier |

Derived: `isDark(systemDark: Boolean): Boolean` — `true` for `DARK`, `false` for `LIGHT`, otherwise
`systemDark`.

### ThemePreset (UI theme layer)

| Field | Type | Notes |
|-------|------|-------|
| `id` | `String` | Stable identifier (e.g. `default_light`, `latte`, `default_dark`, `mocha`) |
| `labelRes` | Int | Localized display name |
| `dark` | Boolean | Whether it is a dark preset |
| `colorScheme` | Material 3 `ColorScheme` | Color roles used by the app |

Sets: `LIGHT_PRESETS` (at least 2) and `DARK_PRESETS` (at least 2). Resolution helpers return the
default when the id is unknown.

## Persistence (DataStore)

| Key | Type | Default |
|-----|------|---------|
| `appearance_mode` | `String` | `SYSTEM` |
| `theme_light_preset` | `String` | `default_light` |
| `theme_dark_preset` | `String` | `default_dark` |

## Resolution rules

| Rule | Source |
|------|--------|
| Unknown mode → `SYSTEM` | FR-010 |
| Unknown light preset id → `default_light` | FR-010 |
| Unknown dark preset id → `default_dark` | FR-010 |
| `SYSTEM` uses `isSystemInDarkTheme()` at the activity root | FR-006 |
| Forced mode ignores the system setting | FR-005 |
| Preset change applies immediately (state flow) | FR-003 |

## State transitions

```text
Default: SYSTEM + default light + default dark
  │ user selects a preset / mode
  ▼
DataStore updated ──► ThemeViewModel emits the effective (dark, ColorScheme)
  │
  ├─ SYSTEM: dark = device dark; scheme = dark/light preset accordingly
  ├─ LIGHT: dark = false; scheme = light preset
  └─ DARK: dark = true; scheme = dark preset

App restart ──► persisted values are read back
Unknown value ──► default for that field (no crash)
```

## Validation rules

| Rule | Source |
|------|--------|
| At least 2 light and 2 dark presets | FR-002 / SC-001 |
| Body text contrast ≥ 4.5:1, large text/icons ≥ 3:1 | FR-007 / SC-004 |
| Theme covers all screens, dialogs and system bars | FR-008 |
| All new text EN/IT | FR-011 |
| Defaults preserve the current appearance | FR-012 |
