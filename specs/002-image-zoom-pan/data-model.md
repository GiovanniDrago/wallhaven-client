# Phase 1 Data Model: Image Zoom and Pan

This feature persists nothing — it is a display-only interaction. The "model" below is the in-memory
interaction state and its invariants, which the pure functions in `ui/components/ZoomPan.kt` enforce.

## Interaction State

| Field | Type | Notes |
|-------|------|-------|
| `scale` | `Float` | Current zoom factor. Invariant: `1f <= scale <= MAX_SCALE`. `1f` is the exact fit view. |
| `offset` | `Offset` (x, y in pixels) | Current pan translation. Invariant: within the per-axis bounds for the current `scale`. |
| `containerSize` | `IntSize` | The measured size of the image viewport (the `Box`). |
| `contentSize` | `IntSize` | The fitted (unscaled) displayed image size, derived from `containerSize` and the image aspect ratio. |

Derived:

- `maxTranslation(containerSize, contentSize, scale)` → per-axis clamp bounds; `0` on an axis whose
  scaled content is not larger than the container on that axis.
- `canPan(scale, bounds)` → true when the content can move on at least one axis.

## Pure Functions (the testable contract)

- `fitContentSize(container: IntSize, imageWidth: Int, imageHeight: Int): IntSize`
- `clampScale(scale: Float, min: Float = 1f, max: Float = MAX_SCALE): Float`
- `maxTranslation(container: IntSize, content: IntSize, scale: Float): Offset`
- `clampOffset(offset: Offset, bounds: Offset): Offset`

Constants: `MIN_SCALE = 1f`, `MAX_SCALE = 6f` (bounded, generous enough to inspect detail),
`DOUBLE_TAP_SCALE` between the two.

## State Transitions

```text
Fit (scale = 1, offset = 0)
  │ two-finger pinch out            │ two-finger pinch in (already zoomed)
  ▼                                 ▼
Zoomed/Panned (1 < scale <= MAX, offset clamped)
  │ pinch in to min                 │ double-tap
  ▼                                 ▼
Fit (centered, offset = 0)

Image identity (url) changes  ─────►  Fit (hard reset)
Viewport resized              ─────►  re-clamp offset to the new bounds
```

- **Reset on image change** (FR-011): the fit state is restored whenever the displayed image changes.
- **Centered fit** (FR-010): at `scale = 1`, `offset` is exactly `Offset.Zero`.
- **Never lost** (FR-008): `offset` is always within `maxTranslation`, so a visible portion stays on
  screen; when the scaled content is smaller than the container on an axis, that axis stays centered.

## Validation Rules

| Rule | Source |
|------|--------|
| Scale clamped to `[1, MAX_SCALE]` | FR-002, FR-012 |
| Offset clamped to per-axis bounds | FR-008 |
| At minimum scale the view is centered and pan is zero | FR-010 |
| A gesture never leaves an unrecoverable state | FR-006 |
| No crash on zero/non-finite sizes or unloaded image | FR-007 |

## Non-Goals

- No database entities, no PagingSource, no network, no exports. Zoom/pan does not modify the stored
  wallpaper or any saved data.
