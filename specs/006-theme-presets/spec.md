# Feature Specification: Light and Dark Theme Presets

**Feature Branch**: `006-theme-presets`

**Created**: `2026-10-23`

**Status**: Draft

**Input**: User description: "Aggiunti nelle impostazioni dei set di temi chiari e scuri. Almeno due per tipo. Cerca esempi moderni online"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Choose a light theme (Priority: P1)

In Settings the user opens an "Appearance" section and chooses one of at least two light theme sets.
The whole app immediately adopts the chosen colors.

**Why this priority**: Light themes are half of the request and the most visible customization.

**Independent Test**: Open Settings, pick a light theme, and confirm every screen (drawer, home,
search, detail, settings, dialogs, full-screen viewer) uses it and keeps using it after a restart.

**Acceptance Scenarios**:

1. **Given** Settings is open, **When** the user reaches the Appearance section, **Then** at least two
   light theme options are listed with their names.
2. **Given** a light theme is selected, **When** the user confirms it, **Then** the whole app switches
   to that light theme immediately.
3. **Given** a light theme is selected, **When** the app is restarted, **Then** the same light theme is
   still active.

---

### User Story 2 - Choose a dark theme (Priority: P1)

In the same Appearance section the user chooses one of at least two dark theme sets, and the whole app
immediately adopts the chosen dark colors.

**Why this priority**: Dark themes are the other half of the request and must be equally available.

**Independent Test**: Open Settings, pick a dark theme, and confirm every screen uses it and keeps
using it after a restart.

**Acceptance Scenarios**:

1. **Given** Settings is open, **When** the user reaches the Appearance section, **Then** at least two
   dark theme options are listed with their names.
2. **Given** a dark theme is selected, **When** the user confirms it, **Then** the whole app switches
   to that dark theme immediately.
3. **Given** a dark theme is selected, **When** the app is restarted, **Then** the same dark theme is
   still active.

---

### User Story 3 - Follow the system or force light/dark (Priority: P2)

The user can choose whether the app follows the device's light/dark setting or forces light or dark.
When following the system, the app uses the selected light theme in light mode and the selected dark
theme in dark mode.

**Why this priority**: It makes both theme sets useful in everyday use without forcing a manual
switch, while still allowing an explicit override.

**Independent Test**: Set "Follow system", change the device appearance, and confirm the app switches
between the chosen light and dark themes accordingly; then force light and dark and confirm the app
ignores the device setting.

**Acceptance Scenarios**:

1. **Given** the Appearance section, **When** the user selects "Follow system", **Then** the app uses
   the selected light theme in light mode and the selected dark theme in dark mode.
2. **Given** the app follows the system, **When** the device switches to dark mode while the app is
   running, **Then** the app updates without a restart.
3. **Given** the user forces "Light" or "Dark", **When** the device appearance changes, **Then** the
   app keeps the forced mode.

---

### Edge Cases

- What happens when the persisted theme value is unknown (for example after an upgrade)?
- What happens to dialogs, snackbars and system bars when the theme changes?
- What happens when the theme changes while an image is loading or a gesture is in progress?
- What happens to text/icon contrast in every preset, including the pastel light/dark palettes?
- What happens when the device is in dark mode but the user forced light (and vice versa)?
- What happens if the user has never opened the Appearance section (defaults)?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Settings MUST include an "Appearance" section.
- **FR-002**: The Appearance section MUST offer at least two light theme presets and at least two dark
  theme presets, each with a user-visible name.
- **FR-003**: Selecting a preset MUST apply it to the whole app immediately, without a restart.
- **FR-004**: The selected light preset, dark preset and appearance mode MUST be persisted locally and
  survive app restarts.
- **FR-005**: The appearance mode MUST offer "Follow system", "Light" and "Dark".
- **FR-006**: In "Follow system" mode the app MUST use the selected light preset in light mode and the
  selected dark preset in dark mode, and MUST react to a live system change.
- **FR-007**: Every preset MUST meet accessible contrast for body text (at least 4.5:1) and for large
  text/icons (at least 3:1).
- **FR-008**: The theme MUST cover every screen and surface: drawer, home/gallery, search, album
  screens, detail, settings, dialogs, snackbars, full-screen viewer and system bars.
- **FR-009**: Theme colors MUST come from the preset definitions; UI code MUST NOT hardcode colors
  outside the theme definitions.
- **FR-010**: An unknown or invalid persisted value MUST fall back to the default presets and the
  "Follow system" mode without crashing.
- **FR-011**: All new user-visible text (section title, mode labels, preset names/descriptions) MUST be
  provided in both English and Italian.
- **FR-012**: The default configuration MUST preserve the current app appearance until the user changes
  it.

### Key Entities *(include if feature involves data)*

- **Theme preset**: A named color scheme for light or dark mode, with the color roles needed by the
  app surfaces.
- **Appearance settings**: The persisted choice of appearance mode (system/light/dark) plus the
  selected light preset and dark preset.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The Appearance section offers at least 2 light and at least 2 dark presets.
- **SC-002**: 100% of screens and dialogs adopt the selected theme within 1 second of selection.
- **SC-003**: The selected mode and presets survive 100% of app restarts.
- **SC-004**: Every preset meets at least 4.5:1 contrast for body text and 3:1 for large text/icons.
- **SC-005**: 100% of the new strings are available in both English and Italian.
- **SC-006**: Following the system changes the applied theme within 1 second of the device change.

## Assumptions

- "Theme" means the color scheme (the app's color roles), not typography, shape or spacing.
- The offered presets are inspired by modern open-source palettes researched online: the Material 3
  baseline palettes (Material Color Utilities / Material Theme Builder) and Catppuccin's light
  "Latte" and dark "Mocha" flavors, with room to add more presets later. Exact hues may be tuned to
  satisfy the contrast requirement.
- "Follow system" is the default mode; the default light/dark presets reproduce the current app
  appearance.
- The theme applies to system bars and dialogs as well as to the app content.
- Themes are display-only and do not change stored wallpapers, filters or any other data.
