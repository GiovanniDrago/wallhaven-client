# Quickstart Validation: Light and Dark Theme Presets

This guide proves the feature end-to-end. It contains no implementation code; details live in
[spec.md](./spec.md), [plan.md](./plan.md), [data-model.md](./data-model.md) and
[contracts/ui-contracts.md](./contracts/ui-contracts.md).

## Prerequisites

- JDK 17. No local Android SDK is required; builds and tests run in CI.
- For manual checks, install the debug/personal APK.

## Automated Verification (quality gate)

```bash
./gradlew test assembleDebug
```

New tests:

- `ThemePresetTest` — at least 2 light and 2 dark presets; WCAG contrast computed from the sRGB
  channels: `onBackground/background` and `onSurface/surface` ≥ 4.5:1, primary/onPrimary ≥ 3:1.
- `ThemeSettingsTest` — `AppearanceMode` resolution (`SYSTEM`/`LIGHT`/`DARK`), defaults, and unknown
  preset-id fallback to the default presets.

## Manual Scenarios

### T1 — Light presets (US1, FR-001/FR-002/FR-003)

1. Open Settings → Appearance.
2. Select the first light preset, then the second: the whole app (drawer, home, search, detail,
   settings, dialogs) changes immediately.
3. Leave and reopen the app: the selected light preset is still active.

### T2 — Dark presets (US2)

1. Select each dark preset in turn: the app switches immediately.
2. Restart the app: the selected dark preset is still active.

### T3 — Appearance mode (US3, FR-005/FR-006)

1. Select "Follow system" and change the device light/dark setting: the app follows it live.
2. Force "Light", then "Dark": the app ignores the device setting.
3. Restart: the forced mode is preserved.

### T4 — Coverage and contrast (FR-007/FR-008)

1. In each preset, check the drawer, gallery, search, album screens, detail, settings, a dialog, a
   snackbar and the full-screen viewer: all use the preset colors.
2. Confirm readable body text and icons on every surface, and that status/navigation bar icons are
   legible in forced light and dark.

## Expected Outcomes

- At least two light and two dark presets are selectable.
- The selected theme applies to the whole app within a second, without a restart.
- The selection survives restarts; unknown persisted values fall back safely.
- Every preset passes the contrast checks; all new text is translated in English and Italian.
