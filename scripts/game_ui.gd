class_name GameUI
extends CanvasLayer
## HUD del nivel: barra de objetivos + overlay de victoria.
## Debe ser hijo directo del nivel (hermano de Background/Objects), no hijo del Sprite2D.

signal restart_requested
signal next_level_requested

@export var found_modulate: Color = Color(0.55, 0.55, 0.55, 0.7)
@export var pending_modulate: Color = Color(1, 1, 1, 1)
## Si true, el chip se desvanece al encontrarlo; si false, se tacha y atenúa.
@export var hide_when_found: bool = false
## Si > 0 y hay siguiente nivel, lo carga solo tras esta espera (segundos).
@export var auto_next_delay_sec: float = 0.0

@onready var progress_label: Label = %ProgressLabel
@onready var status_label: Label = %StatusLabel
@onready var objectives_list: HBoxContainer = %ObjectivesList
@onready var victory_overlay: Control = %VictoryOverlay
@onready var victory_label: Label = %VictoryLabel
@onready var restart_button: Button = %RestartButton
@onready var next_button: Button = %NextButton
@onready var primary_button: Button = %PrimaryButton

var _game_manager: GameManager
var _chips: Dictionary = {} # object_id -> PanelContainer
var _next_level_scene: PackedScene
var _auto_next_tween: Tween


func _ready() -> void:
	layer = 10
	victory_overlay.visible = false
	victory_overlay.mouse_filter = Control.MOUSE_FILTER_IGNORE
	restart_button.pressed.connect(_on_restart_pressed)
	next_button.pressed.connect(_on_next_pressed)
	primary_button.pressed.connect(_on_primary_pressed)


func setup(
	manager: GameManager,
	objects: Array[HiddenObjectDef],
	next_level_scene: PackedScene = null
) -> void:
	_disconnect_manager()
	_cancel_auto_next()
	_game_manager = manager
	_next_level_scene = next_level_scene
	_clear_objectives()
	_build_objectives(objects)
	_connect_manager()
	_refresh_progress(manager.found_count, manager.total_objects)
	_configure_victory_buttons()
	status_label.text = "Toca los objetos ocultos"
	victory_overlay.visible = false
	victory_overlay.mouse_filter = Control.MOUSE_FILTER_IGNORE


func _configure_victory_buttons() -> void:
	var has_next := _next_level_scene != null
	next_button.visible = has_next
	restart_button.visible = has_next
	# Un solo botón principal: siguiente si existe, si no reiniciar.
	primary_button.visible = true
	if has_next:
		primary_button.text = "Siguiente nivel"
		restart_button.text = "Reiniciar"
		next_button.text = "Siguiente nivel"
		# Con siguiente nivel, el primario basta; ocultamos el duplicado "Siguiente".
		next_button.visible = false
	else:
		primary_button.text = "Reiniciar nivel"


func _disconnect_manager() -> void:
	if _game_manager == null:
		return
	if _game_manager.object_found.is_connected(_on_object_found):
		_game_manager.object_found.disconnect(_on_object_found)
	if _game_manager.progress_changed.is_connected(_on_progress_changed):
		_game_manager.progress_changed.disconnect(_on_progress_changed)
	if _game_manager.level_completed.is_connected(_on_level_completed):
		_game_manager.level_completed.disconnect(_on_level_completed)


func _connect_manager() -> void:
	if _game_manager == null:
		return
	_game_manager.object_found.connect(_on_object_found)
	_game_manager.progress_changed.connect(_on_progress_changed)
	_game_manager.level_completed.connect(_on_level_completed)


func _clear_objectives() -> void:
	for child in objectives_list.get_children():
		child.queue_free()
	_chips.clear()


func _build_objectives(objects: Array[HiddenObjectDef]) -> void:
	for def in objects:
		if def == null or def.id.is_empty():
			continue
		var chip := _create_chip(def)
		objectives_list.add_child(chip)
		_chips[def.id] = chip
		if def.starts_found:
			_apply_found_style(chip, def.display_name, false)


