# Wallhaven Client Constitution

## Core Principles

### I. Layered Architecture & Dependency Direction

The codebase is organized into `data/` (with `api`, `db`, `prefs`, `repository`), `domain/`,
`ui/` (`screens`, `viewmodels`, `components`), `di/`, and `util/`. New code MUST be placed in
the layer that owns its responsibility; cross-layer shortcuts are forbidden.

- The UI layer MUST access data exclusively through ViewModels and repositories. Compose
  screens and UI components MUST NOT reference Retrofit services, Room DAOs, or DataSources
  directly.
- DTOs MUST live in `data/api/dto` and MUST NOT leak into the `ui/` or `domain/` layers;
  mapping to domain/UI models happens inside the data layer.
- Dependencies MUST point inward: `ui/` depends on `domain/` and repository abstractions;
  `data/` implements those abstractions. `domain/` MUST NOT depend on Android framework
  details or `data/` implementation classes.
- Dependency injection MUST use Hilt. Manual service locators and static singletons for
  dependencies are forbidden.

Rationale: this keeps the app testable and prevents API and database concerns from coupling
to presentation logic.

### II. Room as Single Source of Truth (Offline-First & Paging)

The local Room database is the single source of truth for browsed and favorited wallpapers.
The gallery and favorites MUST remain fully usable offline.

- Every wallpaper viewed, and every wallpaper returned by the gallery or search, MUST be
  persisted locally so it can be displayed and favorited without network access.
- Favorites MUST be stored in the local Room database.
- All paging MUST use Paging 3. Every write (insert, update, favorite, delete) MUST keep the
  Paging cache coherent and MUST be observable through the Room-backed
  `PagingSource`/`RemoteMediator`.
- The network is a refresh source, not a display source: all UI read paths MUST originate from
  the database.

Rationale: offline-first behavior and a coherent Paging cache are non-negotiable user-facing
guarantees of this app.

### III. Secrets & Privacy by Default

- The wallhaven API key MUST be stored only in the Android Keystore-backed secure store (a
  dedicated preferences file).
- The API key MUST be excluded from backups and device transfer via `backup_rules.xml` and
  `data_extraction_rules.xml`.
- The API key MUST NEVER be written to the wallpaper database, MUST NEVER appear in a request
  URL, and MUST NEVER appear in logs. It MUST be transmitted only through the mechanism
  required by the API.

Rationale: the key is a personal credential; leaking it via storage, URLs, or logs is a
privacy and security failure.

### IV. Localization Completeness

- Every user-visible string MUST exist in both `values/` (English) and `values-it/` (Italian).
- UI code MUST NOT contain hardcoded user-visible strings; all strings MUST be referenced
  through Android string resources.

Rationale: the app ships in English and Italian, and untranslated or hardcoded strings break
that contract.

### V. Verification & Quality Gate

- Every change MUST be verified by the repository test suite: JVM unit tests using JUnit4,
  MockK, OkHttp MockWebServer, and kotlinx-coroutines-test.
- The quality gate is `./gradlew test assembleDebug`; it MUST pass before a change is complete.
  The `Test` workflow runs it on every push.
- Tests MUST run on the JVM and MUST NOT require a device or emulator. Network behavior is
  verified with MockWebServer, never with live calls.

Rationale: deterministic, CI-runnable tests are the only verification path because no local
Android SDK exists on the development VM.

## Technology Stack & Platform Constraints

- Stack: Kotlin + Jetpack Compose (Material 3), Hilt for DI, Room + Paging 3, Retrofit with
  kotlinx.serialization, Coil, Navigation Compose.
- Platform: minSdk 26, targetSdk/compileSdk 37, JVM 17.
- The stack MUST NOT be replaced; changing it requires a spec and a constitution amendment.
- Code MUST follow the repository `.editorconfig`: tabs, LF line endings, final newline, no
  trailing whitespace.
- Existing public APIs and behavior MUST remain compatible unless a spec explicitly changes
  them.
- `applicationId` is `dev.giovannidrago.wallhaven`; this is a personal, non-Play-Store APK.

## Development Workflow, Licensing & Release

- License: GPL-3.0. The MIT attribution to Wallhavend for the secure API-key pattern MUST be
  preserved.
- Release artifacts are produced ONLY by the "Personal Release" workflow (manual dispatch,
  keystore from secrets). Installable debug APKs are produced by the "Test" workflow.
- There is no local Android SDK on the development VM; builds happen in CI.
- Every feature MUST be specified, planned, and implemented through Spec Kit and MUST verify
  compliance with these principles.

## Governance

- Supremacy: this constitution supersedes all other practices. Specs, plans, and
  implementations under Spec Kit MUST verify compliance with these principles.
- Amendment procedure: an amendment MUST be proposed and documented, MUST update this file,
  and MUST be recorded in a Sync Impact Report. Amendments that change principles or governance
  MUST bump `CONSTITUTION_VERSION`.
- Versioning policy (semantic versioning): MAJOR for backward-incompatible governance or
  principle removals/redefinitions; MINOR for a new principle/section or materially expanded
  guidance; PATCH for clarifications, wording, and non-semantic refinements.
- Compliance review: all PRs and reviews MUST verify compliance. Non-compliant changes MUST
  NOT be merged.
- Dates are ISO format `YYYY-MM-DD`.

**Version**: 1.0.0 | **Ratified**: 2026-10-13 | **Last Amended**: 2026-10-13
