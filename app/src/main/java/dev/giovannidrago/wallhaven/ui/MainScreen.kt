package dev.giovannidrago.wallhaven.ui

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.giovannidrago.wallhaven.BuildConfig
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.ui.components.DrawerContent
import dev.giovannidrago.wallhaven.ui.components.openUrl
import dev.giovannidrago.wallhaven.ui.detail.DetailScreen
import dev.giovannidrago.wallhaven.ui.favorites.FavoritesScreen
import dev.giovannidrago.wallhaven.ui.gallery.GalleryScreen
import dev.giovannidrago.wallhaven.ui.navigation.AppSection
import dev.giovannidrago.wallhaven.ui.navigation.DetailRoute
import dev.giovannidrago.wallhaven.ui.navigation.FavoritesRoute
import dev.giovannidrago.wallhaven.ui.navigation.GalleryRoute
import dev.giovannidrago.wallhaven.ui.navigation.SearchRoute
import dev.giovannidrago.wallhaven.ui.navigation.SettingsRoute
import dev.giovannidrago.wallhaven.ui.search.SearchScreen
import dev.giovannidrago.wallhaven.ui.settings.SettingsScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box as ComposeBox

@Composable
fun MainScreen() {
	val navController = rememberNavController()
	val drawerState = rememberDrawerState(DrawerValue.Closed)
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val viewModel: MainViewModel = hiltViewModel()
	val filters by viewModel.filters.collectAsStateWithLifecycle()
	val apiKeyPresent by viewModel.apiKeyPresent.collectAsStateWithLifecycle()

	val currentDestination = navController.currentBackStackEntryAsState().value?.destination
	val selectedSection = when {
		currentDestination?.hasRoute<SearchRoute>() == true -> AppSection.SEARCH
		currentDestination?.hasRoute<FavoritesRoute>() == true -> AppSection.FAVORITES
		currentDestination?.hasRoute<SettingsRoute>() == true -> AppSection.SETTINGS
		else -> AppSection.GALLERY
	}

	fun navigateTo(section: AppSection) {
		scope.launch {
			drawerState.close()
		}
		val route: Any = when (section) {
			AppSection.GALLERY -> GalleryRoute
			AppSection.SEARCH -> SearchRoute()
			AppSection.FAVORITES -> FavoritesRoute
			AppSection.SETTINGS -> SettingsRoute
		}
		navController.navigate(route) {
			popUpTo<GalleryRoute> {
				saveState = true
			}
			launchSingleTop = true
			restoreState = true
		}
	}

	ModalNavigationDrawer(
		drawerState = drawerState,
		drawerContent = {
			ModalDrawerSheet(modifier = Modifier.fillMaxSize()) {
				DrawerContent(
					filters = filters,
					apiKeyPresent = apiKeyPresent,
					selectedSection = selectedSection,
					onNavigate = ::navigateTo,
					onDonate = {
						scope.launch { drawerState.close() }
						if (!openUrl(context, BuildConfig.DONATION_URL)) {
							Toast.makeText(context, R.string.error_open_link, Toast.LENGTH_SHORT).show()
						}
					},
					onNsfwChange = viewModel::setNsfw,
					onSketchyChange = viewModel::setSketchy,
					onCategoryChange = viewModel::setCategory
				)
			}
		}
	) {
		ComposeBox(modifier = Modifier.fillMaxSize()) {
			NavHost(
				navController = navController,
				startDestination = GalleryRoute
			) {
				composable<GalleryRoute> {
					GalleryScreen(
						onOpenDrawer = { scope.launch { drawerState.open() } },
						onWallpaperClick = { id -> navController.navigate(DetailRoute(id)) }
					)
				}
				composable<FavoritesRoute> {
					FavoritesScreen(
						onOpenDrawer = { scope.launch { drawerState.open() } },
						onWallpaperClick = { id -> navController.navigate(DetailRoute(id)) }
					)
				}
				composable<SearchRoute> {
					SearchScreen(
						onOpenDrawer = { scope.launch { drawerState.open() } },
						onWallpaperClick = { id -> navController.navigate(DetailRoute(id)) }
					)
				}
				composable<SettingsRoute> {
					SettingsScreen(onOpenDrawer = { scope.launch { drawerState.open() } })
				}
				composable<DetailRoute> {
					DetailScreen(
						onBack = { navController.popBackStack() },
						onTagClick = { tag -> navController.navigate(SearchRoute(initialQuery = tag)) }
					)
				}
			}
		}
	}
}
