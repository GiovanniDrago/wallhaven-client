---

description: "Task list for Fix Zoom Recenter on Finger Release"
---

# Tasks: Fix Zoom Recenter on Finger Release

**Input**: Design documents from `/specs/003-fix-zoom-recenter/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included. Constitution Principle V requires JVM verification, so the pure `transform(...)`
guard is covered by a unit test.

**Organization**: One user story (the release behaviour fix), so a single story phase.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1) on user-story phase tasks only
- Every task includes an exact file path

## Path Conventions

Single Android application module. Component under
`app/src/main/java/dev/giovannidrago/wallhaven/ui/components/`, tests under
`app/src/test/java/dev/giovannidrago/wallhaven/`.

---

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Confirm the fix needs no new Gradle dependencies and no new user-visible strings (leave `app/build.gradle.kts`, `gradle/libs.versions.toml`, `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml` unchanged)

---

## Phase 2: User Story 1 - Lifting the fingers keeps the focused area (Priority: P1) 🎯 MVP

**Goal**: The moment the fingers are lifted, the image keeps its current zoom and position instead of
snapping back to the center, in both the inline detail image and the full-screen viewer.

**Independent Test**: Zoom/pan to a corner, lift the fingers, and confirm the view does not move; check
the inline image and the full-screen view.

- [ ] T002 [P] [US1] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomPan.kt` guard `transform(...)` so it returns the incoming `scale`/`offset` unchanged when `centroid`, `pan`, or `zoomChange` is not finite (FR-001, FR-005)
- [ ] T003 [P] [US1] In `app/src/test/java/dev/giovannidrago/wallhaven/ZoomPanTest.kt` add a case asserting `transform(...)` returns the incoming `scale`/`offset` unchanged for a non-finite/unspecified centroid (FR-001, FR-005)
- [ ] T004 [US1] In `app/src/main/java/dev/giovannidrago/wallhaven/ui/components/ZoomableImage.kt` stop transforming once no pointer is pressed (read the pressed count and end the gesture before transforming) so the release frame never updates `scale`/`offset`; keep the while-fingers-down behaviour unchanged (FR-001, FR-002, FR-003, FR-004)

**Checkpoint**: US1 works — lifting the fingers no longer recenters the image, inline and full-screen.

---

## Phase 3: Polish & Cross-Cutting Concerns

- [ ] T005 [P] Confirm no user-visible strings were introduced and both locale files are unchanged in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-it/strings.xml` (Principle IV)
- [ ] T006 Run the quality gate `./gradlew test assembleDebug` (CI `Test` workflow) and execute the manual scenarios F1–F3 in `specs/003-fix-zoom-recenter/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **User Story 1 (Phase 2)**: Depends on Setup; T002 and T003 are different files (parallel), T004
  uses `transform(...)` and follows T002.
- **Polish (Phase 3)**: Depends on US1.

### Within the Story

- T002 (guard) before T004 (which relies on the guarded `transform`).
- T004 touches `ZoomableImage.kt`; run after T002.

### Parallel Opportunities

- T002 and T003 are different files and can run in parallel.
- T005 is independent of the code edits.

---

## Implementation Strategy

### MVP

1. Complete Phase 1 (Setup).
2. Complete Phase 2 (US1): guard `transform`, guard the gesture loop, add the JVM test.
3. **STOP and VALIDATE**: `./gradlew test assembleDebug` passes; lifting the fingers keeps the view.
4. Polish: localization check and the quality gate.

---

## Notes

- [P] tasks touch different files and have no incomplete dependencies.
- The component signature is unchanged; only release behaviour is corrected.
- No data, migrations, dependencies, or strings.
- Commit after each task or logical group.
