package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.data.prefs.FilterPreferences
import dev.giovannidrago.wallhaven.data.repository.HomePersonalizationRepository
import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HomePersonalizationRepositoryTest {
	private val baseline = MutableStateFlow(WallhavenFilters())
	private val preferences = mockk<FilterPreferences>(relaxed = true).apply {
		every { filters } returns baseline
	}

	@Test
	fun `effective starts from the persisted baseline`() = runTest {
		val repository = HomePersonalizationRepository(preferences, backgroundScope)
		runCurrent()

		assertEquals(Sorting.LATEST, repository.effective.value.sorting)
		assertEquals(true, repository.effective.value.general)
	}

	@Test
	fun `a quick filter overrides only the session`() = runTest {
		val repository = HomePersonalizationRepository(preferences, backgroundScope)
		runCurrent()

		repository.setQuickSorting(Sorting.RANDOM)
		repository.setQuickCategory(CategoryFilter.ANIME, false)
		runCurrent()

		assertEquals(Sorting.RANDOM, repository.effective.value.sorting)
		assertEquals(false, repository.effective.value.anime)
		assertEquals(Sorting.LATEST, repository.persisted.value.sorting)
		assertEquals(true, repository.persisted.value.anime)
	}

	@Test
	fun `persisted edit clears the session override`() = runTest {
		val repository = HomePersonalizationRepository(preferences, backgroundScope)
		runCurrent()
		repository.setQuickSorting(Sorting.RANDOM)
		runCurrent()

		repository.updatePersisted { it.copy(sorting = Sorting.TOPLIST_MONTH) }
		baseline.value = WallhavenFilters(sorting = Sorting.TOPLIST_MONTH)
		runCurrent()

		assertEquals(Sorting.TOPLIST_MONTH, repository.effective.value.sorting)
		coVerify { preferences.update(any()) }
	}

	@Test
	fun `reset clears the override and restores the defaults`() = runTest {
		val repository = HomePersonalizationRepository(preferences, backgroundScope)
		runCurrent()
		repository.setQuickSketchy(true)
		runCurrent()

		repository.reset()
		runCurrent()

		assertEquals(false, repository.effective.value.sketchy)
		assertEquals(true, repository.effective.value.sfw)
	}

	@Test
	fun `a new session starts without overrides`() = runTest {
		val repository = HomePersonalizationRepository(preferences, backgroundScope)
		runCurrent()
		repository.setQuickSketchy(true)
		runCurrent()

		val newSession = HomePersonalizationRepository(preferences, backgroundScope)
		runCurrent()

		assertEquals(false, newSession.effective.value.sketchy)
	}
}
