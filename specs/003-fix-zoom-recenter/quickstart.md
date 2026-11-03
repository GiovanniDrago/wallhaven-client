# Quickstart Validation: Fix Zoom Recenter on Finger Release

This guide proves the fix end-to-end. It contains no implementation code; details live in
[spec.md](./spec.md), [plan.md](./plan.md), [data-model.md](./data-model.md) and
[contracts/ui-contracts.md](./contracts/ui-contracts.md).

## Prerequisites

- JDK 17. No local Android SDK is required; builds and tests run in CI.
- For manual checks, install the debug/personal APK.

## Automated Verification (quality gate)

```bash
./gradlew test assembleDebug
```

The relevant test is `ZoomPanTest`: it asserts that `transform(...)` returns the incoming scale and
offset unchanged for unusable inputs (non-finite/unspecified centroid), alongside the existing
geometric tests.

## Manual Scenarios

### F1 — Release keeps the focused area (US1, FR-001/FR-002)

1. Open a wallpaper detail; zoom in and pan to a corner (inline with two fingers, or full-screen with
   one finger).
2. Keep looking at a specific detail, then lift the fingers.
3. Confirm the view does **not** move back to the image center — it stays exactly where it was.

### F2 — Both views (FR-004)

1. Repeat F1 in the inline detail image.
2. Open the full-screen viewer (single tap on the image) and repeat F1 there.

### F3 — Edge releases (FR-005)

1. Release one finger and then the other; confirm no jump at any point.
2. Release at the maximum zoom; confirm the zoom and position are retained.
3. Briefly drag and release at the fit view; confirm nothing moves.

## Expected Outcomes

- All JVM tests pass and the APK assembles in CI.
- Lifting the fingers never recenters or rescales the image.
- No jump, flicker, or crash at release, in both the inline and full-screen views.
