# Implementation Plan: Image Zoom and Pan

**Branch**: `002-image-zoom-pan` | **Date**: 2026-10-13 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-image-zoom-pan/spec.md`

## Summary

Rewrite the shared image viewer so that zoom and pan are free, bounded and never trap the user.
Today `ZoomableImage` uses a `transformable` gesture that consumes every drag, keeps an unbounded
pan offset, and has no bounds, which lets the image be dragged off-screen and blocks page scrolling —
the "zoom breaks the app" behavior reported. The new implementation keeps one shared component used
by every full-view image (the wallpaper detail screen, opened from gallery, search, favorites and
albums) and:

- handles one pointer as page scroll (left to the parent) and two pointers as pinch-zoom + pan,
  consuming only the multi-pointer gesture;
- clamps zoom between the fit scale and a fixed maximum, and clamps pan so a visible portion of the
  image always remains on screen;
- restores the centered fit view via pinch-out, double-tap toggle, and a reset that runs whenever the
  displayed image changes.

The zoom/pan math is extracted into pure functions so it is verified by JVM unit tests, matching the
project's test-only quality gate. No new dependencies are introduced.

## Technical Context

**Language/Version**: Kotlin 2.4.10, JVM 17

**Primary Dependencies**: Jetpack Compose (Material 3), Coil (`SubcomposeAsyncImage`), AndroidX
`pointerInput`/gestures. No new dependencies.

**Storage**: N/A — zoom/pan is display-only and persists nothing.

**Testing**: JVM unit tests with JUnit4 (and kotlinx-coroutines-test / MockK where useful) for the
pure zoom/pan math (`clampScale`, `maxTranslation`, `clampOffset`). Run by the CI gate
`./gradlew test assembleDebug`. No device/emulator.

**Target Platform**: Android, minSdk 26, targetSdk/compileSdk 37

**Project Type**: mobile-app (single Android application module `app/`)

**Performance Goals**: gesture tracking stays smooth (no dropped frames) during pinch and pan; the
image follows the fingers without lag.

**Constraints**: one-finger drag scrolls the surrounding page; two-finger gestures zoom/pan; pan is
bounded; the view can never get stuck; the shared component keeps its existing public API
(`ZoomableImage(url, modifier)`) so call sites are unchanged.

**Scale/Scope**: one shared image component and its pure support math; used by every full-view image.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Layered Architecture & Dependency Direction | Change stays in `ui/components` (shared Composable) plus a small pure-math file; no data/API/DB access from UI. | PASS |
| II. Room as Single Source of Truth (Offline-First & Paging) | Not affected: zoom/pan is display-only and writes nothing to the database or Paging cache. | PASS |
| III. Secrets & Privacy by Default | No secrets involved; the image URL is public and unchanged. | PASS |
| IV. Localization Completeness | Any new user-visible text (e.g. a reset control's description) MUST be added to both `values/` and `values-it/`; the design avoids new strings where possible. | PASS |
| V. Verification & Quality Gate | Zoom/pan math is pure and covered by JVM unit tests; CI gate `./gradlew test assembleDebug` must pass. | PASS |

No violations. Complexity Tracking is not required.

**Post-Design Re-Check (after Phase 1)**: Still PASS. The design keeps the change inside the UI
component, adds no dependencies, writes no data, keeps the component API unchanged, and isolates the
testable math in pure functions (Principle V). No new user-visible strings are required by default.

## Project Structure

### Documentation (this feature)

```text
specs/002-image-zoom-pan/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (interaction state model)
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── ui-contracts.md  # Phase 1 output (component + gesture contract)
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created here)
```

### Source Code (repository root)

```text
app/src/main/java/dev/giovannidrago/wallhaven/ui/components/
├── ZoomableImage.kt     # UPDATE: rewrite gesture handling (bounded zoom + two-finger pan)
└── ZoomPan.kt           # NEW: pure math (clampScale, maxTranslation, clampOffset, centroid scale)

app/src/test/java/dev/giovannidrago/wallhaven/
└── ZoomPanTest.kt       # NEW: unit tests for the pure zoom/pan math

app/src/main/res/values/strings.xml       # UPDATE only if a new control string is added
app/src/main/res/values-it/strings.xml    # UPDATE in lockstep with the English file
```

**Structure Decision**: Single Android application module (`app/`). The gesture rewrite stays in the
existing shared `ZoomableImage` component so every image view benefits with no call-site changes; the
only new file is a pure-math helper that the component uses and the JVM tests cover.

## Complexity Tracking

No constitution violations; this section is intentionally empty.
