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

## R8. Full-screen viewer as an overlay (US4)

- **Decision**: Add the full-screen viewer as an overlay in the image detail screen (state flag +
  `Box(fillMaxSize, black)` above the `Scaffold`), hosting the same `ZoomableImage` component. Do not
  create a separate navigation route.
- **Rationale**: Keeps a single image component, makes the "single tap opens / single tap closes"
  behavior local and swipeless, and avoids routing/back-stack complexity (FR-013, FR-017).
- **Alternatives considered**: a dedicated full-screen route (rejected: extra navigation and
  state-passing for no benefit); a Dialog (rejected: harder to make truly full-bleed and gesture
  friendly).

## R9. Extend `ZoomableImage` with optional parameters

- **Decision**: Add `maxScale: Float = MAX_SCALE`, `singleFingerPan: Boolean = false`, and
  `onSingleTap: (() -> Unit)? = null`. Defaults preserve the inline behavior and existing call sites.
- **Rationale**: The full-screen view needs a higher zoom cap, one-finger panning, and tap-to-exit;
  the inline view needs a single-tap-to-open hook. Optional parameters keep the public API compatible
  (constitution) and FR-013/FR-015/FR-016 satisfied.
- **Alternatives considered**: a second component (rejected: duplicated gesture logic and drift).

## R10. One-finger pan mode for full-screen

- **Decision**: When `singleFingerPan` is true the gesture handler consumes single-pointer drags and
  pans the image; when false it keeps leaving single-pointer drags to the parent for page scrolling.
- **Rationale**: In full-screen there is no page to scroll, and the user asked to move the image with a
  single finger (FR-016); inline keeps the page-scroll behavior (FR-005).
- **Alternatives considered**: keeping two-finger pan in full-screen (rejected: contradicts the
  request).

## R11. Zoom caps

- **Decision**: Inline view keeps `MAX_SCALE = 6f`; the full-screen view uses
  `FULLSCREEN_MAX_SCALE = 20f`. `clampScale` gains a `max` parameter (defaulting to `MAX_SCALE`).
- **Rationale**: The clarifications ask for a very high but bounded cap in full-screen ("fill the
  screen and beyond"), while the inline view stays modest (FR-015). A bound keeps the math and tests
  deterministic.
- **Alternatives considered**: a single global cap (rejected: either too small for full-screen or too
  large inline); unlimited zoom (rejected: unusable over-zoom and unbounded math).

## R12. Enter/exit state and back handling

- **Decision**: The viewer opens fresh at fit; exiting (single tap or system back via `BackHandler`)
  returns the detail image to fit by keying the inline image on the viewer flag. The system back only
  closes the viewer while it is open (FR-018).
- **Rationale**: Implements the clarification (Option A) and keeps back behavior predictable.
- **Alternatives considered**: carrying zoom state across (rejected: the user chose fit-on-exit).

## R13. Focal-point zoom and 1:1 panning (FR-020, FR-021)

- **Decision**: Perform the zoom/pan update with a single pure `transform(...)` that (a) clamps the
  scale, (b) keeps the pinch centroid fixed, (c) converts the reported pan from the scaled coordinate
  space to screen pixels by multiplying by the current scale, and (d) clamps the offset to bounds.
- **Rationale**: Because the image is drawn with `graphicsLayer`, pointer deltas arrive in the scaled
  space, so the raw pan is divided by the zoom factor — the reported "a long finger movement moves the
  image very little" bug. Multiplying by the scale restores 1:1 finger tracking (FR-021), and shifting
  the offset by `focal * (oldScale - newScale)` keeps the point between the fingers in place (FR-020).
- **Alternatives considered**: moving the `pointerInput` outside the `graphicsLayer` (rejected:
  Compose applies the layer transform at the node level, so this does not reliably change the
  coordinate space); a pan acceleration multiplier >1 (rejected: the correct fix is 1:1, not faster
  than the finger).
