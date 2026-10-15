---

description: "Task list for Image Zoom and Pan implementation"
---

# Tasks: Image Zoom and Pan

**Input**: Design documents from `/specs/002-image-zoom-pan/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included. Constitution Principle V requires every change to be verified by the repository
JVM test suite, so the pure zoom/pan math is covered by unit tests.

**Organization**: Tasks are grouped by user story from spec.md to allow independent implementation
and testing. There is no persistence, so there are no entities or migrations.

**Status**: US1–US3 are implemented and CI-verified (marked `[X]`). User Story 4 (full-screen viewer)
and the re-run polish tasks are pending.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3, US4) on user-story phase tasks only
- Every task includes an exact file path

## Path Conventions

Single Android application module. Component under
`app/src/main/java/dev/giovannidrago/wallhaven/ui/components/`, detail screen under
`app/src/main/java/dev/giovannidrago/wallhaven/ui/detail/`, tests under
`app/src/test/java/dev/giovannidrago/wallhaven/`.

---

## Phase 1: Setup (Shared Infrastructure)

- [X] T001 Confirm the feature requires no new Gradle dependencies (leave `app/build.gradle.kts` and `gradle/libs.versions.toml` unchanged) and no new user-visible strings (leave `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml` unchanged unless a text control is added)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: The pure zoom/pan math that every story depends on.

- [X] T002 [P] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomPan.kt` with pure functions `fitContentSize`, `clampScale`, `maxTranslation`, `clampOffset` and constants `MIN_SCALE = 1f`, `MAX_SCALE = 6f`, `DOUBLE_TAP_SCALE`, guarding zero and non-finite values
- [X] T003 [P] Write `app/src/test/java/dev/giovannidrago/wallhaven/ZoomPanTest.kt` covering `clampScale` bounds, `maxTranslation`, `clampOffset`, centered fit, and zero/non-finite guards

**Checkpoint**: Math and its tests are ready.

---

## Phase 3: User Story 1 - Zoom and move freely with two fingers (Priority: P1) 🎯 MVP

**Goal**: Two-finger pinch zooms in/out and two-finger drag pans the zoomed image within bounded
limits.

**Independent Test**: Open any image, pinch to zoom in, two-finger drag to a corner, and pinch back
to the fit view.

