package pelkidev.com.mx.findout.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pelkidev.com.mx.findout.domain.GameManager
import pelkidev.com.mx.findout.domain.LevelCatalog
import pelkidev.com.mx.findout.domain.LevelConfig

class FindOutViewModel : ViewModel() {
    private val gameManager = GameManager()
    private val _uiState = MutableStateFlow(FindOutUiState())
    val uiState: StateFlow<FindOutUiState> = _uiState.asStateFlow()

    fun startLevel(level: LevelConfig = LevelCatalog.nivel1) {
        gameManager.startLevel(level.objects.size)
        level.objects.filter { it.startsFound }.forEach { gameManager.markFound(it.id) }

        _uiState.value = FindOutUiState(
            screen = FindOutScreenState.Playing,
            level = level,
            objectives = level.objects.map { def ->
                ObjectiveUi(def.id, def.displayName, gameManager.isFound(def.id))
            },
            foundCount = gameManager.foundCount,
            totalCount = gameManager.totalObjects,
            statusMessage = "Toca los objetos ocultos",
            levelCompleted = gameManager.isLevelComplete(),
            hasNextLevel = LevelCatalog.nextAfter(level.levelId) != null,
            lastFoundId = null,
            debugShowHotspots = false,
        )
    }

    fun onImageTap(imageX: Float, imageY: Float) {
        val state = _uiState.value
        val level = state.level ?: return
        if (state.levelCompleted) return

        val hit = level.objects.firstOrNull { def ->
            !gameManager.isFound(def.id) && def.hotspot.contains(Offset(imageX, imageY))
        } ?: return

        if (!gameManager.markFound(hit.id)) return

        _uiState.update { current ->
            current.copy(
                objectives = current.objectives.map { obj ->
                    if (obj.id == hit.id) obj.copy(found = true) else obj
                },
                foundCount = gameManager.foundCount,
                totalCount = gameManager.totalObjects,
                statusMessage = "¡Encontraste: ${hit.displayName}!",
                levelCompleted = gameManager.isLevelComplete(),
                lastFoundId = hit.id,
            )
        }
    }

    fun restartLevel() {
        startLevel(_uiState.value.level ?: LevelCatalog.nivel1)
    }

    fun goNextLevel() {
        val currentId = _uiState.value.level?.levelId ?: return
        val next = LevelCatalog.nextAfter(currentId) ?: return
        startLevel(next)
    }

    fun backToMenu() {
        _uiState.value = FindOutUiState(screen = FindOutScreenState.Menu)
    }

    fun toggleDebugHotspots() {
        _uiState.update { it.copy(debugShowHotspots = !it.debugShowHotspots) }
    }

    fun clearLastFoundAnim() {
        _uiState.update { it.copy(lastFoundId = null) }
    }
}
