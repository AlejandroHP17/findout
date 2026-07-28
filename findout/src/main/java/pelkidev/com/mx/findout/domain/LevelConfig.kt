package pelkidev.com.mx.findout.domain

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Rect
import pelkidev.com.mx.findout.R

data class HiddenObjectDef(
    val id: String,
    val displayName: String,
    /** Rectángulo en píxeles de la textura (left, top, right, bottom). */
    val hotspot: Rect,
    val startsFound: Boolean = false,
)

enum class ZoomMode {
    /** Encaja el mapa completo (Nivel 1). */
    FIT_ENTIRE,
    /** Llena el lado corto; permite pan en el eje sobrante (Nivel 2+). */
    COVER_SHORT_SIDE,
    /** Escala fija (mapas muy grandes). */
    FIXED,
}

data class LevelConfig(
    val levelId: String,
    @param:DrawableRes val backgroundRes: Int,
    val imageWidth: Float,
    val imageHeight: Float,
    val objects: List<HiddenObjectDef>,
    val enablePan: Boolean = false,
    val zoomMode: ZoomMode = ZoomMode.FIT_ENTIRE,
    val fixedZoom: Float = 1f,
)

object LevelCatalog {
    val nivel1 = LevelConfig(
        levelId = "nivel_1",
        backgroundRes = R.drawable.findout_background_1,
        imageWidth = 1024f,
        imageHeight = 1536f,
        enablePan = false,
        zoomMode = ZoomMode.FIT_ENTIRE,
        objects = listOf(
            HiddenObjectDef(
                id = "objeto_a",
                displayName = "Objeto A",
                hotspot = Rect(180f, 420f, 300f, 520f),
            ),
            HiddenObjectDef(
                id = "objeto_b",
                displayName = "Objeto B",
                hotspot = Rect(620f, 780f, 730f, 870f),
            ),
            HiddenObjectDef(
                id = "objeto_c",
                displayName = "Objeto C",
                hotspot = Rect(400f, 1100f, 540f, 1210f),
            ),
        ),
    )

    val nivel2 = LevelConfig(
        levelId = "nivel_2",
        backgroundRes = R.drawable.findout_background_2,
        imageWidth = 1024f,
        imageHeight = 1024f,
        enablePan = true,
        zoomMode = ZoomMode.COVER_SHORT_SIDE,
        objects = listOf(
            HiddenObjectDef(
                id = "faro",
                displayName = "Faro",
                hotspot = Rect(80f, 120f, 190f, 250f),
            ),
            HiddenObjectDef(
                id = "ancla",
                displayName = "Ancla",
                hotspot = Rect(720f, 480f, 840f, 580f),
            ),
            HiddenObjectDef(
                id = "brujula",
                displayName = "Brújula",
                hotspot = Rect(430f, 820f, 560f, 930f),
            ),
        ),
    )

    val all: List<LevelConfig> = listOf(nivel1, nivel2)

    fun nextAfter(levelId: String): LevelConfig? {
        val index = all.indexOfFirst { it.levelId == levelId }
        if (index < 0 || index >= all.lastIndex) return null
        return all[index + 1]
    }
}
