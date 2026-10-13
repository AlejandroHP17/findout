package pelkidev.com.mx.findout.domain

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Rect
import pelkidev.com.mx.findout.R

data class HiddenObjectDef(
    val id: String,
    val displayName: String,
    /** Rectángulo en píxeles de la textura (left, top, right, bottom). */
    val hotspot: Rect,
    @param:DrawableRes val iconRes: Int,
    val startsFound: Boolean = false,
)

enum class ZoomMode {
    /** Encaja el mapa completo. */
    FIT_ENTIRE,
    /** Llena el lado corto; permite pan en el eje sobrante (mapas grandes). */
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
    /** Pinch-zoom del jugador sobre la escala base (FIT/COVER/FIXED). */
    val enablePinchZoom: Boolean = true,
    val minUserZoom: Float = 1f,
    val maxUserZoom: Float = 3.5f,
)

/** Hotspot cuadrado centrado; sizeFraction ≈ fracción del ancho del mapa. */
private fun overlayRect(
    centerX: Float,
    centerY: Float,
    imageWidth: Float,
    sizeFraction: Float = 0.08f,
): Rect {
    val half = imageWidth * sizeFraction * 0.5f
    return Rect(centerX - half, centerY - half, centerX + half, centerY + half)
}

object LevelCatalog {
    private const val W = 1024f
    private const val H = 1536f

    val nivel1 = LevelConfig(
        levelId = "nivel_1",
        backgroundRes = R.drawable.findout_background_1,
        imageWidth = W,
        imageHeight = H,
        enablePan = false,
        zoomMode = ZoomMode.FIT_ENTIRE,
        objects = listOf(
            HiddenObjectDef(
                id = "sock",
                displayName = "Calcín",
                hotspot = Rect(240f, 540f, 330f, 640f),
                iconRes = R.drawable.findout_obj_sock,
            ),
            HiddenObjectDef(
                id = "paper_plane",
                displayName = "Avión de papel",
                hotspot = Rect(70f, 90f, 155f, 175f),
                iconRes = R.drawable.findout_obj_paper_plane,
            ),
            HiddenObjectDef(
                id = "rubik",
                displayName = "Cubo Rubik",
                hotspot = Rect(455f, 990f, 540f, 1075f),
                iconRes = R.drawable.findout_obj_rubik,
            ),
            HiddenObjectDef(
                id = "keys",
                displayName = "Llaves",
                hotspot = Rect(420f, 1185f, 505f, 1270f),
                iconRes = R.drawable.findout_obj_keys,
            ),
            HiddenObjectDef(
                id = "dino",
                displayName = "Dinosaurio",
                hotspot = Rect(130f, 300f, 220f, 400f),
                iconRes = R.drawable.findout_obj_dino,
            ),
        ),
    )

    /** Cocina del caos — utensilios perdidos. */
    val nivel2 = LevelConfig(
        levelId = "nivel_2",
        backgroundRes = R.drawable.findout_background_2,
        imageWidth = W,
        imageHeight = H,
        enablePan = false,
        zoomMode = ZoomMode.FIT_ENTIRE,
        enablePinchZoom = true,
        minUserZoom = 1f,
        maxUserZoom = 3.5f,
        objects = listOf(
            // Entre trapos del fregadero (izq / centro)
            HiddenObjectDef(
                id = "spatula",
                displayName = "Espátula",
                hotspot = overlayRect(240f, 820f, W, 0.08f),
                iconRes = R.drawable.findout_obj_spatula,
            ),
            // Encimera junto a harina
            HiddenObjectDef(
                id = "egg",
                displayName = "Huevo",
                hotspot = overlayRect(500f, 720f, W, 0.07f),
                iconRes = R.drawable.findout_obj_egg,
            ),
            // Borde de la mesa
            HiddenObjectDef(
                id = "mug",
                displayName = "Taza",
                hotspot = overlayRect(430f, 1200f, W, 0.08f),
                iconRes = R.drawable.findout_obj_mug,
            ),
            // Nevera entre imanes
            HiddenObjectDef(
                id = "recipe",
                displayName = "Receta",
                hotspot = overlayRect(820f, 540f, W, 0.075f),
                iconRes = R.drawable.findout_obj_recipe,
            ),
            // Cesta de verduras
            HiddenObjectDef(
                id = "chili",
                displayName = "Chile",
                hotspot = overlayRect(680f, 1080f, W, 0.08f),
                iconRes = R.drawable.findout_obj_chili,
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
