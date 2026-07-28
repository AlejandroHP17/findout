class_name LevelConfig
extends Resource
## Configuración de un nivel Find Out: fondo + lista de objetos ocultos.

enum ZoomMode {
	USE_SCENE, ## Respeta los exports de la escena.
	FIT_ENTIRE,
	COVER_SHORT_SIDE,
	FIXED,
}

@export var level_id: String = "nivel_1"
@export var background: Texture2D
@export var objects: Array[HiddenObjectDef] = []
## Escena del siguiente nivel (opcional). Si está vacía, la UI solo ofrece reiniciar.
@export var next_level_scene: PackedScene

@export var enable_camera_pan: bool = false
@export var zoom_mode: ZoomMode = ZoomMode.USE_SCENE
@export var fixed_zoom: float = 1.0
