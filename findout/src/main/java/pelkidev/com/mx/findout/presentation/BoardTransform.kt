package pelkidev.com.mx.findout.presentation

import androidx.compose.ui.geometry.Offset
import pelkidev.com.mx.findout.domain.LevelConfig
import pelkidev.com.mx.findout.domain.ZoomMode
import kotlin.math.max
import kotlin.math.min

internal fun computeBaseScale(level: LevelConfig, boxW: Float, boxH: Float): Float {
    return when (level.zoomMode) {
        ZoomMode.FIT_ENTIRE -> min(boxW / level.imageWidth, boxH / level.imageHeight)
        ZoomMode.COVER_SHORT_SIDE -> max(boxW / level.imageWidth, boxH / level.imageHeight)
        ZoomMode.FIXED -> level.fixedZoom
    }
}

internal fun clampUserZoom(userZoom: Float, minZoom: Float, maxZoom: Float): Float =
    userZoom.coerceIn(minZoom, maxZoom)

internal fun clampPanOffset(
    offset: Offset,
    drawnW: Float,
    drawnH: Float,
    boxW: Float,
    boxH: Float,
): Offset {
    val x = if (drawnW <= boxW) {
        (boxW - drawnW) / 2f
    } else {
        offset.x.coerceIn(boxW - drawnW, 0f)
    }
    val y = if (drawnH <= boxH) {
        (boxH - drawnH) / 2f
    } else {
        offset.y.coerceIn(boxH - drawnH, 0f)
    }
    return Offset(x, y)
}

/**
 * Ajusta el pan para que el punto de imagen bajo [centroid] se mantenga fijo
 * al cambiar de [oldScale] a [newScale].
 */
internal fun panAfterZoomAroundCentroid(
    pan: Offset,
    oldScale: Float,
    newScale: Float,
    centroid: Offset,
    imageWidth: Float,
    imageHeight: Float,
    boxW: Float,
    boxH: Float,
): Offset {
    if (oldScale <= 0f || newScale <= 0f) return pan
    val imageX = (centroid.x - pan.x) / oldScale
    val imageY = (centroid.y - pan.y) / oldScale
    val newPan = Offset(
        centroid.x - imageX * newScale,
        centroid.y - imageY * newScale,
    )
    return clampPanOffset(
        newPan,
        imageWidth * newScale,
        imageHeight * newScale,
        boxW,
        boxH,
    )
}

internal fun canPanBoard(
    enablePan: Boolean,
    userZoom: Float,
    drawnW: Float,
    drawnH: Float,
    boxW: Float,
    boxH: Float,
): Boolean {
    if (enablePan) return true
    if (userZoom > 1.01f) return true
    return drawnW > boxW + 0.5f || drawnH > boxH + 0.5f
}
