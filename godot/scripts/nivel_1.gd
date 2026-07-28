extends Node2D
## Nivel Find Out: carga fondo + spawnea hotspots desde LevelConfig.

@export var level_config: LevelConfig
## Si está activo, dibuja los rectángulos de los hotspots (útil para ajustar coords).
@export var debug_show_hotspots: bool = true
## Alternativa a LevelConfig.next_level_scene (tiene prioridad el del config).
@export var next_level_scene: PackedScene

@onready var background: Sprite2D = $Background
@onready var objects_root: Node2D = $Objects
@onready var camera: Camera2D = $Camera2D
@onready var game_manager: GameManager = $GameManager
@onready var game_ui: GameUI = $GameUI


func _ready() -> void:
	_apply_config()
	_fit_camera_to_background()
	_setup_ui()
	_spawn_hidden_objects()


func _apply_config() -> void:
	if level_config == null:
		push_error("Nivel1: asigna un LevelConfig en el inspector.")
		return
	if level_config.background != null:
		background.texture = level_config.background
	background.centered = false
	background.position = Vector2.ZERO


func _fit_camera_to_background() -> void:
	if background.texture == null:
		return
	var tex_size := background.texture.get_size()
	var viewport_size := get_viewport_rect().size
	var zoom := minf(viewport_size.x / tex_size.x, viewport_size.y / tex_size.y)
	camera.zoom = Vector2(zoom, zoom)
	camera.position = tex_size * 0.5
	camera.make_current()


func _setup_ui() -> void:
	var objects: Array[HiddenObjectDef] = []
	if level_config != null:
		objects = level_config.objects
	game_ui.setup(game_manager, objects, _resolve_next_level_scene())
	if not game_ui.restart_requested.is_connected(_on_restart_requested):
		game_ui.restart_requested.connect(_on_restart_requested)
	if not game_ui.next_level_requested.is_connected(_on_next_level_requested):
		game_ui.next_level_requested.connect(_on_next_level_requested)


func _resolve_next_level_scene() -> PackedScene:
	if level_config != null and level_config.next_level_scene != null:
		return level_config.next_level_scene
	return next_level_scene


func _spawn_hidden_objects() -> void:
	for child in objects_root.get_children():
		child.queue_free()

	if level_config == null:
		return

	var defs := level_config.objects
	game_manager.start_level(defs.size())

	for def in defs:
		if def == null or def.id.is_empty():
			push_warning("Nivel1: HiddenObjectDef inválido (id vacío).")
			continue
		var hotspot := HiddenObject.new()
		hotspot.setup(def, debug_show_hotspots)
		hotspot.found.connect(_on_hotspot_found)
		objects_root.add_child(hotspot)
		if def.starts_found:
			game_manager.mark_found(def.id, def.display_name)


func _on_hotspot_found(object_id: String, display_name: String) -> void:
	game_manager.mark_found(object_id, display_name)


func _on_restart_requested() -> void:
	get_tree().reload_current_scene()


func _on_next_level_requested() -> void:
	var next_scene := _resolve_next_level_scene()
	if next_scene == null:
		push_warning("Nivel1: no hay next_level_scene asignada; se reinicia el nivel.")
		get_tree().reload_current_scene()
		return
	get_tree().change_scene_to_packed(next_scene)
