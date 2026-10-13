package pelkidev.com.mx.findout.presentation

import androidx.annotation.DrawableRes

data class ObjectiveUi(
    val id: String,
    val displayName: String,
    @param:DrawableRes val iconRes: Int,
    val found: Boolean,
)

data class FindOutUiState(
    val screen: FindOutScreenState = FindOutScreenState.Menu,
    val level: pelkidev.com.mx.findout.domain.LevelConfig? = null,
    val objectives: List<ObjectiveUi> = emptyList(),
    val foundCount: Int = 0,
    val totalCount: Int = 0,
    val statusMessage: String = "",
    val levelCompleted: Boolean = false,
    val hasNextLevel: Boolean = false,
    val lastFoundId: String? = null,
    val debugShowHotspots: Boolean = false,
)

enum class FindOutScreenState {
    Menu,
    Playing,
}