- [X] T004 [US1] Rewrite the gesture handling in `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` to use `pointerInput` + `awaitEachGesture`; with two or more pointers consume `calculateZoom()`/`calculatePan()` and update `scale` via `clampScale` and `offset` via `clampOffset`; keep the signature `ZoomableImage(url, modifier)`
- [X] T005 [US1] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` track viewport size (`onSizeChanged`) and the image intrinsic size (Coil `onSuccess`), derive the fitted content size, and drive `graphicsLayer` from the clamped state

**Checkpoint**: US1 functional (free bounded two-finger zoom/pan).

---

## Phase 4: User Story 2 - Gestures never trap the user (Priority: P1)

**Goal**: One finger always scrolls the page; zoom/pan/scroll always remain available.

**Independent Test**: Repeatedly zoom, pan and scroll; after every gesture the next one responds.

- [X] T006 [US2] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` do not consume single-pointer drags (parent scroll), key `pointerInput` by the image `url`, and ignore gestures until measured/loaded
- [X] T007 [US2] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` re-clamp `offset` when the viewport size changes and guard zero/non-finite sizes; extend `app/src/test/java/dev/giovannidrago/wallhaven/ZoomPanTest.kt` with these guards

**Checkpoint**: US1 and US2 both work; no stuck state.

---

## Phase 5: User Story 3 - Always recover the full view (Priority: P2)

**Goal**: Always return to the centered fit view; the image can never be lost.

**Independent Test**: Zoom, pan to an edge, then double-tap and pinch out.

- [X] T008 [US3] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` reset `scale`/`offset` on `url` change and implement double-tap to toggle between fit and a zoomed view centered on the tap
- [X] T009 [US3] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` ensure pinching out to `MIN_SCALE` restores the centered fit and `clampOffset` keeps a visible portion on screen

**Checkpoint**: US1–US3 independently functional.

---

## Phase 6: User Story 4 - Full-screen image view (Priority: P2)

**Goal**: A single tap on the detail image opens it full screen on a black background; zoom freely to
fill and beyond; move with one finger; exit with a single tap or back; only reachable from the detail.

**Independent Test**: Open a detail, tap the image, confirm the black full-screen view; zoom and move
with one finger; exit with a tap and again with back.

### Implementation for User Story 4

- [X] T010 [US4] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomPan.kt` add `FULLSCREEN_MAX_SCALE = 20f` and a `max` parameter to `clampScale(scale: Float, max: Float = MAX_SCALE)`; extend `app/src/test/java/dev/giovannidrago/wallhaven/ZoomPanTest.kt` with cases for the higher cap (FR-015, FR-012)
- [X] T011 [P] [US4] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` add optional parameters `maxScale: Float = MAX_SCALE`, `singleFingerPan: Boolean = false`, `onSingleTap: (() -> Unit)? = null`, keeping existing call sites unchanged (contracts: component contract)
- [X] T012 [US4] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` implement `singleFingerPan` (consume a one-finger drag and pan the image) and invoke `onSingleTap` on a single tap so it does not fire when a double tap zooms (FR-016, edge cases)
- [X] T013 [US4] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/detail/DetailScreen.kt` add the full-screen overlay: a `fullScreen` state, a `Box(fillMaxSize).background(Color.Black)` drawn above the `Scaffold` hosting a `ZoomableImage(maxScale = FULLSCREEN_MAX_SCALE, singleFingerPan = true, onSingleTap = exit)`, and `BackHandler(enabled = fullScreen)` to exit (FR-013, FR-014, FR-017)
- [X] T014 [US4] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/detail/DetailScreen.kt` key the inline image on the `fullScreen` flag so exiting resets it to fit, and wire the inline image's `onSingleTap` to open the viewer; leave the gallery/grid call sites unchanged so the viewer is reachable only from the detail (FR-018, FR-019)

**Checkpoint**: All four user stories are independently functional.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T015 [P] Verify no hardcoded user-visible strings were introduced; if any control with visible text was added, add matching keys to `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml` (Principle IV)
- [X] T016 Run the quality gate `./gradlew test assembleDebug` (CI `Test` workflow) and execute the manual scenarios Z1–Z5 in `specs/002-image-zoom-pan/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup; BLOCKS all user stories.
- **US1 (Phase 3)**: Depends on Foundational.
- **US2 (Phase 4)**: Depends on US1 (same file).
- **US3 (Phase 5)**: Depends on US1 (same file).
- **US4 (Phase 6)**: Depends on Foundational (ZOOM math) and US1 (the component); touches
  `ZoomableImage.kt` and `DetailScreen.kt`.
- **Polish (Phase 7)**: Depends on all stories.

### Within Each User Story

- The pure math and its tests come before the component work.
- `ZoomableImage.kt` tasks (T004–T012) touch the same file and MUST run sequentially.
- `DetailScreen.kt` tasks (T013, T014) touch the same file and MUST run sequentially.

### Parallel Opportunities

- T002/T003 are different files.
- T010 (`ZoomPan.kt` + test) and T011 (`ZoomableImage.kt` parameters) are different files and can run
  in parallel; T012/T013/T014 follow in order.
- T015 is independent of the code edits.

---

## Parallel Example: User Story 4

```bash
Task: "Add FULLSCREEN_MAX_SCALE + clampScale max param in ui/components/ZoomPan.kt"
Task: "Add maxScale/singleFingerPan/onSingleTap parameters in ui/components/ZoomableImage.kt"
```

---

## Implementation Strategy

### Incremental Delivery

1. Setup + Foundational + US1–US3 → already implemented and CI-verified.
2. US4 → full-screen viewer (overlay, black background, one-finger pan, high zoom cap, enter/exit).
3. Polish → localization check and the quality gate (re-run after US4).

---

## Notes

- [P] tasks touch different files and have no incomplete dependencies.
- The component's public API gains only optional parameters, so existing call sites are unchanged.
- No data, no migrations, no new dependencies, no new strings.
- Commit after each task or logical group.
