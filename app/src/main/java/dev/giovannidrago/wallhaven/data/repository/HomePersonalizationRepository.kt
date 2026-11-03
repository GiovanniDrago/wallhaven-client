package dev.giovannidrago.wallhaven.data.repository

import dev.giovannidrago.wallhaven.data.prefs.FilterPreferences
import dev.giovannidrago.wallhaven.di.ApplicationScope
import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Combines the persisted home personalization with a session-only override coming from the drawer's
 * quick filters.
 *
 * The drawer quick filters start aligned with the baseline; once changed they apply only to the
 * current session and win over the baseline. Saving a value from the Home personalization screen
 * clears the overrides so the saved baseline becomes authoritative again. The override is kept in
 * memory, so a new app session starts from the persisted configuration.
 */
@Singleton
class HomePersonalizationRepository @Inject constructor(
	private val filterPreferences: FilterPreferences,
	@ApplicationScope scope: CoroutineScope
) {
	private val sessionOverride = MutableStateFlow<WallhavenFilters?>(null)

	val persisted: StateFlow<WallhavenFilters> = filterPreferences.filters
		.stateIn(scope, SharingStarted.Eagerly, WallhavenFilters())

	val effective: StateFlow<WallhavenFilters> =
		combine(persisted, sessionOverride) { baseline, override ->
			override?.let { baseline.withQuickValuesFrom(it) } ?: baseline
		}.stateIn(scope, SharingStarted.Eagerly, WallhavenFilters())

	fun setQuickSorting(sorting: Sorting) = updateSession { it.copy(sorting = sorting) }

	fun setQuickSketchy(enabled: Boolean) = updateSession { it.copy(sketchy = enabled) }

	fun setQuickNsfw(enabled: Boolean) = updateSession { it.copy(nsfw = enabled) }

	fun setQuickCategory(category: CategoryFilter, enabled: Boolean) = updateSession { current ->
		val updated = current.withCategory(category, enabled)
		if (updated.categoryCount == 0) current else updated
	}

	suspend fun updatePersisted(transform: (WallhavenFilters) -> WallhavenFilters) {
		filterPreferences.update(transform)
		// The saved baseline is authoritative again: drop the overrides and realign the quick filters.
		sessionOverride.value = null
	}

	suspend fun reset() {
		sessionOverride.value = null
		filterPreferences.update { WallhavenFilters() }
	}

	private fun updateSession(transform: (WallhavenFilters) -> WallhavenFilters) {
		val base = sessionOverride.value ?: effective.value
		sessionOverride.value = transform(base)
	}
}

private fun WallhavenFilters.withQuickValuesFrom(other: WallhavenFilters): WallhavenFilters = copy(
	sorting = other.sorting,
	general = other.general,
	anime = other.anime,
	people = other.people,
	sketchy = other.sketchy,
	nsfw = other.nsfw
)
