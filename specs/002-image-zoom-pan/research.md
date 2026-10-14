# Phase 0 Research: Image Zoom and Pan

Each decision records what was chosen, why, and what alternatives were rejected.

## R1. Gesture model: separate one-finger scroll from two-finger zoom/pan

- **Decision**: Replace the `transformable` gesture with custom low-level pointer handling
  (`Modifier.pointerInput` + `awaitEachGesture`). While two or more pointers are down, compute the
  zoom factor (`calculateZoom`), the pan delta (`calculatePan`) and the centroid (`calculateCentroid`),
  apply them to the view state, and **consume** those pointer changes. While a single pointer is down,
  do **not** consume the changes, so the surrounding scrollable page receives the drag and scrolls.
- **Rationale**: The reported failures — the page cannot scroll, the image cannot be moved, and the
  zoom gets stuck — come from `transformable` consuming every drag (including one-finger drags) and
  from an unbounded pan offset. Consuming only multi-pointer gestures removes the scroll conflict, and
  leaving single-pointer drags to the parent restores normal scrolling on every image screen
  (FR-005, FR-002, FR-003, FR-006).
- **Alternatives considered**: `Modifier.transformable(rememberTransformableState)` (rejected: eats
  one-finger drags → the current bug); `detectTransformGestures` (rejected: same consumption); a
  zoom-only library (rejected: new dependency and the stack MUST NOT change).

## R2. Bounded pan and zoom

- **Decision**: Compute the on-screen fitted content size from the container size and the image
  aspect ratio, then the maximum translation per axis as `max(0, (contentSize * scale - containerSize) /
  2)`. Clamp the pan offset within those bounds; when the scaled content is smaller than the container
  on an axis, lock that axis to zero (centered). Clamp the scale between `1` (exact fit) and a fixed
  maximum.
- **Rationale**: Guarantees a visible portion always remains on screen (FR-008) and an exact centered
  fit at minimum zoom (FR-010, FR-012). This is the standard image-viewer clamping behavior.
- **Alternatives considered**: unbounded offset (rejected: the current bug where the image is lost);
  clamping only when the gesture ends (rejected: janky and can still leave the viewport mid-gesture).

## R3. Recovering the full view

- **Decision**: Reset scale/offset to fit whenever the displayed image changes
  (`LaunchedEffect(url)`). Support double-tap to toggle between the fit view and a zoomed view centered
  on the tap point, and allow pinch-out to the exact fit. Because pan is clamped (R2) the image can
  never be lost, so no separate "find the image" recovery is needed.
- **Rationale**: FR-009, FR-010, FR-011.
- **Alternatives considered**: pinch-out only (rejected: less convenient); a mandatory reset button
  (rejected: requires a new string in both locales and adds clutter; double-tap needs none).

## R4. One shared component, unchanged public API

- **Decision**: Keep the existing `ZoomableImage(url: String, modifier: Modifier = Modifier)` API and
  update its internals. Every full-view image (wallpaper detail opened from gallery, search,
  favorites, albums) already renders through it.
- **Rationale**: FR-001 (available on every image) with no call-site churn and no change to existing
  public behavior outside the component (constitution: keep public APIs compatible).
- **Alternatives considered**: a new component or per-screen implementations (rejected: duplication
  and inconsistent behavior).

## R5. Testable zoom/pan math on the JVM

- **Decision**: Extract the math into pure top-level functions in `ui/components/ZoomPan.kt`
  (`clampScale`, `maxTranslation`, `clampOffset`, and a helper to compute the fitted content size) and
  cover them with JUnit tests in `ZoomPanTest.kt`. The gesture wiring itself stays in the Composable.
- **Rationale**: Constitution Principle V requires JVM-only verification and the project has no
  Robolectric; pure functions give strong coverage of the bounds/zoom guarantees without a device.
- **Alternatives considered**: instrumented UI tests (rejected: need a device, forbidden by the
  constitution); Robolectric (rejected: new dependency and outside the mandated stack).

## R6. No new dependencies and minimal strings

- **Decision**: Use only the existing Compose gesture APIs. Prefer no new user-visible strings; if a
  reset affordance is added later, its text MUST be added to both `values/` and `values-it/`.
- **Rationale**: Keep the mandated stack (constitution) and localization completeness (Principle IV).
- **Alternatives considered**: third-party zoom libraries (rejected: dependency change).

## R7. Stability against the stuck state

- **Decision**: Ignore gestures until the container is measured and the image is loaded; guard all
  size/scale math against zero and non-finite values; key `pointerInput` by the image identity so the
  gesture coroutine is recreated cleanly when the image changes.
- **Rationale**: FR-007 (no crashes/freezes) and prevents stale gesture state from surviving a change
  of image — the root of the "zoom does not work again" symptom.
- **Alternatives considered**: reusing a single long-lived gesture coroutine (rejected: stale state
  after the image changes).
