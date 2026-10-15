# Quickstart Validation: Image Zoom and Pan

This guide proves the feature works end-to-end. It contains no implementation code; details live in
[spec.md](./spec.md), [plan.md](./plan.md), [data-model.md](./data-model.md) and
[contracts/ui-contracts.md](./contracts/ui-contracts.md).

## Prerequisites

- JDK 17. No local Android SDK is required; builds and tests run in CI.
- For manual checks, install the debug APK produced by the `Test` CI workflow (or the personal release
  APK delivered to the shared folder).

## Automated Verification (quality gate)

```bash
./gradlew test assembleDebug
```

Same gate the `Test` workflow runs on every push (Constitution Principle V). The new test is:

- `ZoomPanTest` — pure math: scale is clamped to `[1, MAX_SCALE]` (and honors a custom higher cap
  such as the full-screen 20×); `maxTranslation` is zero when the scaled content fits the viewport and
  grows with scale; `clampOffset` keeps the offset inside the bounds; the fit view is centered;
  zero/oversized inputs never produce `NaN`/infinite values.

## Manual Scenarios

### Z1 — Free two-finger zoom and pan (US1, FR-002/FR-003/FR-004)

1. Open any wallpaper detail (from gallery, search, favorites or an album).
2. Pinch outward with two fingers: the image zooms in and follows the gesture.
3. Drag with two fingers: the visible area moves to reveal other parts of the image.
4. Pinch inward: the image zooms back and stops at the exact fit view.
5. Keep pinching outward past the maximum: zoom stops cleanly with no distortion.

### Z2 — Gestures never trap the user (US2, FR-005/FR-006/FR-007)

1. At fit scale, drag with one finger: the page scrolls.
2. Zoom in, then drag with one finger: the page still scrolls (or scrolls again immediately).
3. Perform a long, mixed sequence of pinch, two-finger pan and one-finger scroll; after every step the
   next gesture responds immediately.
4. Repeat 50 zoom/pan/scroll cycles: no freeze, no crash, and no state that requires reopening the
   image to regain control.

### Z3 — Always recover the full view (US3, FR-008/FR-009/FR-010/FR-011)

1. Zoom in and drag hard toward each edge: the image stops at its bounds and never leaves the screen.
2. Double-tap: the view toggles between fit and a zoomed view centered on the tap point.
3. At fit scale, confirm the image is centered with no residual pan.
4. Go back and reopen the same wallpaper, and open a different one: each opens at the fit, centered
   view.

### Z4 — Stability around loading and rotation (FR-007)

1. Open an image and gesture while it is still loading: no jump or crash once it appears.
2. Rotate the device while zoomed: the image re-clamps to the new viewport and remains visible.

### Z5 — Full-screen viewer (US4, FR-013–FR-019)

1. Open an image detail and single-tap the image: it opens full screen, black where the image does not
   cover the screen.
2. Pinch to zoom: it can fill the screen and beyond, up to the high cap (about 20×); a one-finger drag
   moves the image.
3. Single-tap the image: the viewer closes and returns to the detail with the image at fit.
4. Reopen and press the system back action: only the viewer closes, returning to the detail.
5. From the gallery or any grid, tap a thumbnail: it opens the detail as today; no full-screen view
   opens from there.

## Expected Outcomes

- All JVM tests pass and the debug APK assembles in CI.
- Pinch-zoom and two-finger pan feel free and bounded; one-finger drag always scrolls the page.
- The image can never be lost off-screen and the fit view is always one gesture away.
- No crashes, freezes, or stuck-gesture states across repeated interactions.
