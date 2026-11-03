# Feature Specification: Home Personalization

**Feature Branch**: `005-home-personalization`

**Created**: 2026-10-15

**Status**: Draft

**Input**: User description: "In the menu on the left, add a new menu item \"Home personalization\" that will display a set of settings similar to filters, but they will be used for the home instead of the simple current options ultimi, ultimo mese, casuale. By doing that the home will be filtered with that each time"

## Clarifications

### Session 2026-10-15

- Q: Which settings should the Home personalization screen include? → A: The full search filter set: sorting, categories, purity, color, minimum/exact resolution and format.
- Q: How should the drawer quick filters and the Home personalization screen coexist? → A: They start aligned to the personalization; a quick-filter change applies only to the current session and overrides the personalization, while the personalization remains the persisted configuration.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Open and edit the Home personalization settings (Priority: P1)

The side menu gains a "Home personalization" entry that opens a dedicated screen. There the user finds a
set of options similar to the search filters: how the home is sorted (latest, top of the month,
random), which categories are included (general, anime, people), the allowed purity (SFW, sketchy,
NSFW), and optional refinements such as color, minimum/exact resolution and format. The screen also
offers a reset to the default values.

**Why this priority**: This is the new surface the user asked for; without it there is nothing to
configure.

**Independent Test**: Open the drawer, open "Home personalization", change some values, and confirm
they are kept while the screen is open and after leaving and reopening it.

**Acceptance Scenarios**:

1. **Given** the drawer is open, **When** it is displayed, **Then** a "Home personalization" entry is
   present among the existing entries.
2. **Given** "Home personalization" is open, **When** the user views it, **Then** it shows sorting,
   categories, purity and the optional refinements (color, resolution, format) in a layout consistent
   with the search filters.
3. **Given** the user changes any option, **When** they leave and reopen the screen, **Then** the
   chosen values are still selected.
4. **Given** the user taps "Reset", **When** the action completes, **Then** every option returns to its
   default value (the current behaviour: latest, all categories, SFW, no refinements).

---

### User Story 2 - The home always uses the personalization (Priority: P1)

Whatever the user configures in Home personalization is applied to the home feed every time: on app
start, when returning to the home, on refresh, and after the settings change. The home is no longer
limited to the simple "latest / last month / random" choice.

**Why this priority**: The settings only matter if the home actually respects them.

**Independent Test**: Set a personalization (for example top of the month, anime only, sketchy
allowed), return to the home and refresh; confirm the feed matches those settings.

**Acceptance Scenarios**:

1. **Given** a saved personalization, **When** the user opens the home, **Then** the feed is requested
   with those settings.
2. **Given** the user changes the personalization, **When** they return to the home, **Then** the feed
   reflects the new settings without requiring a restart.
3. **Given** the home already showed results, **When** the user pulls to refresh, **Then** the refresh
   uses the current personalization.
4. **Given** the default personalization, **When** the home opens, **Then** it behaves exactly as it
   does today (latest, all categories, SFW, no extra constraints).

---

### User Story 3 - Quick drawer filters are session overrides (Priority: P2)

The drawer quick filters (collapsible "Filters" group) start aligned with the saved Home
personalization. If the user changes one, that change applies only for the current session and
overrides the personalization for the home; on a new session the quick filters return to the saved
personalization.

**Why this priority**: Lets the user make a quick, temporary adjustment without altering the saved
configuration, while keeping both surfaces understandable.

**Independent Test**: With a saved personalization, change a quick filter in the drawer and confirm
the home follows it; restart the app and confirm the quick filters and the home return to the saved
personalization.

**Acceptance Scenarios**:

1. **Given** a saved personalization, **When** the drawer is opened, **Then** the quick filters show
   the same values as the personalization.
2. **Given** the drawer quick filters, **When** the user changes a value, **Then** the home uses that
   value for the rest of the current session, overriding the personalization.
