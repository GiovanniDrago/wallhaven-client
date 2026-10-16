# UI Contracts: Fix Zoom Recenter on Finger Release

The change is a behaviour correction on the shared image component; the component signature is
unchanged.

## Release Behaviour Contract

`ZoomableImage` (`url`, `modifier`, `maxScale`, `singleFingerPan`, `fillParent`, `onSingleTap`).

- **While fingers are down**: pinch zoom and drag pan behave exactly as today.
- **On release**: the moment the last finger leaves the screen, the current `scale` and `offset` are
  retained. The view MUST NOT move, re-center, or change zoom (FR-001, FR-002).
- **No side effects**: no jump, flicker, or crash on the release frame (FR-005).
- **Everywhere**: the behaviour applies to the inline detail image and the full-screen viewer, since
  both use this component (FR-004).

## Pure Math Contract

`transform(scale, offset, centroid, container, content, zoomChange, pan, maxScale)`:

- For usable inputs, returns the next state as before.
- For unusable inputs (non-finite/unspecified `centroid`, non-finite `pan` or `zoomChange`), returns
  the incoming `scale` and `offset` unchanged, so a release frame can never recenter or rescale the
  view.

## Non-Goals

- No API signature change.
- No new user-visible strings.
