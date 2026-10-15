package dev.giovannidrago.wallhaven.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * A zoomable, pannable image used by every full-view image in the app.
 *
 * Inline, one finger scrolls the surrounding page while two fingers pinch to zoom and drag to pan.
 * When [singleFingerPan] is set (the full-screen viewer) a single-finger drag pans the image instead.
 * Zoom is clamped to `MIN_SCALE..maxScale` and pan is clamped so a visible portion of the image
 * always remains on screen, which keeps the view from getting stuck or losing the image.
 */
@Composable
fun ZoomableImage(
	url: String,
	modifier: Modifier = Modifier,
	maxScale: Float = MAX_SCALE,
	singleFingerPan: Boolean = false,
	fillParent: Boolean = false,
	onSingleTap: (() -> Unit)? = null
) {
	var scale by remember(url) { mutableFloatStateOf(MIN_SCALE) }
	var offset by remember(url) { mutableStateOf(Offset.Zero) }
	var containerSize by remember { mutableStateOf(IntSize.Zero) }
	var imageSize by remember { mutableStateOf(IntSize.Zero) }

	val contentSize = remember(containerSize, imageSize) {
		fitContentSize(containerSize, imageSize.width, imageSize.height)
	}

	// Re-clamp the pan whenever the viewport or the loaded image size changes (for example rotation).
	LaunchedEffect(containerSize, contentSize) {
		val bounds = maxTranslation(containerSize, contentSize, scale)
		offset = if (scale > MIN_SCALE) clampOffset(offset, bounds) else Offset.Zero
	}

	val currentScale by rememberUpdatedState(scale)
	val currentContainer by rememberUpdatedState(containerSize)
	val currentContent by rememberUpdatedState(contentSize)
	val currentOnSingleTap by rememberUpdatedState(onSingleTap)

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
			onSuccess = { state ->
				val drawable = state.result.drawable
				imageSize = IntSize(drawable.intrinsicWidth, drawable.intrinsicHeight)
			},
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
			modifier = (
				if (fillParent) {
					Modifier.fillMaxSize()
				} else {
					Modifier
						.fillMaxWidth()
						.heightIn(min = 200.dp, max = 520.dp)
				}
				)
				.onSizeChanged { containerSize = it }
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
					translationX = offset.x
					translationY = offset.y
				}
				.pointerInput(url, containerSize, contentSize, maxScale, singleFingerPan) {
					awaitEachGesture {
						// Inline, single-pointer drags are intentionally not consumed so the parent
						// scrollable page keeps scrolling; the full-screen viewer consumes them.
						awaitFirstDown(requireUnconsumed = false)
						var transforming = false
						do {
							val event = awaitPointerEvent()
							val pressedCount = event.changes.count { it.pressed }
							if (pressedCount >= 2 || singleFingerPan) {
								transforming = true
							}
							if (transforming) {
								val zoomChange = if (pressedCount >= 2) event.calculateZoom() else 1f
								val panChange = event.calculatePan()
								val newScale = clampScale(scale * zoomChange, maxScale)
								val bounds = maxTranslation(containerSize, contentSize, newScale)
								scale = newScale
								offset = if (newScale > MIN_SCALE) {
									clampOffset(offset + panChange, bounds)
								} else {
									Offset.Zero
								}
								event.changes.forEach { change ->
									if (change.positionChanged()) {
										change.consume()
									}
								}
							}
						} while (event.changes.any { it.pressed })
					}
				}
				.pointerInput(url) {
					detectTapGestures(
						onTap = { currentOnSingleTap?.invoke() },
						onDoubleTap = { tap ->
							if (currentScale > MIN_SCALE) {
								scale = MIN_SCALE
								offset = Offset.Zero
							} else {
								val target = clampScale(DOUBLE_TAP_SCALE, maxScale)
								val bounds = maxTranslation(currentContainer, currentContent, target)
								val centerX = currentContainer.width / 2f
								val centerY = currentContainer.height / 2f
								val desired = Offset(
									x = -(tap.x - centerX) * target,
									y = -(tap.y - centerY) * target
								)
								scale = target
								offset = clampOffset(desired, bounds)
							}
						}
					)
				}
		)
	}
}
