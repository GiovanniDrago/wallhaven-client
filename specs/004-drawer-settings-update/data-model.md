# Phase 1 Data Model: Drawer Filters, Donation Move and Update Check

No Room changes. The update checker keeps one small preference in the existing DataStore and works
with a small result model; the drawer part is UI state only.

## Entities / Value Objects

### Installed version

| Field | Type | Notes |
|-------|------|-------|
| `currentVersion` | `String` | `BuildConfig.VERSION_NAME`, injected into the repository. |

### Latest release (GitHub)

| Field | Type | Notes |
|-------|------|-------|
| `version` | `String` | `tag_name` with a leading `v` removed; build metadata ignored for comparison. |
| `downloadUrl` | `String?` | Chosen APK asset URL, else `null` when no APK exists. |
| `htmlUrl` | `String` | Release page, used when no APK asset exists. |

Asset rule: prefer `WallhavenClient-latest.apk`; otherwise the first asset ending with `.apk`;
otherwise fall back to `htmlUrl`.

### Last check date (DataStore)

| Key | Type | Notes |
|-----|------|-------|
| `last_update_check` | `String?` | ISO local date `yyyy-MM-dd`; absent means "never checked". |

### UpdateCheckResult (sealed)

| Variant | Payload | Meaning |
|---------|---------|---------|
| `UpToDate` | — | Latest version is not newer than the installed one. |
| `UpdateAvailable` | `version`, `downloadUrl` | A newer release exists; `downloadUrl` is the APK asset or the release page. |
| `Error` | — | Network/API/parse failure (silent for the automatic check). |

### Drawer UI state

| Field | Type | Notes |
|-------|------|-------|
| `filtersExpanded` | `Boolean` | Hoisted in `MainScreen`; reset to `false` when the drawer closes. |

## Version Comparison Rules

- Extract the version from `tag_name` by taking the first numeric group matching
  `\d+(\.\d+){0,2}`; a tag like `personal-WallhavenClient-0.5.0-16` yields `0.5.0`. If there is no
  numeric group, treat it as "no update".
- Strip a leading `v`; drop everything after `+` (build metadata).
- Split on `.`; each component parsed as an integer; non-numeric or missing components count as `0`.
- Compare major, then minor, then patch. `latest > current` means an update is available.
- Examples: `v0.5.1` > `0.5.0`; `1.10.0` > `1.9.9`; `1.2` == `1.2.0`; `1.3.0+build` == `1.3.0`.
- `versionName` is bumped manually, so a release with the same `versionName` is not newer by these
  rules (the trailing CI run number is not a semantic version).

## State Transitions

```text
App start
  ├─ last_check == today ──► do nothing (silent throttled)
  └─ otherwise ──► fetch latest
                     ├─ success ──► store today
                     │               ├─ newer ──► show dialog
                     │               └─ not newer ──► nothing (silent)
                     └─ error ──► nothing, do not store (retry next start)

Settings "Check for updates" (manual, always runs)
  ──► fetch latest
        ├─ newer ──► store today, show dialog
        ├─ not newer ──► store today, "no updates" message
        └─ error ──► "could not check" message (do not store)
```

## Validation Rules

| Rule | Source |
|------|--------|
| Numeric major/minor/patch comparison ignoring `v`/build metadata | FR-008 |
| Prefer the latest APK asset, else the first APK, else the release page | FR-010 |
| At most one automatic check per calendar day | FR-013 |
| Manual checks always run | FR-013 |
| No secret is sent or logged | FR-015 |
| Donation removed from drawer, kept in Settings | FR-004/FR-005 |
| Filters collapsed by default on each drawer open | FR-001/FR-002 |
