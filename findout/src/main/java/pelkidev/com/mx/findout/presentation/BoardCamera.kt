package pelkidev.com.mx.findout.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import pelkidev.com.mx.findout.domain.LevelConfig

/**
 * Cámara del tablero: zoom de usuario + pan en píxeles de pantalla.
 * Se actualiza de forma síncrona dentro del gesto (sin esperar recomposición).
 */
internal class BoardCamera {
    var userZoom by mutableFloatStateOf(1f)
        private set
    var pan by mutableStateOf(Offset.Zero)
        private set

    fun reset(level: LevelConfig, baseScale: Float, boxW: Float, boxH: Float) {
        userZoom = 1f
        val drawnW = level.imageWidth * baseScale
        val drawnH = level.imageHeight * baseScale
        pan = clampPanOffset(
            Offset((boxW - drawnW) / 2f, (boxH - drawnH) / 2f),
            drawnW,
            drawnH,
            boxW,
            boxH,
        )
    }

    fun reclamp(level: LevelConfig, baseScale: Float, boxW: Float, boxH: Float) {
        val scale = baseScale * userZoom
        pan = clampPanOffset(
            pan,
            level.imageWidth * scale,
            level.imageHeight * scale,
            boxW,
            boxH,
        )
    }

    fun applyGesture(
        centroid: Offset,
        panDelta: Offset,
        zoomFactor: Float,
        level: LevelConfig,
        baseScale: Float,
        boxW: Float,
        boxH: Float,
    ) {
        var nextZoom = userZoom
        var nextPan = pan
        val oldScale = baseScale * nextZoom

        if (level.enablePinchZoom && zoomFactor != 1f) {
            nextZoom = clampUserZoom(
                nextZoom * zoomFactor,
                level.minUserZoom,
                level.maxUserZoom,
            )
            val newScale = baseScale * nextZoom
            nextPan = panAfterZoomAroundCentroid(
                pan = nextPan,
                oldScale = oldScale,
                newScale = newScale,
                centroid = centroid,
                imageWidth = level.imageWidth,
                imageHeight = level.imageHeight,
                boxW = boxW,
                boxH = boxH,
            )
        }

        val nextDrawnW = level.imageWidth * baseScale * nextZoom
        val nextDrawnH = level.imageHeight * baseScale * nextZoom
        val allowPan = canPanBoard(
            level.enablePan,
            nextZoom,
            nextDrawnW,
            nextDrawnH,
            boxW,
            boxH,
        )
        nextPan = if (allowPan && panDelta != Offset.Zero) {
            clampPanOffset(nextPan + panDelta, nextDrawnW, nextDrawnH, boxW, boxH)
        } else {
            clampPanOffset(nextPan, nextDrawnW, nextDrawnH, boxW, boxH)
        }

        userZoom = nextZoom
        pan = nextPan
    }

    fun screenToImage(screen: Offset, baseScale: Float): Offset {
        val scale = baseScale * userZoom
        if (scale <= 0f) return Offset.Unspecified
        return Offset(
            (screen.x - pan.x) / scale,
            (screen.y - pan.y) / scale,
        )
    }
}
