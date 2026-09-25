package dev.giovannidrago.wallhaven.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 6f
private const val DOUBLE_TAP_SCALE = 2.5f

@Composable
fun ZoomableImage(url: String, modifier: Modifier = Modifier) {
	var scale by remember { mutableFloatStateOf(MIN_SCALE) }
	var offset by remember { mutableStateOf(Offset.Zero) }

	val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
		scale = (scale * zoomChange).coerceIn(MIN_SCALE, MAX_SCALE)
		offset = if (scale > MIN_SCALE) {
			offset + panChange
		} else {
			Offset.Zero
		}
	}

	Box(
		modifier = modifier.clipToBounds(),
		contentAlignment = Alignment.Center
	) {
		SubcomposeAsyncImage(
			model = ImageRequest.Builder(LocalContext.current)
				.data(url)
				.crossfade(true)
				.build(),
			contentDescription = null,
			contentScale = ContentScale.Fit,
			loading = {
				CircularProgressIndicator()
			},
			error = {
				Text(
					text = "!",
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.error
				)
			},
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(min = 200.dp, max = 520.dp)
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
					translationX = offset.x
					translationY = offset.y
				}
				.transformable(transformableState)
				.pointerInput(Unit) {
					detectTapGestures(
						onDoubleTap = {
							if (scale > MIN_SCALE) {
								scale = MIN_SCALE
								offset = Offset.Zero
							} else {
								scale = DOUBLE_TAP_SCALE
							}
						}
					)
				}
		)
	}
}
