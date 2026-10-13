# Prompt base — Nuevo nivel Find Out (Android AAR)

Copia el bloque **«Pedido»**, rellena los campos y pégalo en el chat del agente.
El agente debe seguir el patrón del **Nivel 1** (`nivel_1` + `findout_background_1` + sprites PNG `findout_obj_*`).

## Regla crítica — el agente CREA las imágenes

Al recibir este prompt, el agente **debe generar el arte**, no solo el código:

1. **Fondo:** generar ilustración PNG del escenario (misma calidad/estilo que `findout_background_1.png` salvo que se pida otro).
2. **Cada objeto:** generar sprite PNG aislado, estilo igual al fondo (para camuflaje), con **fondo transparente**.
3. Usar la herramienta de generación de imágenes del IDE (`GenerateImage` o equivalente), con el fondo como `reference_image_paths` cuando exista.
4. Procesar/exportar a `findout/src/main/res/drawable/` con prefijo `findout_`.
5. **Prohibido** dejar placeholders, vectores planos XML como sustituto, reutilizar iconos de otro nivel “por ahora”, o pedir al usuario que aporte el arte (salvo que el pedido diga lo contrario).

Sin imágenes nuevas en drawable + hotspots cableados, el nivel **no está terminado**.

---

## Pedido (rellenar y pegar)

```text
/feature nuevo-nivel Find Out

Usa como plantilla: find-out-game/docs/PROMPT_NUEVO_NIVEL.md
y el Nivel 1 existente en LevelCatalog.nivel1.

IMPORTANTE: Tú (el agente) DEBES CREAR las imágenes del fondo y de TODOS los objetos
con GenerateImage (o equivalente), estilo/calidad como findout_background_1,
objetos con transparencia, guardados en findout/src/main/res/drawable/.
No uses placeholders ni omitas el arte.

### Identidad
- level_id: nivel_N          # ej. nivel_2 (snake_case, estable)
- título menú: Nivel N — <nombre corto>
- descripción corta: <1 frase>

### Temática / escena
- tema: <ej. cocina desordenada / playa al atardecer / taller>
- mood / estilo visual: <igual que nivel 1: ilustración cartoon detallada, o describir>
- referencias (opcional): <URL, archivo, o "como findout_background_1">

### Lienzo (fondo)
- ancho_px: <ej. 1024>
- alto_px: <ej. 1536>
- aspect: <ej. 2:3 | 1:1 | 4:3 | 16:9>
- zoom_mode: FIT_ENTIRE | COVER_SHORT_SIDE | FIXED
- enable_pan: true | false     # true si el mapa no cabe / COVER
- enable_pinch_zoom: true      # default true
- min_user_zoom: 1.0
- max_user_zoom: 3.5

### Objetos a encontrar
- cantidad: <ej. 5>
- lista (id | nombre UI | pista de colocación en la escena):
  1. <id_snake> | <Nombre> | <dónde se camufla>
  2. ...
- dificultad: fácil | media | difícil
- tamaño overlay aprox. (fracción del ancho del mapa): 0.06–0.10

### Entregables obligatorios (agente)
1. GENERAR imagen de fondo → findout_background_<N>.png (calidad nivel 1).
2. GENERAR una imagen por objeto → findout_obj_<id>.png (transparente, camuflaje).
3. Prefijo de recursos: findout_ (resourcePrefix del módulo).
4. Registrar en LevelCatalog (LevelConfig.kt) con hotspots Rect en píxeles del fondo.
5. Cablear menú y LevelCatalog.all + nextAfter.
6. Barra superior: solo iconos; sin textos de estado.
7. Pinch-zoom + clip; BoardCamera síncrona.
8. ./gradlew :findout:testDebugUnitTest :findout:assembleRelease :findout:publishToMavenLocal
9. No meter código del juego en MiniJuegos; solo republicar AAR.

### Fuera de alcance (salvo que se pida)
- Godot (opcional después)
- Modos extra, pistas, ads
- Que el usuario dibuje o suba el arte (el agente lo genera)
```

