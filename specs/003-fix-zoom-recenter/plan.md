# Implementation Plan: Fix Zoom Recenter on Finger Release

**Branch**: `003-fix-zoom-recenter` | **Date**: 2026-10-15 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/003-fix-zoom-recenter/spec.md`

## Summary

When the user lifts their fingers at the end of a zoom/pan gesture, the shared `ZoomableImage`
gesture loop still processes the pointer-up event. On that release frame the gesture deltas are no
longer meaningful, so `transform(...)` recomputes the view and forces it back to the centered fit
(a non-finite pinch centroid collapses to `Offset.Zero`, and a zero zoom factor clamps to the minimum
scale), which snaps the image back to its center. The fix keeps the current scale and offset on
release: the gesture loop stops transforming once no pointer is pressed, and the pure `transform(...)`
returns the incoming state unchanged when its inputs are not usable. The change applies to both the
inline detail image and the full-screen viewer because they share the component. No new dependencies,
data, or strings.

## Technical Context

**Language/Version**: Kotlin 2.4.10, JVM 17

**Primary Dependencies**: Jetpack Compose gestures (`pointerInput`/`awaitEachGesture`), Coil. No new
dependencies.

**Storage**: N/A — display-only interaction state.

**Testing**: JVM unit tests (JUnit4) for the pure `transform(...)` guard. Run by the CI gate
`./gradlew test assembleDebug`.

**Target Platform**: Android, minSdk 26, targetSdk/compileSdk 37

**Project Type**: mobile-app (single Android application module `app/`)

**Performance Goals**: no jump/flicker/crash at the moment of release; the release frame is a no-op.

**Constraints**: zoom and pan while the fingers are down MUST stay exactly as they are today; only
the behaviour on release changes.

**Scale/Scope**: one shared component and its pure math; used by every zoomable image.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Layered Architecture & Dependency Direction | Change stays in `ui/components`; no data/API access. | PASS |
| II. Room as Single Source of Truth (Offline-First & Paging) | Not affected; display-only. | PASS |
| III. Secrets & Privacy by Default | No secrets involved. | PASS |
| IV. Localization Completeness | No user-visible strings added. | PASS |
| V. Verification & Quality Gate | The pure `transform(...)` guard is covered by a JVM test; CI gate must pass. | PASS |

No violations. Complexity Tracking is not required.

**Post-Design Re-Check (after Phase 1)**: Still PASS — UI-only, pure math tested, no data/deps/strings.

## Project Structure

### Documentation (this feature)

```text
specs/003-fix-zoom-recenter/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (interaction state invariant)
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── ui-contracts.md  # Phase 1 output (release behaviour)
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
app/src/main/java/dev/giovannidrago/wallhaven/ui/components/
├── ZoomableImage.kt     # UPDATE: stop transforming once no pointer is pressed (retain state)
└── ZoomPan.kt           # UPDATE: guard transform(...) against non-finite centroid/pan/zoom

app/src/test/java/dev/giovannidrago/wallhaven/
└── ZoomPanTest.kt       # UPDATE: assert transform keeps the state for unusable inputs
```

**Structure Decision**: Single Android application module. The fix is confined to the shared image
component and its pure math; no call sites change.

## Complexity Tracking

No constitution violations; this section is intentionally empty.
