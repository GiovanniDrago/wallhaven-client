# UI Contracts: Home Personalization

## Drawer

- A new "Home personalization" entry is shown among the sections (with a distinct icon) and opens the
  dedicated screen.
- The existing collapsible quick filters remain; they display the **effective** values (baseline plus
  any session override).
- Changing a quick filter applies to the current session only and wins over the baseline; it does not
  change the persisted configuration.

## Home personalization screen

Controls (same groups as the search filters, adapted to the home):

- **Sorting**: latest, top of the month, random.
- **Categories**: general, anime, people (at least one stays on).
- **Purity**: SFW, sketchy, NSFW (NSFW gated by the API key).
- **Color**: none or one wallhaven color.
- **Resolution**: "at least" and a list of exact resolutions.
- **Format**: orientation presets and exact ratios.
- **Reset**: restores the defaults (latest, all categories, SFW, no refinements).

Every change is persisted immediately. Editing a value also clears the corresponding quick-filter
session override, so the new baseline takes effect on the home right away.

## Home feed

- The gallery is driven by the effective filters; it re-queries when they change (settings or quick
  override) and on pull-to-refresh.
- It still reads from Room through the existing pager; offline it shows the cached pages for the
  effective query key.

## Repository contract

`HomePersonalizationRepository`

| Member | Purpose |
|--------|---------|
| `persisted: Flow<WallhavenFilters>` | Saved baseline (DataStore) |
| `effective: Flow<WallhavenFilters>` | Baseline + session override |
| `setQuickSorting/setQuickCategory/setQuickPurity/...` | Update the session override only |
| `updatePersisted(transform)` | Save the baseline and clear the affected override |
| `reset()` | Defaults + clear override |

## Settings

The Settings "default sorting" section edits the persisted baseline sorting through the same repository
and shows the persisted value; it clears any sorting session override.

## Localization

New user-visible text (menu entry, screen title/description, reset) exists in both `values/` and
`values-it/`; the filter labels reuse the existing search/category/purity strings.
