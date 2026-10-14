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

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3) on user-story phase tasks only
- Every task includes an exact file path

## Path Conventions

Single Android application module. Component under
`app/src/main/java/dev/giovannidrago/wallhaven/ui/components/`, tests under
`app/src/test/java/dev/giovannidrago/wallhaven/`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the change needs no new dependencies and no new user-visible strings.

- [ ] T001 Confirm the feature requires no new Gradle dependencies (leave `app/build.gradle.kts` and `gradle/libs.versions.toml` unchanged) and no new user-visible strings (leave `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml` unchanged unless a text control is added)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: The pure zoom/pan math that every story depends on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T002 [P] Create `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomPan.kt` with pure functions `fitContentSize(container, imageWidth, imageHeight)`, `clampScale(scale)` and constants `MIN_SCALE = 1f`, `MAX_SCALE = 6f`, `DOUBLE_TAP_SCALE`, `maxTranslation(container, content, scale)` and `clampOffset(offset, bounds)`, guarding zero and non-finite values
- [ ] T003 [P] Write `app/src/test/java/dev/giovannidrago/wallhaven/ZoomPanTest.kt`: `clampScale` clamps to `[1, 6]`; `maxTranslation` is zero when the scaled content fits the viewport and grows with scale; `clampOffset` keeps the offset inside the bounds; the fit view is centered (`bounds = Offset.Zero` → `offset = Offset.Zero`); zero/oversized inputs never produce `NaN` or infinite values

**Checkpoint**: Math and its tests are ready - all user stories can now begin.

---

## Phase 3: User Story 1 - Zoom and move freely with two fingers (Priority: P1) 🎯 MVP

**Goal**: Two-finger pinch zooms in/out and two-finger drag pans the zoomed image, following the
fingers, within bounded limits.

**Independent Test**: Open any image, pinch to zoom in, two-finger drag to a corner, and pinch back to
the fit view; each step follows the gesture.

### Implementation for User Story 1

- [ ] T004 [US1] Rewrite the gesture handling in `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` to use `Modifier.pointerInput` + `awaitEachGesture`; when two or more pointers are down, read `calculateZoom()`, `calculatePan()` and `calculateCentroid()`, consume those changes, and update `scale` via `clampScale` and `offset` via `clampOffset`; keep the public signature `ZoomableImage(url: String, modifier: Modifier = Modifier)`
- [ ] T005 [US1] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` track the viewport size with `onSizeChanged` and the image intrinsic size from the `SubcomposeAsyncImage` painter state, derive the fitted content size with `fitContentSize`, and drive `graphicsLayer { scaleX; scaleY; translationX; translationY }` from the clamped state

**Checkpoint**: US1 is functional and testable on its own (free bounded two-finger zoom/pan).

---

## Phase 4 - User Story 2 - Gestures never trap the user (Priority: P1)

**Goal**: One finger always scrolls the page; zoom/pan/scroll always remain available; no crash or
freeze.

**Independent Test**: Repeatedly zoom, pan and scroll a long detail page; after every gesture the next
zoom/pan/scroll responds immediately.

### Implementation for User Story 2

- [ ] T006 [US2] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` do not consume single-pointer drags (let the parent scrollable page handle them), key `pointerInput` by the image `url` so state resets cleanly, and ignore gestures until the container is measured and the image is loaded
- [ ] T007 [US2] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` re-clamp `offset` whenever the viewport size changes (e.g. rotation) and guard against zero/non-finite sizes; extend `app/src/test/java/dev/giovannidrago/wallhaven/ZoomPanTest.kt` with cases for these guards

**Checkpoint**: US1 and US2 both work; the screen never gets stuck.

---

## Phase 5 - User Story 3 - Always recover the full view (Priority: P2)

**Goal**: The user can always return to the centered fit view, and the image can never be lost
off-screen.

**Independent Test**: Zoom, pan to an edge, then double-tap and pinch out; confirm the image returns
centered at fit scale and never leaves the viewport.

### Implementation for User Story 3

- [ ] T008 [US3] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` reset `scale`/`offset` to the fit view whenever `url` changes (`LaunchedEffect(url)`), and implement double-tap to toggle between the fit view and a zoomed view centered on the tap point (`DOUBLE_TAP_SCALE`)
- [ ] T009 [US3] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` ensure pinching out to `MIN_SCALE` restores the exact centered fit (`offset = Offset.Zero`) and that `clampOffset` keeps a visible portion of the image on screen in every direction

**Checkpoint**: All three user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Localization safety and the quality gate.

- [ ] T010 [P] Verify no hardcoded user-visible strings were introduced in the component; if any control with visible text was added, add matching keys to `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml` (Principle IV)
- [ ] T011 Run the quality gate `./gradlew test assembleDebug` (CI `Test` workflow) and execute the manual scenarios Z1–Z4 in `specs/002-image-zoom-pan/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup; BLOCKS all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational.
- **User Story 2 (Phase 4)**: Depends on Foundational; builds on the US1 gesture rewrite (same file).
- **User Story 3 (Phase 5)**: Depends on Foundational; builds on the US1 gesture rewrite (same file).
- **Polish (Phase 6)**: Depends on all targeted stories.

### Within Each User Story

- The pure math and its tests (T002/T003) come first.
- `ZoomableImage.kt` tasks (T004–T009) touch the same file and MUST run sequentially.

### Parallel Opportunities

- T002 and T003 are different files and run in parallel.
- T001 and T010 are independent of the component edits.
- The user-story phases (US1/US2/US3) all edit the same component file, so they run sequentially even
  though they are logically independent.

---

## Parallel Example: Foundational

```bash
Task: "Create pure math in ui/components/ZoomPan.kt"
Task: "Write ZoomPanTest.kt covering clampScale/maxTranslation/clampOffset/guards"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup) and Phase 2 (Foundational).
2. Complete Phase 3 (US1): free bounded two-finger zoom/pan.
3. **STOP and VALIDATE**: `./gradlew test assembleDebug` passes; zoom/pan follows the fingers within
   bounds.
4. Demo the MVP.

### Incremental Delivery

1. Setup + Foundational → math ready.
2. US1 → two-finger zoom/pan (MVP).
3. US2 → one-finger scroll preserved, no stuck state.
4. US3 → reset/double-tap and never-lost bounds.
5. Polish → localization check and the quality gate.

---

## Notes

- [P] tasks touch different files and have no incomplete dependencies.
- The component's public API (`ZoomableImage(url, modifier)`) is unchanged, so all image screens get
  the fix with no call-site edits.
- No data, no migrations, no new dependencies.
- Commit after each task or logical group.
