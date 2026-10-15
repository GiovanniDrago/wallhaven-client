# Feature Specification: Image Zoom and Pan

**Feature Branch**: `002-image-zoom-pan`

**Created**: 2026-10-13

**Status**: Draft

**Input**: User description: "improve the zoom in/out feature. I want to feel free so zoom in and move the zoomed area with the two two fingers and be able to zoom back. This must be something available whenever I open an Image inside this application. The issue at the moment is that sometimes the zoom break the application and does not let me zoom again, scroll down or move the image to se a particular of the image out of the zoomed section."

## Clarifications

### Session 2026-10-15

- Q: When the user taps to open the full-screen image and then exits, what should happen to zoom and pan? → A: Full-screen opens at the fit view; exiting returns the inline image to fit.
- Q: Where is the full-screen image view available? → A: Only from the image detail screen (tapping the image there); not from the gallery or any grid view.
- Q: How far should the user be able to zoom in the full-screen view? → A: A very high but bounded cap (about 20× the fit size).
- Q: Where should zoom focus when pinching? → A: On the midpoint between the two fingers (the pinch centroid), not the screen center.
- Q: How should panning move the image relative to the finger? → A: The image must follow the finger so a given finger movement moves the image by the same distance; currently a long finger movement moves the image far too little (the pan is slowed down by the zoom level).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Zoom and move freely with two fingers (Priority: P1)

While viewing an image, the user places two fingers and pinches to zoom in, drags with two fingers to
move the zoomed area and inspect any part of the picture, then pinches back out to return to the
original fit.

**Why this priority**: This is the core of the request. Without reliable two-finger zoom and pan,
the feature delivers nothing.

**Independent Test**: Open any image, zoom in with a pinch, drag to a corner, and pinch back to the
fit view; confirm each step follows the fingers.

**Acceptance Scenarios**:

1. **Given** an image is open at fit scale, **When** the user pinches outward with two fingers,
   **Then** the image zooms in smoothly and follows the gesture.
2. **Given** the image is zoomed in, **When** the user drags with two fingers, **Then** the visible
   area moves in the direction of the drag, revealing other parts of the image.
3. **Given** the image is zoomed in, **When** the user pinches inward, **Then** the image zooms out
   and returns to the original fit view at the minimum scale.
4. **Given** the user reaches the maximum zoom, **When** the user keeps pinching outward, **Then**
   the zoom stops at the maximum without distortion or loss of control.

---

### User Story 2 - Gestures never trap the user (Priority: P1)

Zooming, panning, and scrolling the page around the image all remain available at all times. A
gesture cannot leave the screen in a state where the user can no longer zoom, move the image, or
scroll the page.

**Why this priority**: The reported problem is that zoom sometimes breaks the app and blocks further
zoom, page scrolling, or moving the image. Fixing that stuck state is as important as the zoom
itself.

**Independent Test**: Repeatedly zoom, pan, and scroll through a long image detail page; verify that
after each gesture the user can still zoom, pan, and scroll without restarting the screen.

**Acceptance Scenarios**:

1. **Given** the image is at fit scale, **When** the user drags with one finger, **Then** the page
   scrolls normally.
2. **Given** the image is zoomed in, **When** the user drags with one finger, **Then** the page
   still scrolls (or the user can immediately scroll again) instead of the screen becoming stuck.
3. **Given** any sequence of zoom and pan gestures, **When** the gestures end, **Then** the user can
   immediately start another zoom, pan, or scroll.
4. **Given** the user zooms and pans repeatedly, **Then** the screen never freezes or crashes and
   never requires the user to leave and reopen the image to regain control.

---

### User Story 3 - Always recover the full view (Priority: P2)

At any point the user can return to the original, fully visible image, and the image can never be
moved completely out of sight.

**Why this priority**: Being able to zoom back and never lose the image is what makes the free zoom
safe and usable.

**Independent Test**: Zoom in, drag toward an edge, and use the reset action; confirm the image
returns centered at fit scale. Drag hard in every direction and confirm part of the image stays
visible.

