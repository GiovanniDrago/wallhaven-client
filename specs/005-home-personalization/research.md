# Phase 0 Research: Home Personalization

## R1. Reuse `WallhavenFilters` instead of a new model

- **Decision**: Extend the existing `WallhavenFilters` (the home/drawer model) with `sfw: Boolean =
  true`, `color: String? = null`, `atleast: String? = null`, `resolutions: List<String> =
  emptyList()`, `ratios: List<String> = emptyList()`. Update `purity`, `purityValues`, `key` and
  `toQuery()` accordingly.
- **Rationale**: The home feed already builds its query from `WallhavenFilters`; extending it keeps one
  model, one query builder and one cache path (Principle II). Defaults preserve today's behavior.
- **Alternatives considered**: a separate `HomeOptions` + query builder (rejected: duplicates the mask
  and query logic and needs its own cache-key handling); reusing `SearchOptions` (rejected: its key is
  `search|...` and it carries the text query, which would collide with the search cache).

## R2. Persist the extended filters in the existing store

- **Decision**: Extend `FilterPreferences.toFilters()`/`update()` with the new fields, using new
  preference keys and sane defaults; remove keys when a value is cleared.
- **Rationale**: DataStore already holds the home filters; no new dependency or Room migration.
- **Alternatives considered**: a separate preferences file (rejected: unnecessary split).

## R3. Persisted baseline + session override

- **Decision**: Add `HomePersonalizationRepository` with `persisted: Flow<WallhavenFilters>`, an
  in-memory `sessionOverride: MutableStateFlow<WallhavenFilters?>`, and `effective: Flow<WallhavenFilters>`
  = `combine(persisted, override) { p, o -> o?.mergeQuick(p) ?: p }`. `mergeQuick` replaces only the
  quick subset (sorting, categories, sketchy/nsfw); `sfw` and refinements always come from the
  persisted baseline. Quick setters mutate the override; personalization setters update the baseline
  and clear the corresponding override.
- **Rationale**: Implements the clarification exactly (quick filters are session-only and win; the
  personalization is the persisted baseline; a personalization edit realigns the quick value).
- **Alternatives considered**: persisting the quick filters (rejected: contradicts the clarification);
  a second ViewModel-scoped state (rejected: the gallery and the drawer live in different ViewModels,
  so the override must be shared).

## R4. Query key and cache

- **Decision**: Build `WallhavenFilters.key` from the existing base (`sorting|categories|purity`) plus
  the refinement segments only when they differ from the defaults, so the default key is unchanged and
  a personalized home gets its own cached pages.
- **Rationale**: Preserves the current home cache for users who do not personalize; distinct keys keep
  Paging coherent (Principle II).
- **Alternatives considered**: always append all refinements (rejected: invalidates the existing cache
  for default users); a single fixed key (rejected: personalized queries would clobber each other).

## R5. Screen and controls

- **Decision**: New `HomePersonalizationScreen` reachable from a drawer item, with the same control
  groups as the search filters: sorting (latest/top of the month/random), categories, purity, color,
  minimum resolution, exact resolutions, format/ratios, plus reset. Reuse existing string resources
  where the labels are identical; add new strings for the menu entry, screen title, description and
  reset.
- **Rationale**: FR-002/FR-011; consistency with the search UI without a risky refactor.
- **Alternatives considered**: reusing `SearchFilterSheet` directly (rejected: it is bound to
  `SearchOptions` and the text query); factoring all controls first (rejected: out-of-scope refactor).

## R6. Navigation and drawer entry

- **Decision**: Add `AppSection.HOME_PERSONALIZATION` + `HomePersonalizationRoute` and render the item
  in `DrawerContent` like the other sections; wire the route in `MainScreen` with the drawer menu
  button.
- **Rationale**: FR-001; matches the existing navigation pattern.
- **Alternatives considered**: a dialog or sheet from the drawer (rejected: the spec asks for a set of
  settings, best as a screen).

## R7. Settings "default sorting" reconciliation

- **Decision**: Route `SettingsViewModel.setSorting` through `HomePersonalizationRepository` so it
  updates the persisted baseline and clears the sorting session override; show the persisted value in
  Settings.
- **Rationale**: Avoids a second editor writing a value that a session override would silently hide.
- **Alternatives considered**: removing the Settings sorting section (rejected: out of scope and it
  would change existing behavior).

## R8. Existing guards

- **Decision**: Keep NSFW gated by the API key (the mediator already strips NSFW without a key) and
  prevent turning off the last category in both the drawer and the new screen.
- **Rationale**: FR-009/FR-010; preserves current behavior.
- **Alternatives considered**: allowing zero categories (rejected: wallhaven returns nothing).

## R9. Testing

- **Decision**: Extend `WallhavenFiltersTest` for the new fields, purity mask, key and query; add
  `HomePersonalizationRepositoryTest` (MockK for `FilterPreferences`) covering effective merge, session
  override precedence, baseline update clearing the override, and restart semantics (new repository
  instance loses the override).
- **Rationale**: Principle V and the AGENTS baseline; JVM-only.
- **Alternatives considered**: Compose UI tests (rejected: no device/Robolectric).

## R10. Offline behavior

- **Decision**: The home continues to use the Room-backed `RemoteMediator`/`PagingSource`; only the
  query key changes, so already-cached pages for the effective settings render offline.
- **Rationale**: FR-012 / Principle II.
- **Alternatives considered**: bypassing Room (rejected: violates Principle II).
