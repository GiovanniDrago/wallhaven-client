# Phase 1 Data Model: Fix Zoom Recenter on Finger Release

This feature corrects a state transition; it adds no data. The relevant model is the gesture state and
the invariant that a release frame is a no-op.

## Interaction State (unchanged)

| Field | Type | Notes |
|-------|------|-------|
| `scale` | `Float` | `1f <= scale <= maxScale`. |
| `offset` | `Offset` | Clamped pan within the image bounds. |
| `containerSize` / `contentSize` | `IntSize` | Viewport and fitted content sizes. |

## Gesture Frame Rules (updated)

| Rule | Source |
|------|--------|
| While at least one pointer is pressed, zoom/pan update the state as today | FR-003 |
| When no pointer is pressed (release frame), the state MUST NOT change | FR-001, FR-002 |
| `transform(...)` MUST return the incoming `scale`/`offset` when `centroid`, `pan` or `zoomChange` is not finite | FR-005 |
| No jump, flicker or crash at release | FR-005 |

## State Transitions

```text
Gesture in progress (>=1 pointer pressed)
  │ fingers lifted (0 pointers pressed)
  ▼
Released — identical scale + offset (no recenter)   ← the fix
```

## Validation Rules

| Rule | Source |
|------|--------|
| Release leaves `scale` and `offset` unchanged | FR-001, FR-002 |
| Unusable transform inputs leave the state unchanged | FR-005 |
| Applies to inline and full-screen (shared component) | FR-004 |

## Non-Goals

- No change to how zoom/pan behave while the fingers are down.
- No data, migrations, network, or new strings.