---

## Ejemplo relleno (cocina)

```text
/feature nuevo-nivel Find Out

Usa: find-out-game/docs/PROMPT_NUEVO_NIVEL.md + LevelCatalog.nivel1

### Identidad
- level_id: nivel_2
- título menú: Nivel 2 — Cocina del caos
- descripción corta: Encuentra los utensilios perdidos en la cocina

### Temática / escena
- tema: cocina familiar desordenada a media mañana
- mood / estilo visual: igual que findout_background_1 (cartoon detallado, colores vivos)
- referencias: findout_background_1.png

### Lienzo (fondo)
- ancho_px: 1024
- alto_px: 1536
- aspect: 2:3
- zoom_mode: FIT_ENTIRE
- enable_pan: false
- enable_pinch_zoom: true
- min_user_zoom: 1.0
- max_user_zoom: 3.5

### Objetos a encontrar
- cantidad: 5
- lista:
  1. spatula | Espátula | entre trapos del fregadero
  2. egg | Huevo | sobre la encimera junto a harina
  3. mug | Taza | borde de la mesa
  4. recipe | Receta | pegada en la nevera entre imanes
  5. chili | Chile | en la cesta de verduras
- dificultad: media
- tamaño overlay aprox.: 0.08

Implementa ya en :findout y publica a Maven Local.
```

---

## Checklist técnico (agente)

| Paso | Detalle |
|------|---------|
| Repo | Solo `find-out-game/` módulo `:findout` |
| **Arte fondo** | **Generar** con `GenerateImage` → `findout_background_<N>.png` |
| **Arte objetos** | **Generar** cada uno + quitar fondo → `findout_obj_<id>.png` (RGBA) |
| Referencia | Usar `findout_background_1` o el nuevo fondo como `reference_image_paths` |
| Dominio | `HiddenObjectDef(id, displayName, hotspot, iconRes)` |
| Catálogo | Añadir nivel en `LevelCatalog` y en `all` |
| Menú | Botón o lista desde `LevelCatalog.all` |
| Camuflaje | Mismo estilo que el fondo; hotspots en zonas de desorden |
| Zoom | `BoardCamera` + `clipToBounds` |
| Host | `publishToMavenLocal` → Sync MiniJuegos |
| Tests | Contar objetos / `GameManager` si aplica |
| DoD arte | Sin PNG nuevos del nivel → **no cerrar** la tarea |

### Coordenadas hotspot

- Origen: esquina **superior izquierda** del PNG.
- `Rect(left, top, right, bottom)` en píxeles del lienzo (`imageWidth` × `imageHeight`).
- El overlay se dibuja en ese rect; el hit-test usa el mismo `Rect`.
- Con pinch-zoom, los objetos viven en la capa transformada (no reposicionar a mano).

### Naming

```
level_id          → nivel_3
background        → findout_background_3.png
object id         → rubber_duck
object drawable   → findout_obj_rubber_duck.png
LevelCatalog      → nivel3 / o listado indexado
```

### Defaults recomendados (si el pedido omite campos)

| Campo | Default |
|-------|---------|
| Lienzo | 1024 × 1536 (2:3) |
| zoom_mode | `FIT_ENTIRE` |
| enable_pan | `false` (o `true` si COVER / mapa grande) |
| pinch | `true`, 1.0–3.5 |
| cantidad objetos | 5 |
| tamaño overlay | ~0.08 del ancho |

---

## Referencia viva

- Nivel plantilla: `findout/.../domain/LevelConfig.kt` → `nivel1`
- UI board: `FindOutPlayScreen.kt` (`BoardCamera`, barra solo iconos)
- Playbook AAR: `docs/MINIJUEGOS_AAR_PLAYBOOK.md` (monorepo AllInOne)