**Acceptance Scenarios**:

1. **Given** the image is zoomed and panned, **When** the user double-taps, **Then** the view
   toggles between the fit view and a zoomed view.
2. **Given** the image is zoomed and panned, **When** the user uses the reset action, **Then** the
   image returns to fit scale and centered.
3. **Given** the image is zoomed and panned toward an edge, **When** the user drags beyond the image
   bounds, **Then** the image stops at its bounds and a visible portion always remains on screen.

---

### User Story 4 - Full-screen image view (Priority: P2)

From the image detail, a single tap on the image opens it full screen on a black background. The user
can zoom freely to fill the screen and beyond and move the image with a single finger, then leave
with a single tap or the back action.

**Why this priority**: It turns the detail image into an immersive viewer, building on the existing
zoom and pan work.

**Independent Test**: Open an image detail, tap the image, confirm the full-screen black view; zoom
and move with one finger; exit once with a tap and once with back.

**Acceptance Scenarios**:

1. **Given** the image detail is open, **When** the user single-taps the image, **Then** the image
   opens full screen.
2. **Given** the full-screen view is open, **When** part of the screen is not covered by the image,
   **Then** that area is black.
3. **Given** the full-screen view is open, **When** the user pinches, **Then** the image zooms freely
   at least enough to fill the screen and beyond, within the bounded maximum.
4. **Given** the full-screen view is open and zoomed, **When** the user drags with a single finger,
   **Then** the image moves to reveal other areas.
5. **Given** the full-screen view is open, **When** the user single-taps the image or uses the back
   action, **Then** the app returns to the image detail with the image at fit.
6. **Given** the gallery or any grid is shown, **When** the user taps a thumbnail, **Then** no
   full-screen view opens from there (the tap opens the detail as today).

---

### Edge Cases

- What happens when the user pinches while the surrounding page is being scrolled?
- What happens when the user zooms in and out very rapidly?
- What happens when the user reaches the minimum scale (the image must return to exactly the fit
  view, centered, with no residual pan).
- What happens when the pan reaches the image bounds (no part of the image may leave the viewport
  entirely).
- What happens when the image is still loading or fails to load while the user gestures?
- What happens when the user leaves the screen (back navigation) while zoomed and then reopens the
  same image?
- What happens on an image whose displayed size is smaller than the viewport in one orientation?
- What happens when a single tap and a double tap could both be recognized in the full-screen view
  (single tap exits, double tap zooms)? They MUST NOT conflict.
- What happens to the full-screen view when the device is rotated?
- What happens when the back action is used in the full-screen view versus at the detail level?
- What happens when the user taps or pinches while the image is still loading in the full-screen
  view?
- What happens when the pinch centroid is near an edge of the image or already outside it?
- What happens when panning at a high zoom: the image must track the finger exactly, with no slowdown
  proportional to the zoom.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Every image opened in the app MUST support the zoom and pan gestures described here,
  consistently wherever an image is displayed at full view.
- **FR-002**: Users MUST be able to zoom in and out continuously with a two-finger pinch, within a
  minimum scale (the fit view) and a defined maximum scale.
- **FR-003**: Users MUST be able to move the zoomed image with a two-finger drag to reveal any part
  of the image.
- **FR-004**: The image MUST follow the fingers smoothly and proportionally during zoom and pan.
- **FR-005**: On the detail page (not in the full-screen view), when the image is at fit scale, a
  one-finger vertical drag MUST scroll the surrounding page normally.
- **FR-006**: Zoom and pan gestures MUST NOT leave the screen in a stuck state: after any gesture,
  the user MUST be able to zoom, pan, or scroll again without leaving the screen.
- **FR-007**: Zooming and panning MUST NOT crash or freeze the app, including under rapid or repeated
  gestures.
- **FR-008**: The image MUST NOT be pannable entirely out of view; at least a defined visible portion
  MUST always remain on screen.
- **FR-009**: Users MUST be able to return to the original fit view at any time, both by pinching
  back to the minimum scale and through a reset action (double-tap toggling between fit and zoomed).
