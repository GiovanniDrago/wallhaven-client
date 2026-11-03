# UI Contracts: Image Zoom and Pan

This feature is a UI-only behavior change on the shared image component. The contract is the component
API, the gesture semantics, and the bounds guarantees it must uphold.

## Component Contract

`ZoomableImage(url: String, modifier: Modifier = Modifier, maxScale: Float = MAX_SCALE,
singleFingerPan: Boolean = false, onSingleTap: (() -> Unit)? = null)`.

- **Renders**: the image at fit scale inside a bounded, clipping viewport; shows a loading indicator
  while loading and an error indicator on failure (as today).
- **State**: internal only (`scale`, `offset`); no state is hoisted and nothing is persisted.
- **Reset**: whenever `url` changes, the view returns to the fit, centered state.
- **Parameters**: `maxScale` bounds the zoom (inline `MAX_SCALE = 6f`, full-screen
  `FULLSCREEN_MAX_SCALE = 20f`); `singleFingerPan` makes a one-finger drag pan the image (full-screen)
  instead of scrolling the page (inline); `onSingleTap` is invoked on a single tap (inline opens the
  viewer, full-screen closes it), and must not fire when a double tap zooms.

All full-view images (wallpaper detail opened from gallery, search, favorites and albums) use this
component, so the behavior applies everywhere (FR-001).

## Full-Screen Viewer Contract

- **Trigger**: a single tap on the image on the detail screen opens the viewer (FR-013). It is not
  reachable from the gallery or any grid (FR-019).
- **Appearance**: the viewer covers the whole window with a black background; areas the image does not
  cover are black (FR-014).
- **Gestures**: pinch zooms up to `FULLSCREEN_MAX_SCALE` (FR-015); a single finger pans the image
  (FR-016); double tap still toggles zoom.
- **Exit**: a single tap on the image or the system back closes the viewer and returns to the detail
  (FR-017). While the viewer is open, the system back closes the viewer only (it does not leave the
  detail).
- **State**: the viewer opens at fit, and closing it returns the inline detail image to fit (FR-018,
  Clarifications 2026-10-15).

## Gesture Contract

| Gesture | Pointer count | Behavior | Consumed by |
|---------|---------------|----------|-------------|
| Page scroll | 1 finger drag | Scrolls the surrounding scrollable page | Parent (not consumed by the image) |
| Zoom | 2 fingers pinch | Changes scale continuously within `[1, MAX_SCALE]` | Image |
| Pan | 2 fingers drag | Moves the zoomed image, revealing other areas | Image |
| Zoom back | 2 fingers pinch in to minimum | Returns to the exact fit view | Image |
| Toggle zoom | Double tap | Toggles between fit and a zoomed view centered on the tap | Image |

Rules:

- Only multi-pointer (two or more) gestures are consumed by the image; single-pointer drags are left
  to the parent so page scrolling always works (FR-005).
- Pinch zoom keeps the point under the midpoint between the fingers fixed (the pinch centroid), not
  the screen centre (FR-020).
- Panning moves the image by the same distance the finger(s) travel, at every zoom level; the pan is
  scaled by the current zoom to compensate for the layer transform, so it never slows down at high
  zoom (FR-021).
- After any gesture the user can immediately zoom, pan or scroll again; no gesture path leaves the
  view unable to respond (FR-006).
- Rapid or repeated gestures MUST NOT crash or freeze the view (FR-007).

## Bounds Guarantees

- Scale is clamped to `[1, MAX_SCALE]`; `1` is exactly the fit view (FR-002, FR-012).
- Pan offset is clamped per axis so that:
  - at least a visible portion of the image remains on screen in every direction (FR-008);
  - an axis whose scaled content is not larger than the viewport stays centered;
  - at `scale = 1` the offset is zero and the image is centered (FR-010).

## Integration Contract

- The image view is placed inside a vertically scrollable detail page. The component MUST NOT consume
  single-pointer vertical drags, so the page keeps scrolling normally when the image is at fit scale.
- No new user-visible strings are required by default. If a control with user-visible text (e.g. a
  reset button and its content description) is added, the string MUST exist in both `values/` and
  `values-it/` (Principle IV).

## Error / Edge Behavior

- Before the image is loaded and the viewport is measured, gestures are ignored (no jump, no crash).
- Zero or non-finite sizes are guarded so the bounds math cannot produce `NaN`/infinite offsets.
- On viewport resize (for example a rotation), the offset is re-clamped to the new bounds.
