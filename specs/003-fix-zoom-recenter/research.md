# Phase 0 Research: Fix Zoom Recenter on Finger Release

## R1. Root cause

- **Decision**: The recenter is caused by processing the pointer-up event. `ZoomableImage` runs its
  `do { ... } while (changes.any { it.pressed })` loop and transforms *before* checking that pointers
  are still down. On the release frame the gesture deltas are meaningless: the pinch centroid can be
  non-finite (so the offset clamp collapses to `Offset.Zero`) and/or the computed zoom factor can be
  zero (so `clampScale` returns `MIN_SCALE` and `transform` returns the centered fit). Either way the
  image snaps back to its center.
- **Rationale**: Matches the report exactly — the view is correct during the gesture and jumps to the
  center the instant the fingers are lifted.
- **Alternatives considered**: Blaming the `LaunchedEffect` re-clamp (rejected: it only runs on size
  changes, not on release); a rendering/animation issue (rejected: nothing animates the offset).

## R2. Fix

- **Decision**: Two complementary guards:
  1. In `ZoomableImage`, stop transforming as soon as no pointer is pressed (check the pressed count
     before transforming, and end the gesture then) so the release frame never updates the state.
  2. In the pure `transform(...)`, return the incoming `scale`/`offset` unchanged when `centroid`,
     `pan`, or `zoomChange` is not finite (or the centroid is unspecified), so an unusable frame can
     never recenter or rescale the view.
- **Rationale**: The loop guard fixes the trigger; the pure guard makes the math robust and testable on
  the JVM (Principle V) regardless of the caller.
- **Alternatives considered**: Only the loop guard (rejected: less defensive); only the math guard
  (rejected: still processes a meaningless frame and could fight the loop).

## R3. Verification

- **Decision**: Add a `ZoomPanTest` case asserting that `transform(...)` with a non-finite/unspecified
  centroid returns exactly the incoming scale and offset. Keep the existing geometric tests.
- **Rationale**: Cheap, deterministic, JVM-only, and directly encodes "release must not move the view".
- **Alternatives considered**: Instrumented gesture tests (rejected: need a device, forbidden by the
  constitution).

## R4. Scope

- **Decision**: The fix lives in the shared `ZoomableImage`, so the inline detail image and the
  full-screen viewer are both corrected with no call-site changes.
- **Rationale**: FR-004; keeps one implementation.
- **Alternatives considered**: Fixing each call site (rejected: duplication).
