package pelkidev.com.mx.findout.presentation

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTransformTest {
    @Test
    fun clampUserZoomRespectsLimits() {
        assertEquals(1f, clampUserZoom(0.5f, 1f, 3.5f), 0.001f)
        assertEquals(3.5f, clampUserZoom(9f, 1f, 3.5f), 0.001f)
        assertEquals(2f, clampUserZoom(2f, 1f, 3.5f), 0.001f)
    }

    @Test
    fun zoomAroundCentroidKeepsImagePointStable() {
        val oldScale = 1f
        val newScale = 2f
        val pan = Offset(100f, 50f)
        val centroid = Offset(300f, 200f)
        val newPan = panAfterZoomAroundCentroid(
            pan = pan,
            oldScale = oldScale,
            newScale = newScale,
            centroid = centroid,
            imageWidth = 800f,
            imageHeight = 600f,
            boxW = 400f,
            boxH = 300f,
        )
        val imageX = (centroid.x - pan.x) / oldScale
        val imageY = (centroid.y - pan.y) / oldScale
        val imageXAfter = (centroid.x - newPan.x) / newScale
        val imageYAfter = (centroid.y - newPan.y) / newScale
        assertEquals(imageX, imageXAfter, 0.5f)
        assertEquals(imageY, imageYAfter, 0.5f)
    }

    @Test
    fun canPanWhenZoomedEvenIfLevelDisablesPan() {
        assertFalse(canPanBoard(false, 1f, 100f, 100f, 200f, 200f))
        assertTrue(canPanBoard(false, 1.5f, 150f, 150f, 200f, 200f))
        assertTrue(canPanBoard(true, 1f, 100f, 100f, 200f, 200f))
    }
}
