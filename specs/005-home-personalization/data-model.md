# Phase 1 Data Model: Home Personalization

No Room schema changes. Two layers of configuration share one model: the **persisted baseline** in
DataStore and an **in-memory session override** for the drawer's quick filters. The home always uses
the **effective** combination.

## Model: `WallhavenFilters` (extended)

| Field | Type | Default | Notes |
|-------|------|---------|-------|
| `sorting` | `Sorting` | `LATEST` | `LATEST`, `TOPLIST_MONTH`, `RANDOM` |
| `general` | `Boolean` | `true` | Category |
| `anime` | `Boolean` | `true` | Category |
| `people` | `Boolean` | `true` | Category |
| `sfw` | `Boolean` | `true` | Purity (full set) |
| `sketchy` | `Boolean` | `false` | Purity |
| `nsfw` | `Boolean` | `false` | Purity; requires the API key |
| `color` | `String?` | `null` | Single wallhaven color |
| `atleast` | `String?` | `null` | Minimum resolution |
| `resolutions` | `List<String>` | empty | Exact resolutions (comma-joined, sorted) |
| `ratios` | `List<String>` | empty | Exact ratios (comma-joined, sorted) |

Derived: `categories` (3-digit mask), `purity` (sfw/sketchy/nsfw mask), `categoryCount`, `purityCount`,
`resolutionsValue`, `ratiosValue`, `key`, `toQuery()`.

### Query key

`<sorting>|<categories>|<purity>` plus only the non-default refinement segments
(`|<color>|<atleast>|<resolutions>|<ratios>`). The default configuration therefore keeps the existing
home cache key; a personalized configuration gets its own cached pages.

### Query

`toQuery()` fills `color`, `atleast`, `resolutions` and `ratios` (null when empty) in addition to the
existing sorting/categories/purity fields.

## Persisted baseline (DataStore)

| Key | Type | Notes |
|-----|------|-------|
| existing filter keys | — | Reused for sorting/categories/purity |
| `home_sfw` | `Boolean` | Default `true` |
| `home_color` | `String?` | Removed when cleared |
| `home_atleast` | `String?` | Removed when cleared |
| `home_resolutions` | `Set<String>` | Removed when empty |
| `home_ratios` | `Set<String>` | Removed when empty |

## Session override (in memory)

| Field | Type | Notes |
|-------|------|-------|
| `override` | `WallhavenFilters?` | `null` until the user changes a drawer quick filter; lives in the singleton repository and dies with the process. |

Overridable fields (the drawer subset): `sorting`, `general`, `anime`, `people`, `sketchy`, `nsfw`.
`sfw` and the refinements are never overridden by the quick filters.

## Effective filters

```text
effective = override?.mergeQuick(persisted) ?: persisted
mergeQuick: persisted.copy(
  sorting = override.sorting,
  general/anime/people = override.*,
  sketchy/nsfw = override.*
)
```

## State Transitions

```text
App start                ──► override = null, effective = persisted
Drawer quick change      ──► override = effective.copy(quick field), effective updates immediately
Home personalization edit ──► persisted updated; the edited field's override is cleared
Reset                    ──► persisted = defaults; override = null
Process death/restart    ──► override lost, effective = persisted
```

## Validation Rules

| Rule | Source |
|------|--------|
| At least one category remains enabled | FR-010 |
| NSFW only with a saved API key (existing mediator guard) | FR-009 |
| Quick filters never persist | FR-014 |
| Defaults reproduce today's home | FR-006 |
| Query key changes with the effective filters | FR-004 |
| Home still reads from Room (offline) | FR-012 |
