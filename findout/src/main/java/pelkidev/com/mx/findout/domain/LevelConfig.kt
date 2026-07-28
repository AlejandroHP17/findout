package pelkidev.com.mx.findout.domain

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Rect
import pelkidev.com.mx.findout.R

data class HiddenObjectDef(
    val id: String,
    val displayName: String,
    /** Rectángulo en píxeles de la textura (origen arriba-izquierda). */
    val hotspot: Rect,
    val startsFound: Boolean = false,
)

data class LevelConfig(
    val levelId: String,
    @param:DrawableRes val backgroundRes: Int,
    val imageWidth: Float,
    val imageHeight: Float,
    val objects: List<HiddenObjectDef>,
)

object LevelCatalog {
    val nivel1 = LevelConfig(
        levelId = "nivel_1",
        backgroundRes = R.drawable.findout_background_1,
        imageWidth = 1024f,
        imageHeight = 1536f,
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

    val all: List<LevelConfig> = listOf(nivel1)

    fun nextAfter(levelId: String): LevelConfig? {
        val index = all.indexOfFirst { it.levelId == levelId }
        if (index < 0 || index >= all.lastIndex) return null
        return all[index + 1]
    }
}
