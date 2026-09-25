package dev.giovannidrago.wallhaven.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.util.formatPurity

@Composable
fun WallpaperGrid(
	items: LazyPagingItems<WallpaperEntity>,
	emptyText: String,
	onWallpaperClick: (String) -> Unit,
	onToggleFavorite: (WallpaperEntity, Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	val refreshState = items.loadState.refresh

	when {
		items.itemCount == 0 && refreshState is LoadState.Loading -> LoadingGrid(modifier)
		items.itemCount == 0 && refreshState is LoadState.Error -> ErrorState(error = refreshState.error, onRetry = { items.retry() }, modifier = modifier)
		items.itemCount == 0 -> EmptyState(text = emptyText, modifier = modifier)
		else -> LazyVerticalStaggeredGrid(
			columns = StaggeredGridCells.Adaptive(minSize = 132.dp),
			modifier = modifier.fillMaxSize(),
			contentPadding = PaddingValues(12.dp),
			verticalItemSpacing = 12.dp,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			items(
				count = items.itemCount,
				key = items.itemKey { it.id },
				contentType = items.itemContentType { "wallpaper" }
			) { index ->
				val wallpaper = items[index]
				if (wallpaper != null) {
					WallpaperCard(
						wallpaper = wallpaper,
						onClick = { onWallpaperClick(wallpaper.id) },
						onToggleFavorite = { onToggleFavorite(wallpaper, !wallpaper.favorited) }
					)
				} else {
					PlaceholderCard()
				}
			}

			if (items.loadState.append is LoadState.Loading) {
				item(span = StaggeredGridItemSpan.FullLine) {
					AppendLoading()
				}
			}
			if (items.loadState.append is LoadState.Error) {
				item(span = StaggeredGridItemSpan.FullLine) {
					AppendError(error = (items.loadState.append as LoadState.Error).error, onRetry = { items.retry() })
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WallpaperCard(
	wallpaper: WallpaperEntity,
	onClick: () -> Unit,
	onToggleFavorite: () -> Unit
) {
	Card(
		onClick = onClick,
		shape = RoundedCornerShape(14.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
		modifier = Modifier.fillMaxWidth()
	) {
		Box {
			AsyncImage(
				model = ImageRequest.Builder(LocalContext.current)
					.data(wallpaper.thumbLarge ?: wallpaper.thumbOriginal ?: wallpaper.path)
					.crossfade(true)
					.build(),
				contentDescription = stringResource(R.string.cd_thumbnail),
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxWidth()
					.aspectRatio(wallpaper.aspectRatio)
			)
			if (wallpaper.purity != "sfw") {
				PurityBadge(
					purity = wallpaper.purity,
					modifier = Modifier
						.align(Alignment.TopStart)
						.padding(6.dp)
				)
			}
			FavoriteButton(
				favorited = wallpaper.favorited,
				onClick = onToggleFavorite,
				modifier = Modifier
					.align(Alignment.TopEnd)
					.padding(4.dp)
			)
		}
	}
}

@Composable
private fun FavoriteButton(
	favorited: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	IconButton(
		onClick = onClick,
		modifier = modifier
			.size(34.dp)
			.background(Color.Black.copy(alpha = 0.35f), CircleShape)
	) {
		Icon(
			imageVector = if (favorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
			contentDescription = stringResource(R.string.cd_favorite),
			tint = if (favorited) MaterialTheme.colorScheme.primary else Color.White,
			modifier = Modifier.size(18.dp)
		)
	}
}

@Composable
private fun PurityBadge(purity: String, modifier: Modifier = Modifier) {
	Surface(
		shape = RoundedCornerShape(6.dp),
		color = when (purity) {
			"nsfw" -> MaterialTheme.colorScheme.error
			else -> MaterialTheme.colorScheme.tertiary
		},
		contentColor = Color.White,
		modifier = modifier
	) {
		Text(
			text = formatPurity(purity),
			style = MaterialTheme.typography.labelSmall,
			modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
		)
	}
}

@Composable
private fun LoadingGrid(modifier: Modifier = Modifier) {
	val ratios = remember { listOf(0.75f, 1.1f, 0.85f, 1.3f, 0.7f, 1.0f, 1.2f, 0.8f) }
	LazyVerticalStaggeredGrid(
		columns = StaggeredGridCells.Adaptive(minSize = 132.dp),
		modifier = modifier.fillMaxSize(),
		contentPadding = PaddingValues(12.dp),
		verticalItemSpacing = 12.dp,
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		items(ratios.size) { index ->
			PlaceholderCard(aspectRatio = ratios[index])
		}
	}
}

@Composable
private fun PlaceholderCard(aspectRatio: Float = 0.9f) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(aspectRatio)
			.clip(RoundedCornerShape(14.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant)
	)
}

@Composable
private fun AppendLoading() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp),
		contentAlignment = Alignment.Center
	) {
		CircularProgressIndicator()
	}
}
