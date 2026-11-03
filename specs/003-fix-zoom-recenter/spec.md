# Feature Specification: Fix Zoom Recenter on Finger Release

**Feature Branch**: `003-fix-zoom-recenter`

**Created**: 2026-10-15

**Status**: Draft

**Input**: User description: "there is an issue with the latest feature. The zoom and navigation of the image are fine, but in the moment that I lift up my fingers from the screen, it center to center of the image, which is not what intended to be, it should stay focus where I'm looking"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Lifting the fingers keeps the focused area (Priority: P1)

While zooming and moving an image, the moment the user lifts their fingers off the screen the view
must stay exactly where they left it. Today the image jumps back to its center on release, losing the
area the user was inspecting.

**Why this priority**: The zoom and pan themselves already work; this jump on release is what makes
the feature unusable for inspecting a specific detail. Fixing it is the whole point of this change.

**Independent Test**: Zoom and pan to a corner of the image, lift the fingers, and confirm the view
does not move; repeat for both the inline image and the full-screen view.

**Acceptance Scenarios**:

1. **Given** the user has zoomed and panned to a chosen area, **When** they lift their fingers,
   **Then** the view stays on that area with the same zoom and position.
2. **Given** the user is mid-pinch (zoom), **When** they lift the fingers, **Then** the zoom level and
   position are exactly what they were on the last frame of the gesture.
3. **Given** the user releases one finger and then the other, **When** the last finger leaves the
   screen, **Then** no recentering or jump occurs.
4. **Given** the image is at the fit view (not zoomed), **When** the user taps or briefly drags and
   releases, **Then** nothing moves.

---

### Edge Cases

- What happens when the user releases the fingers at the maximum zoom level?
- What happens when the gesture is cancelled by the system (e.g. a system gesture interrupts it)?
- What happens when one finger is lifted while the other keeps moving?
- What happens when the release happens while the image is still loading?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Lifting the fingers at the end of a zoom or pan gesture MUST NOT change the image's zoom
  level or position.
- **FR-002**: After the fingers are lifted, the area the user was focused on MUST remain under the
  same screen position it had during the gesture; the view MUST NOT re-center.
- **FR-003**: The zoom and pan behaviour while the fingers are on the screen MUST remain as it is today.
- **FR-004**: The corrected release behaviour MUST apply to every image view that supports zoom and pan
  (the inline image and the full-screen view).
- **FR-005**: The fix MUST NOT introduce a jump, flicker, or crash at the moment of release.

### Key Entities *(include if feature involves data)*

Not applicable: this is a correction to the image interaction state; no new data is introduced.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Across 50 zoom/pan gestures, releasing the fingers produces 0 recentering events (the
  view never returns to the image center on release).
- **SC-002**: After release, the visible area is identical to the last frame of the gesture, so the
  user keeps looking at the same detail without any corrective gesture.
- **SC-003**: The fix works identically in the inline image and the full-screen view.

## Assumptions

- Zoom and pan during the gesture are correct and must not change; only the behaviour at the moment the
  fingers are lifted is wrong.
- Both the inline (detail page) image and the full-screen viewer are affected and must be fixed.
- Releasing the fingers is a routine end-of-gesture event, so the view must simply retain its current
  scale and position.
