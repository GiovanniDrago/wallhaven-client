# Quickstart Validation: Home Personalization

This guide proves the feature end-to-end. It contains no implementation code; details live in
[spec.md](./spec.md), [plan.md](./plan.md), [data-model.md](./data-model.md) and
[contracts/](./contracts/ui-contracts.md).

## Prerequisites

- JDK 17. No local Android SDK is required; builds and tests run in CI.
- For manual checks, install the debug/personal APK.

## Automated Verification (quality gate)

```bash
./gradlew test assembleDebug
```

New/updated tests:

- `WallhavenFiltersTest` — extended fields, purity mask (sfw/sketchy/nsfw), query key (default key
  unchanged; refinements produce a different key) and `toQuery()` (color/atleast/resolutions/ratios).
- `HomePersonalizationRepositoryTest` — effective merge, session override precedence, baseline update
  clearing the override, reset, and restart semantics (a new repository instance has no override).

## Manual Scenarios

### H1 — Settings screen (US1, FR-001/FR-002/FR-003)

1. Open the drawer: a "Home personalization" entry is present.
2. Open it: sorting, categories, purity, color, resolution and format controls are shown, plus reset.
3. Change several values, leave and reopen: the values are still selected.

### H2 — Home uses the personalization (US2, FR-004/FR-005/FR-006)

1. Set a clear personalization (e.g. top of the month, anime only, sketchy allowed).
2. Return to the home and pull to refresh: the feed matches those settings.
3. Change a setting and return to the home: the feed reflects it without a restart.
4. Reset and confirm the home matches today's default behavior.

### H3 — Quick filters are session overrides (US3, FR-007/FR-013/FR-014/FR-015)

1. With a saved personalization, open the drawer: the quick filters show the same values.
2. Change a quick filter: the home follows it immediately and stays overridden for the session.
3. Restart the app: the quick filters and the home return to the saved personalization.
4. Change the same value in Home personalization: the saved baseline updates and the quick filter
   realigns.

### H4 — Guards (FR-009/FR-010)

1. Without an API key, NSFW stays unavailable, as today.
2. Try to turn off the last category: it is prevented.

### H5 — Offline (FR-012)

1. Load the home online with a personalization, then go offline.
2. Reopen the home: the cached results for that configuration are still shown.

## Expected Outcomes

- All JVM tests pass and the APK assembles in CI.
- The home is filtered by the personalization on every load/refresh.
- Quick filters override only for the session and never change the saved baseline.
- Defaults reproduce the current home; offline still works from the Room cache.
