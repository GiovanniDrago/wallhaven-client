package dev.giovannidrago.wallhaven

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import dev.giovannidrago.wallhaven.ui.components.FULLSCREEN_MAX_SCALE
import dev.giovannidrago.wallhaven.ui.components.MAX_SCALE
import dev.giovannidrago.wallhaven.ui.components.MIN_SCALE
import dev.giovannidrago.wallhaven.ui.components.clampOffset
import dev.giovannidrago.wallhaven.ui.components.clampScale
import dev.giovannidrago.wallhaven.ui.components.fitContentSize
import dev.giovannidrago.wallhaven.ui.components.maxTranslation
import dev.giovannidrago.wallhaven.ui.components.transform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoomPanTest {

	@Test
	fun `clamp scale keeps the value within the supported range`() {
		assertEquals(MIN_SCALE, clampScale(0.25f), 0.0001f)
		assertEquals(2f, clampScale(2f), 0.0001f)
		assertEquals(MAX_SCALE, clampScale(100f), 0.0001f)
	}

	@Test
	fun `clamp scale falls back to fit for non finite input`() {
		assertEquals(MIN_SCALE, clampScale(Float.NaN), 0.0001f)
		assertEquals(MIN_SCALE, clampScale(Float.POSITIVE_INFINITY), 0.0001f)
	}

	@Test
	fun `clamp scale honors a custom higher cap`() {
		assertEquals(15f, clampScale(15f, FULLSCREEN_MAX_SCALE), 0.0001f)
		assertEquals(FULLSCREEN_MAX_SCALE, clampScale(100f, FULLSCREEN_MAX_SCALE), 0.0001f)
		assertEquals(MAX_SCALE, clampScale(100f), 0.0001f)
	}

	@Test
	fun `fit content size preserves the aspect ratio`() {
		val fitted = fitContentSize(IntSize(100, 100), imageWidth = 200, imageHeight = 100)

		assertEquals(100, fitted.width)
		assertEquals(50, fitted.height)
	}

	@Test
	fun `fit content size is zero for degenerate inputs`() {
		assertEquals(IntSize.Zero, fitContentSize(IntSize.Zero, 200, 100))
		assertEquals(IntSize.Zero, fitContentSize(IntSize(100, 100), 0, 100))
	}

	@Test
	fun `max translation is zero when the scaled content fits the viewport`() {
		val bounds = maxTranslation(IntSize(100, 100), IntSize(100, 100), scale = 1f)

		assertEquals(Offset.Zero, bounds)
	}

	@Test
	fun `max translation grows with the scale`() {
		val bounds = maxTranslation(IntSize(100, 100), IntSize(100, 100), scale = 2f)

		assertEquals(50f, bounds.x, 0.0001f)
		assertEquals(50f, bounds.y, 0.0001f)
	}

	@Test
	fun `max translation locks an axis that is not larger than the viewport`() {
		val bounds = maxTranslation(IntSize(100, 100), IntSize(100, 50), scale = 2f)

		assertEquals(50f, bounds.x, 0.0001f)
		assertEquals(0f, bounds.y, 0.0001f)
	}

	@Test
	fun `clamp offset keeps the offset within the bounds`() {
		val bounds = Offset(50f, 50f)

		assertEquals(Offset(50f, 50f), clampOffset(Offset(1000f, 1000f), bounds))
		assertEquals(Offset(-50f, -50f), clampOffset(Offset(-1000f, -1000f), bounds))
		assertEquals(Offset(10f, -10f), clampOffset(Offset(10f, -10f), bounds))
	}

	@Test
	fun `fit view is centered with no residual pan`() {
		assertEquals(Offset.Zero, clampOffset(Offset(500f, 500f), Offset.Zero))
	}

	@Test
	fun `clamp offset collapses non finite input to the center`() {
		assertEquals(Offset.Zero, clampOffset(Offset(Float.NaN, Float.NaN), Offset(50f, 50f)))
	}

	@Test
	fun `clamp offset never returns a non finite coordinate`() {
		val clamped = clampOffset(Offset(Float.POSITIVE_INFINITY, 0f), Offset(50f, 50f))

		assertTrue(clamped.x.isFinite())
		assertTrue(clamped.y.isFinite())
	}

	@Test
	fun `zoom keeps the point under the centroid fixed`() {
		val result = transform(
			scale = 1f,
			offset = Offset.Zero,
			centroid = Offset(75f, 50f),
			container = IntSize(100, 100),
			content = IntSize(100, 100),
			zoomChange = 2f,
			pan = Offset.Zero,
			maxScale = MAX_SCALE
		)

		assertEquals(2f, result.scale, 0.0001f)
		// focal = centroid - centre = (25, 0); offset += focal * (1 - 2) = (-25, 0).
		assertEquals(-25f, result.offset.x, 0.0001f)
		assertEquals(0f, result.offset.y, 0.0001f)
	}

	@Test
	fun `pan moves the offset by pan times scale`() {
		val result = transform(
			scale = 2f,
			offset = Offset.Zero,
			centroid = Offset(50f, 50f),
			container = IntSize(100, 100),
			content = IntSize(100, 100),
			zoomChange = 1f,
			pan = Offset(10f, 0f),
			maxScale = MAX_SCALE
		)

		assertEquals(2f, result.scale, 0.0001f)
		assertEquals(20f, result.offset.x, 0.0001f)
	}

	@Test
	fun `pan is clamped to the bounds`() {
		val result = transform(
			scale = 2f,
			offset = Offset.Zero,
			centroid = Offset(50f, 50f),
			container = IntSize(100, 100),
			content = IntSize(100, 100),
			zoomChange = 1f,
			pan = Offset(1000f, 0f),
			maxScale = MAX_SCALE
		)

		// maxTranslation at scale 2 with 100/100 content inside a 100/100 container is 50.
		assertEquals(50f, result.offset.x, 0.0001f)
	}

	@Test
	fun `transform centers the view at the minimum scale`() {
		val result = transform(
			scale = 1f,
			offset = Offset(30f, 30f),
			centroid = Offset(10f, 10f),
			container = IntSize(100, 100),
			content = IntSize(100, 100),
			zoomChange = 0.5f,
			pan = Offset.Zero,
			maxScale = MAX_SCALE
		)

		assertEquals(MIN_SCALE, result.scale, 0.0001f)
		assertEquals(Offset.Zero, result.offset)
	}
}
