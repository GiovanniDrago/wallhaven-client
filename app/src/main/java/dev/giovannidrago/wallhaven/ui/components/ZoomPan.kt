package dev.giovannidrago.wallhaven.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

const val MIN_SCALE = 1f
const val MAX_SCALE = 6f
const val FULLSCREEN_MAX_SCALE = 20f
const val DOUBLE_TAP_SCALE = 2.5f

/**
 * Clamps the zoom factor to `[MIN_SCALE, max]`; non-finite input falls back to the fit scale and an
 * invalid maximum falls back to [MAX_SCALE].
 */
fun clampScale(scale: Float, max: Float = MAX_SCALE): Float {
	if (!scale.isFinite()) {
		return MIN_SCALE
	}
	val safeMax = if (max.isFinite() && max >= MIN_SCALE) max else MAX_SCALE
	return scale.coerceIn(MIN_SCALE, safeMax)
}

/**
 * The fitted (unscaled) size of an image displayed inside [container] preserving its aspect ratio.
 * Returns [IntSize.Zero] for degenerate inputs so the bounds math can never divide by zero.
 */
fun fitContentSize(container: IntSize, imageWidth: Int, imageHeight: Int): IntSize {
	if (container.width <= 0 || container.height <= 0 || imageWidth <= 0 || imageHeight <= 0) {
		return IntSize.Zero
	}
	val fit = minOf(
		container.width.toFloat() / imageWidth,
		container.height.toFloat() / imageHeight
	)
	return IntSize(
		width = (imageWidth * fit).roundToInt(),
		height = (imageHeight * fit).roundToInt()
	)
}

/**
 * Maximum absolute translation per axis for [content] scaled by [scale] inside [container], so the
 * scaled content never reveals empty space beyond its own edges. An axis whose scaled content is not
 * larger than the container is locked to zero (centered).
 */
fun maxTranslation(container: IntSize, content: IntSize, scale: Float): Offset {
	if (container.width <= 0 || container.height <= 0 || !scale.isFinite()) {
		return Offset.Zero
	}
	val safeScale = scale.coerceAtLeast(0f)
	val scaledWidth = content.width * safeScale
	val scaledHeight = content.height * safeScale
	val maxX = ((scaledWidth - container.width) / 2f).coerceAtLeast(0f)
	val maxY = ((scaledHeight - container.height) / 2f).coerceAtLeast(0f)
	return Offset(maxX, maxY)
}

/** Clamps [offset] within [bounds]; non-finite values collapse to the centered position. */
fun clampOffset(offset: Offset, bounds: Offset): Offset {
	if (!offset.x.isFinite() || !offset.y.isFinite()) {
		return Offset.Zero
	}
	val maxX = if (bounds.x.isFinite()) bounds.x.coerceAtLeast(0f) else 0f
	val maxY = if (bounds.y.isFinite()) bounds.y.coerceAtLeast(0f) else 0f
	return Offset(
		x = offset.x.coerceIn(-maxX, maxX),
		y = offset.y.coerceIn(-maxY, maxY)
	)
}
