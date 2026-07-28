class_name CameraPan
extends Camera2D
## Cámara con pan táctil/mouse y límites al tamaño del mapa.
## Los toques cortos (tap) no mueven la cámara; los arrastres sí.

signal pan_started
signal pan_ended

@export var pan_enabled: bool = true
## Píxeles de pantalla antes de considerar el gesto un arrastre (no un tap).
@export var drag_threshold_px: float = 14.0
@export var pan_inertia: float = 0.0 ## 0 = sin inercia; >0 suaviza (experimental)

enum ZoomMode {
	FIT_ENTIRE, ## Encaja el mapa completo (sin scroll si cabe).
	COVER_SHORT_SIDE, ## Llena el lado corto; permite pan en el eje sobrante.
	FIXED, ## Usa fixed_zoom (útil en mapas muy grandes).
}

var zoom_mode: ZoomMode = ZoomMode.COVER_SHORT_SIDE
var fixed_zoom: float = 1.0

var map_size: Vector2 = Vector2.ZERO
var is_dragging: bool = false
var did_pan_this_gesture: bool = false

var _pointer_down: bool = false
var _last_screen_pos: Vector2 = Vector2.ZERO
var _drag_accum_px: float = 0.0


func configure_for_map(
	p_map_size: Vector2,
	p_zoom_mode: ZoomMode = ZoomMode.COVER_SHORT_SIDE,
	p_fixed_zoom: float = 1.0,
	p_pan_enabled: bool = true,
) -> void:
	map_size = p_map_size
	zoom_mode = p_zoom_mode
	fixed_zoom = p_fixed_zoom
	pan_enabled = p_pan_enabled
	_apply_zoom()
	position = map_size * 0.5
	_clamp_to_bounds()
	make_current()


func _apply_zoom() -> void:
	if map_size == Vector2.ZERO:
		return
	var viewport_size := get_viewport_rect().size
	match zoom_mode:
		ZoomMode.FIT_ENTIRE:
			var z := minf(viewport_size.x / map_size.x, viewport_size.y / map_size.y)
			zoom = Vector2(z, z)
		ZoomMode.COVER_SHORT_SIDE:
			# Escala para cubrir el eje más restrictivo → suele sobrar mapa en el otro.
			var z := maxf(viewport_size.x / map_size.x, viewport_size.y / map_size.y)
			zoom = Vector2(z, z)
		ZoomMode.FIXED:
			zoom = Vector2(fixed_zoom, fixed_zoom)


func handle_input(event: InputEvent) -> bool:
	## Devuelve true si consumió el evento (pan en curso).
	if not pan_enabled:
		return false

	if event is InputEventScreenTouch:
		var touch := event as InputEventScreenTouch
		if touch.pressed:
			_begin_pointer(touch.position)
		else:
			_end_pointer()
		return false

	if event is InputEventScreenDrag:
		var drag := event as InputEventScreenDrag
		return _move_pointer(drag.position)

	if event is InputEventMouseButton:
		var mouse := event as InputEventMouseButton
		if mouse.button_index == MOUSE_BUTTON_LEFT:
			if mouse.pressed:
				_begin_pointer(mouse.position)
			else:
				_end_pointer()
		return false

	if event is InputEventMouseMotion and _pointer_down:
		var motion := event as InputEventMouseMotion
		return _move_pointer(motion.position)

	return false


func was_tap_gesture() -> bool:
	return not did_pan_this_gesture


func screen_to_world(screen_pos: Vector2) -> Vector2:
	# Convierte coordenada de viewport → mundo 2D con la transform de canvas actual.
	return get_canvas_transform().affine_inverse() * screen_pos


func _begin_pointer(screen_pos: Vector2) -> void:
	_pointer_down = true
	_last_screen_pos = screen_pos
	_drag_accum_px = 0.0
	did_pan_this_gesture = false
	is_dragging = false


func _move_pointer(screen_pos: Vector2) -> bool:
	if not _pointer_down:
		return false
	var delta_screen := screen_pos - _last_screen_pos
	_last_screen_pos = screen_pos
	_drag_accum_px += delta_screen.length()

	if not is_dragging and _drag_accum_px < drag_threshold_px:
		return false

	if not is_dragging:
		is_dragging = true
		did_pan_this_gesture = true
		pan_started.emit()

	# Arrastrar el dedo a la derecha → el mapa se mueve a la derecha (cámara al revés).
	position -= delta_screen / zoom
	_clamp_to_bounds()
	return true


func _end_pointer() -> void:
	if is_dragging:
		pan_ended.emit()
	_pointer_down = false
	is_dragging = false


func _clamp_to_bounds() -> void:
	if map_size == Vector2.ZERO:
		return
	var view_size := get_viewport_rect().size / zoom
	var half := view_size * 0.5

	if map_size.x <= view_size.x:
		position.x = map_size.x * 0.5
	else:
		position.x = clampf(position.x, half.x, map_size.x - half.x)

	if map_size.y <= view_size.y:
		position.y = map_size.y * 0.5
	else:
		position.y = clampf(position.y, half.y, map_size.y - half.y)
