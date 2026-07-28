class_name HiddenObject
extends Area2D
## Hotspot clickable generado desde HiddenObjectDef.

signal found(object_id: String, display_name: String)

@export var object_id: String = ""
@export var display_name: String = ""
@export var debug_draw: bool = false

var is_found: bool = false


func setup(def: HiddenObjectDef, show_debug: bool = false) -> void:
	object_id = def.id
	display_name = def.display_name
	debug_draw = show_debug
	position = def.hotspot.position

	var shape := RectangleShape2D.new()
	shape.size = def.hotspot.size

	var collision := CollisionShape2D.new()
	collision.shape = shape
	collision.position = def.hotspot.size * 0.5
	add_child(collision)

	input_pickable = true
	monitorable = false
	monitoring = false

	if def.starts_found:
		is_found = true
		visible = false


func _ready() -> void:
	input_event.connect(_on_input_event)
	if debug_draw:
		queue_redraw()


func _on_input_event(_viewport: Node, event: InputEvent, _shape_idx: int) -> void:
	if is_found:
		return
	if event is InputEventMouseButton:
		var mouse := event as InputEventMouseButton
		if mouse.pressed and mouse.button_index == MOUSE_BUTTON_LEFT:
			_try_find()
	elif event is InputEventScreenTouch:
		var touch := event as InputEventScreenTouch
		if touch.pressed:
			_try_find()


func _try_find() -> void:
	if is_found:
		return
	is_found = true
	input_pickable = false
	found.emit(object_id, display_name)
	_play_found_feedback()


func _play_found_feedback() -> void:
	# Mueve el origen al centro del rect para que el pop escale desde el medio.
	var collision := get_child(0) as CollisionShape2D
	if collision != null:
		position += collision.position
		collision.position = Vector2.ZERO

	var tween := create_tween()
	# 1) Pequeño "pop" de escala
	tween.tween_property(self, "scale", Vector2(1.28, 1.28), 0.12)\
		.set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
	# 2) Encoge y desaparece a la vez
	tween.tween_property(self, "scale", Vector2.ZERO, 0.22)\
		.set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_IN)
	tween.parallel().tween_property(self, "modulate:a", 0.0, 0.22)\
		.set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN)
	tween.tween_callback(func() -> void:
		visible = false
	)


func _draw() -> void:
	if not debug_draw or is_found:
		return
	var shape_node := get_child(0) as CollisionShape2D
	if shape_node == null or shape_node.shape == null:
		return
	var rect_shape := shape_node.shape as RectangleShape2D
	if rect_shape == null:
		return
	var half := rect_shape.size * 0.5
	var rect := Rect2(shape_node.position - half, rect_shape.size)
	draw_rect(rect, Color(1.0, 0.85, 0.1, 0.35), true)
	draw_rect(rect, Color(1.0, 0.85, 0.1, 0.9), false, 2.0)