3. **Given** a quick-filter override, **When** the app is restarted, **Then** the quick filters and the
   home return to the saved personalization.
4. **Given** the Home personalization screen, **When** the user changes a value, **Then** the saved
   baseline is updated and the corresponding quick filter realigns to it.

---

### Edge Cases

- What happens when the user selects NSFW without a saved API key? (The option must remain unavailable
  or be rejected, as it is today.)
- What happens when every category is turned off? (At least one category must remain enabled.)
- What happens when a resolution or format refinement is selected that matches nothing? (The home shows
  the normal empty state.)
- What happens when the settings change while the home is loading?
- What happens when the app is offline? (The home must still show the cached results for the last
  settings.)
- What happens when the user resets the personalization while the home is showing results?
- What happens when the user changes a quick filter and then edits the same value in the Home
  personalization screen? The personalization edit updates the saved baseline and realigns that quick
  filter.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The side menu MUST include a "Home personalization" entry that opens a dedicated screen.
- **FR-002**: The screen MUST expose the full search filter set adapted for the home: sorting (latest,
  top of the month, random), categories (general, anime, people), purity (SFW, sketchy, NSFW), color,
  minimum and exact resolution, and format (orientation/exact ratios).
- **FR-003**: The home settings MUST be persisted locally and survive app restarts.
- **FR-004**: The home feed MUST be requested with the saved home settings every time it loads,
  including app start, returning to the home, and pull-to-refresh.
- **FR-005**: Changing the home settings MUST be reflected in the home feed without an app restart.
- **FR-006**: The default home settings MUST reproduce today's behaviour (latest, all categories,
  SFW, no refinements).
- **FR-007**: The drawer's quick filters MUST start aligned with the persisted Home personalization.
- **FR-008**: The screen MUST provide a reset action that restores the default values.
- **FR-009**: NSFW MUST remain gated by the saved API key, exactly as it is today.
- **FR-010**: At least one category MUST remain enabled; turning the last one off MUST be prevented.
- **FR-011**: All new user-visible text MUST be provided in both English and Italian.
- **FR-012**: The home MUST remain usable offline, showing the locally cached results for the current
  settings.
- **FR-013**: A change made through the drawer quick filters MUST apply only to the current session
  and MUST override the corresponding personalization value for the home.
- **FR-014**: Quick-filter overrides MUST NOT be persisted; a new session MUST start from the saved
  personalization.
- **FR-015**: A change made in the Home personalization screen MUST update the persisted baseline and
  realign the corresponding quick-filter value.

### Key Entities *(include if feature involves data)*

- **Home personalization**: The persisted set of home settings: sorting, enabled categories, allowed
  purity, and the optional color/resolution/format refinements.
- **Home feed**: The gallery shown on the home, requested with the home personalization each time.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of home loads and refreshes use the saved personalization.
- **SC-002**: A user can open Home personalization from the drawer and change a setting in under 30
  seconds.
- **SC-003**: The drawer quick filters start aligned with the saved personalization, and a quick-filter
  change is reflected in the home for the current session with 0 conflicting values.
- **SC-007**: A quick-filter change never modifies the saved personalization and is discarded when the
  app restarts.
- **SC-008**: After a restart, the home uses the saved personalization again.
- **SC-004**: The default personalization produces exactly the same home feed as the current app.
- **SC-005**: The home remains available offline with the cached results for the current settings.
- **SC-006**: 100% of the new user-visible strings are available in both English and Italian.

## Assumptions

- "Settings similar to filters" means the search filter set (categories, purity, sorting, color,
  resolution and format), adapted for a home feed with no text query; relevance does not apply.
- The home is the existing gallery screen; the new settings apply to it only, not to search or
  favorites.
- The drawer quick filters are a session-only override aligned to the persisted personalization; they
  are not saved and, once changed, take precedence over the personalization until the app restarts.
- The current behaviours are preserved: NSFW still requires an API key, at least one category stays
  enabled, and the default configuration matches today's gallery.
