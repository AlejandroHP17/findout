extends Node2D
## Nivel Find Out genérico: fondo, hotspots, UI y pan de cámara opcional.

@export var level_config: LevelConfig
@export var debug_show_hotspots: bool = false
@export var next_level_scene: PackedScene

## Si true (Nivel 2+), permite arrastrar la cámara por el mapa.
@export var enable_camera_pan: bool = false
@export var zoom_mode: CameraPan.ZoomMode = CameraPan.ZoomMode.FIT_ENTIRE
@export var fixed_zoom: float = 1.0

@onready var background: Sprite2D = $Background
@onready var objects_root: Node2D = $Objects
@onready var camera: CameraPan = $Camera2D
@onready var game_manager: GameManager = $GameManager
@onready var game_ui: GameUI = $GameUI

var _gesture_start_screen: Vector2 = Vector2.ZERO
var _gesture_active: bool = false


func _ready() -> void:
	_apply_config()
	_setup_camera()
	_setup_ui()
	_spawn_hidden_objects()


func _apply_config() -> void:
	if level_config == null:
		push_error("Nivel: asigna un LevelConfig en el inspector.")
		return
	if level_config.background != null:
		background.texture = level_config.background
	background.centered = false
	background.position = Vector2.ZERO

	if level_config.enable_camera_pan:
		enable_camera_pan = true
	if level_config.zoom_mode != LevelConfig.ZoomMode.USE_SCENE:
		match level_config.zoom_mode:
			LevelConfig.ZoomMode.FIT_ENTIRE:
				zoom_mode = CameraPan.ZoomMode.FIT_ENTIRE
			LevelConfig.ZoomMode.COVER_SHORT_SIDE:
				zoom_mode = CameraPan.ZoomMode.COVER_SHORT_SIDE
			LevelConfig.ZoomMode.FIXED:
				zoom_mode = CameraPan.ZoomMode.FIXED
				fixed_zoom = level_config.fixed_zoom


func _setup_camera() -> void:
	if background.texture == null:
		return
	var map_size := background.texture.get_size()
	var mode := zoom_mode
	if not enable_camera_pan:
		mode = CameraPan.ZoomMode.FIT_ENTIRE
	camera.configure_for_map(map_size, mode, fixed_zoom, enable_camera_pan)


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
			push_warning("Nivel: HiddenObjectDef inválido (id vacío).")
			continue
		var hotspot := HiddenObject.new()
		# Con pan activo, el nivel resuelve el tap; Area2D no consume el gesto.
		hotspot.setup(def, debug_show_hotspots, not enable_camera_pan)
		hotspot.found.connect(_on_hotspot_found)
		objects_root.add_child(hotspot)
		if def.starts_found:
			game_manager.mark_found(def.id, def.display_name)


func _input(event: InputEvent) -> void:
	if not enable_camera_pan:
		return
	# No panear si la UI de victoria está capturando.
	if game_ui.victory_overlay.visible:
		return

	if event is InputEventScreenTouch:
		var touch := event as InputEventScreenTouch
		if touch.pressed:
			_gesture_active = true
			_gesture_start_screen = touch.position
			camera.handle_input(event)
		else:
			var was_tap := camera.was_tap_gesture() and _gesture_active
			var start := _gesture_start_screen
			camera.handle_input(event)
			if was_tap:
				_try_pick_at_screen(start)
			_gesture_active = false
		return

	if event is InputEventMouseButton and (event as InputEventMouseButton).button_index == MOUSE_BUTTON_LEFT:
		var mouse := event as InputEventMouseButton
		if mouse.pressed:
			_gesture_active = true
			_gesture_start_screen = mouse.position
			camera.handle_input(event)
		else:
			var was_tap := camera.was_tap_gesture() and _gesture_active
			var start := _gesture_start_screen
			camera.handle_input(event)
			if was_tap:
				_try_pick_at_screen(start)
			_gesture_active = false
		return

	if camera.handle_input(event):
		get_viewport().set_input_as_handled()


func _try_pick_at_screen(screen_pos: Vector2) -> void:
	var world_pos := camera.screen_to_world(screen_pos)
	for child in objects_root.get_children():
		var hotspot := child as HiddenObject
		if hotspot == null or hotspot.is_found:
			continue
		if hotspot.contains_world_point(world_pos):
			hotspot.try_find()
			break


func _on_hotspot_found(object_id: String, display_name: String) -> void:
	game_manager.mark_found(object_id, display_name)


func _on_restart_requested() -> void:
	get_tree().reload_current_scene()


func _on_next_level_requested() -> void:
	var next_scene := _resolve_next_level_scene()
	if next_scene == null:
		push_warning("Nivel: no hay next_level_scene; se reinicia.")
		get_tree().reload_current_scene()
		return
	get_tree().change_scene_to_packed(next_scene)