func _create_chip(def: HiddenObjectDef) -> PanelContainer:
	var chip := PanelContainer.new()
	chip.mouse_filter = Control.MOUSE_FILTER_IGNORE
	chip.modulate = pending_modulate

	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.05, 0.07, 0.1, 0.82)
	style.set_corner_radius_all(12)
	style.content_margin_left = 12
	style.content_margin_right = 12
	style.content_margin_top = 8
	style.content_margin_bottom = 8
	chip.add_theme_stylebox_override("panel", style)

	var row := HBoxContainer.new()
	row.mouse_filter = Control.MOUSE_FILTER_IGNORE
	row.add_theme_constant_override("separation", 8)
	chip.add_child(row)

	if def.icon != null:
		var icon := TextureRect.new()
		icon.texture = def.icon
		icon.custom_minimum_size = Vector2(36, 36)
		icon.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
		icon.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
		icon.mouse_filter = Control.MOUSE_FILTER_IGNORE
		row.add_child(icon)

	var label := Label.new()
	label.name = "NameLabel"
	label.text = def.display_name
	label.mouse_filter = Control.MOUSE_FILTER_IGNORE
	label.add_theme_font_size_override("font_size", 22)
	label.add_theme_color_override("font_color", Color(1, 1, 1, 1))
	row.add_child(label)

	chip.set_meta("display_name", def.display_name)
	chip.set_meta("found", false)
	return chip


func _on_object_found(object_id: String, display_name: String, _found_count: int, _total: int) -> void:
	var chip: PanelContainer = _chips.get(object_id) as PanelContainer
	if chip != null:
		_apply_found_style(chip, display_name, true)
	status_label.text = "¡Encontraste: %s!" % display_name


func _apply_found_style(chip: PanelContainer, display_name: String, animate: bool) -> void:
	if chip.get_meta("found", false):
		return
	chip.set_meta("found", true)

	var label := chip.find_child("NameLabel", true, false) as Label
	if label != null:
		label.text = "✓ %s" % display_name
		label.add_theme_color_override("font_color", Color(0.75, 0.75, 0.75, 1))

	if hide_when_found:
		if animate:
			var tween := create_tween()
			tween.tween_property(chip, "modulate:a", 0.0, 0.3)
			tween.tween_callback(chip.hide)
		else:
			chip.hide()
	else:
		chip.modulate = found_modulate


func _on_progress_changed(found_count: int, total: int) -> void:
	_refresh_progress(found_count, total)


func _refresh_progress(found_count: int, total: int) -> void:
	progress_label.text = "Encontrados: %d / %d" % [found_count, total]


func _on_level_completed(_found_count: int, _total: int) -> void:
	status_label.text = "¡Nivel completado!"
	_configure_victory_buttons()
	victory_overlay.visible = true
	victory_overlay.mouse_filter = Control.MOUSE_FILTER_STOP
	victory_overlay.modulate.a = 0.0
	var tween := create_tween()
	tween.tween_property(victory_overlay, "modulate:a", 1.0, 0.35)

	if _next_level_scene != null and auto_next_delay_sec > 0.0:
		_cancel_auto_next()
		_auto_next_tween = create_tween()
		_auto_next_tween.tween_interval(auto_next_delay_sec)
		_auto_next_tween.tween_callback(_emit_next_level)


func _cancel_auto_next() -> void:
	if _auto_next_tween != null and _auto_next_tween.is_valid():
		_auto_next_tween.kill()
	_auto_next_tween = null


func _on_primary_pressed() -> void:
	_cancel_auto_next()
	if _next_level_scene != null:
		_emit_next_level()
	else:
		restart_requested.emit()


func _on_restart_pressed() -> void:
	_cancel_auto_next()
	restart_requested.emit()


func _on_next_pressed() -> void:
	_cancel_auto_next()
	_emit_next_level()


func _emit_next_level() -> void:
	next_level_requested.emit()
