# AGENTS.md — wallhaven-client

Android Kotlin/Compose client (Room, Keystore) gestito con **spec-kit** (`opencode` integration
1.1.2). Questo file è la mappa della pipeline: quale fase esegue quale agente, come si misura la
coverage e quali regole non si violano mai.

## Vincoli assoluti

- **Non modificare i comandi generati** `.opencode/commands/speckit.*.md`, né i file tracciati in
  `.specify/integrations/*.manifest.json`: `specify integration status` deve restare invariato.
  Le personalizzazioni vivono in `.opencode/agents/`, `.opencode/commands/{coverage,review-spec,
  impl-parallel}.md`, in questo file e nello workflow CI.
- **Nessun commit, push o run CI** (`gh workflow run`, `gh run rerun`) senza approvazione esplicita
  dell'utente nella conversazione corrente.
- **Non toccare `app/src/main`** fuori dallo scope del task approvato. L'unica eccezione ammessa è
  la strumentazione di build necessaria a misurare la coverage (plugin/report, workflow).
- **Niente build locale**: questa VM non ha Android SDK (`ANDROID_HOME` vuoto). `./gradlew` non è
  eseguibile; la verifica passa dalla CI. Non inventare risultati di test o coverage.
- Il repo è privato e l'APK è personale: nessuna esposizione pubblica, nessun secret nei file.

## Mappatura fase → agente

| Fase / comando | Agente | Note |
|---|---|---|
| `/speckit.constitution` | primary | invariato |
| `/speckit.specify` | primary | invariato |
| `/speckit.clarify` | primary | invariato |
| `/speckit.plan` | primary | invariato |
| `/speckit.tasks` | primary | invariato |
| `/speckit.analyze` | **reviewer** | analisi read-only dei tre artefatti |
| `/speckit.checklist` | **reviewer** | checklist come review, nessuna modifica |
| `/speckit.implement` | **speckit-orchestrator** | un `task-worker` per task, poi `reviewer` |
| `/speckit.converge` | **reviewer** | verifica dei task aggiunti prima del commit |
| `/speckit.git.*` | primary | commit/push solo su approvazione |
| `/speckit.apk-delivery.*` | primary | delivery APK via CI |
| `/coverage` | **coverage-analyst** | solo artifact CI, read-only |
| `/review-spec <feature>` | **reviewer** | read-only |
| `/impl-parallel <feature>` | **speckit-orchestrator** | orchestrazione implement |

Agenti in `.opencode/agents/`:

- `speckit-orchestrator` (primary): può lanciare **solo** `task-worker`, `reviewer`,
  `coverage-analyst`; il permesso `subagent` nega tutto il resto.
- `task-worker` (subagent, `opencode-go/deepseek-v4.1-flash#high`): un task, scope di file
  esplicito, acceptance criteria; shell limitata a `git status/diff/log`, niente Gradle.
- `reviewer` (subagent): `edit` e `shell` in deny; finding in ordine di severità.
- `coverage-analyst` (subagent): `edit` in deny, shell solo `gh run …`, `gh api …`, `unzip …`.

## Coverage dei test unitari

Strumentazione (AGP 9, non la DSL di AGP 8):

- `app/build.gradle.kts` usa la DSL per-variante AGP 9:
  `beforeVariants(selector().withBuildType("debug")) { variantBuilder ->
  variantBuilder.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enableCodeCoverage = true }`
  (non il legacy `buildTypes.debug.enableUnitTestCoverage`). AGP applica JaCoCo da solo e registra il
  task **`createDebugUnitTestCoverageReport`** (HTML + `report.xml`).
- CI (`.github/workflows/test.yml`, con `workflow_dispatch`) esegue
  `./gradlew test assembleDebug createDebugUnitTestCoverageReport` e pubblica l'artifact
  `wallhaven-client-coverage-<run_number>` con:
  - `app/build/reports/coverage/**` (HTML + `report.xml`);
  - `app/build/outputs/unit_test_code_coverage/**` (file `.exec`).
- Download: `gh run download <run-id> -n wallhaven-client-coverage-<run_number> -D /tmp/opencode/coverage`.

### Baseline attesa

Misurata sui test unitari JVM (`app/src/test/java/dev/giovannidrago/wallhaven/`):

| Spec | Classi/sorgenti in baseline | Test di riferimento |
|---|---|---|
| 002 — zoom/pan | `ui/components/ZoomPan.kt` (`fitContentSize`, `clampScale`, `maxTranslation`, `clampOffset`, `ZoomPanState`, `transform`) | `ZoomPanTest` |
| 004 — filtri drawer | `domain/WallhavenFilters.kt`, `domain/WallhavenQuery.kt`, `domain/SearchOptions.kt` | `WallhavenFiltersTest`, `WallhavenQueryTest`, `SearchOptionsTest` |
| 004 — update check | `data/repository/UpdateRepository.kt`, `domain/VersionComparison.kt` | `UpdateRepositoryTest`, `VersionComparisonTest` |
| 006 — preset tema | `domain/ThemeSettings.kt`, `ui/theme/ThemePresets.kt` | `ThemeSettingsTest`, `ThemePresetTest` |

Fuori scope dichiarato (non misurato o non misurabile con test JVM):

- Compose UI: `ui/components/DrawerContent.kt`, `ui/components/UpdateAvailableDialog.kt`,
  `ui/components/ZoomableImage.kt`, `ui/settings/SettingsScreen.kt`, `ui/MainScreen.kt`;
- DAO/Room e database reale (`data/db/*`), Hilt/DI (`di/*`), `MainActivity`;
- wrapper DataStore (`data/prefs/FilterPreferences.kt`, `UpdatePreferences.kt`,
  `ThemePreferences.kt`): esercitati solo tramite mock nei repository test, non misurati
  direttamente.

## Convenzioni di lavoro

- Risposte e report in italiano, con path dei file; separare sempre fatti misurati da stime.
- Un task = un cambiamento coerente; niente refactor fuori scope.
- Se un task richiede una modifica fuori dallo scope concordato, fermarsi e chiedere.