- **FR-010**: When the fit view is restored, the image MUST be centered with no residual panning.
- **FR-011**: The zoom and pan state MUST reset when the user leaves and reopens the image view.
- **FR-012**: The minimum zoom MUST correspond exactly to the fully fitted image, and the maximum
  zoom MUST be a fixed, bounded factor.
- **FR-013**: A single tap on the image in the detail screen MUST open a full-screen image view.
- **FR-014**: The full-screen view MUST show the image on a black background; any area not covered by
  the image MUST be black.
- **FR-015**: In the full-screen view, users MUST be able to zoom freely, at least enough to fill the
  screen and beyond, up to a very high but bounded maximum (about 20× the fit size).
- **FR-016**: In the full-screen view, users MUST be able to move the image with a single-finger drag.
- **FR-017**: A single tap on the image or the back action MUST exit the full-screen view and return
  to the image detail.
- **FR-018**: The full-screen view MUST open at the fit view, and exiting MUST return the detail image
  to fit.
- **FR-019**: The full-screen view MUST be reachable only from the image detail screen, never from the
  gallery or any grid view.
- **FR-020**: Pinch zooming MUST keep the point under the midpoint between the two fingers (the pinch
  centroid) fixed, so the zoom focuses on the fingers rather than the screen center.
- **FR-021**: A drag MUST move the image by the same distance the finger(s) travel, at every zoom
  level; the image movement MUST NOT be reduced as the zoom increases.

### Key Entities *(include if feature involves data)*

Not applicable: this feature changes image interaction behavior only and introduces no new data.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of image views in the app support pinching to zoom, two-finger panning, and
  returning to the fit view.
- **SC-002**: A user can zoom in, pan to any part of the image, and return to the fit view in one
  continuous sequence of gestures without leaving the screen.
- **SC-003**: Across 50 consecutive zoom/pan/scroll cycles there are 0 stuck states (cases where a
  gesture stops responding) and 0 crashes.
- **SC-004**: After zooming in, the user can scroll the surrounding page again within a single
  gesture.
- **SC-005**: The image never disappears entirely from the viewport, regardless of pan direction or
  speed.
- **SC-006**: Returning to the fit view always restores the centered, fully visible image.
- **SC-007**: A single tap in the image detail opens the full-screen view, and a single tap or the
  back action closes it, in 100% of attempts.
- **SC-008**: In the full-screen view, a user can zoom to fill the screen and beyond and move to any
  part of the image with a single finger.
- **SC-009**: The area of the screen not covered by the image in the full-screen view is always black.
- **SC-010**: Panning moves the image by the same distance as the finger(s) at every zoom level (a long
  finger drag produces an equally long image movement).
- **SC-011**: Pinching keeps the area between the fingers in place (the pinch centroid stays fixed)
  instead of zooming toward the screen center.

## Assumptions

- On the detail page, the image sits inside a vertically scrollable page: one finger scrolls the page
  while two fingers zoom and pan the image. In the full-screen view there is no page to scroll, so one
  finger moves the image and pinching zooms.
- The full-screen view is a state of the image detail screen, reached by tapping the image there; it is
  not available from the gallery or any grid view (Clarifications, 2026-10-15).
- The full-screen view opens at the fit view; exiting (single tap or back) returns the detail image to
  fit (Clarifications, 2026-10-15).
- The full-screen background is black, including the areas the image does not cover; the app and
  system bars may be hidden while it is open.
- "Zoom back" is satisfied by pinching inward to the fit scale, by double-tap toggling, and by a
  reset action.
- The detail image keeps a modest bounded maximum zoom (a small multiple of the fit size); the
  full-screen view uses a much higher but still bounded maximum (about 20× the fit size) so it can
  fill the screen and deliberately over-zoom (Clarifications, 2026-10-15).
- The same zoom/pan behavior applies to all full-view images in the app, since they share the same
  image view.
- Zoom and pan are display-only and do not change the stored wallpaper or any saved data.
