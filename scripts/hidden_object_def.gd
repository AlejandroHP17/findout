class_name HiddenObjectDef
extends Resource
## Definición de un objeto oculto en coordenadas de la textura de fondo (píxeles).

@export var id: String = ""
@export var display_name: String = ""
## Ícono opcional para la barra de objetivos de la UI.
@export var icon: Texture2D
## Rectángulo clickable en espacio de la imagen (origen arriba-izquierda).
@export var hotspot: Rect2 = Rect2(0, 0, 64, 64)
@export var starts_found: bool = false
