class_name LevelConfig
extends Resource
## Configuración de un nivel Find Out: fondo + lista de objetos ocultos.

@export var level_id: String = "nivel_1"
@export var background: Texture2D
@export var objects: Array[HiddenObjectDef] = []
## Escena del siguiente nivel (opcional). Si está vacía, la UI solo ofrece reiniciar.
@export var next_level_scene: PackedScene
