class_name GameManager
extends Node
## Gestiona el progreso del nivel: total, encontrados y victoria.

signal object_found(object_id: String, display_name: String, found_count: int, total: int)
signal level_completed(found_count: int, total: int)
signal progress_changed(found_count: int, total: int)

var total_objects: int = 0
var found_count: int = 0
var _found_ids: Dictionary = {} # String -> true


func start_level(total: int) -> void:
	total_objects = maxi(total, 0)
	found_count = 0
	_found_ids.clear()
	progress_changed.emit(found_count, total_objects)


func mark_found(object_id: String, display_name: String = "") -> bool:
	if object_id.is_empty() or _found_ids.has(object_id):
		return false
	if total_objects <= 0:
		push_warning("GameManager: mark_found llamado sin start_level.")
		return false

	_found_ids[object_id] = true
	found_count += 1
	object_found.emit(object_id, display_name, found_count, total_objects)
	progress_changed.emit(found_count, total_objects)

	if found_count >= total_objects:
		level_completed.emit(found_count, total_objects)
	return true


func is_found(object_id: String) -> bool:
	return _found_ids.has(object_id)


func is_level_complete() -> bool:
	return total_objects > 0 and found_count >= total_objects


func remaining() -> int:
	return maxi(total_objects - found_count, 0)
