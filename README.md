# Find Out

Juego tipo *Find Out* embebible para [MiniJuegos](https://github.com/AlejandroHP17/MiniJuegos). Repo propio: library publicable (AAR) + app shell.

El prototipo Godot queda en [`godot/`](godot/) como referencia; el artefacto que consume el host es el módulo `:findout`.

## Artefacto Maven

| Coordenada | Valor |
|------------|--------|
| Group | `pelkidev.com.mx.minijuegos` |
| Artifact | `findout` |
| Versión (CI) | `1.0.0-SNAPSHOT` |
| Packages | `https://maven.pkg.github.com/AlejandroHP17/findout` |

Depende de: `pelkidev.com.mx.minijuegos:sdk:1.0.0`

## Módulos

| Módulo | Tipo | Rol |
|--------|------|-----|
| `:findout` | `android-library` | Lógica, UI Compose, `FindOutGameModule` → AAR |
| `:app` | `android-application` | Shell (`applicationId` = `pelkidev.com.mx.findout`) |

## Desarrollo local

```bash
./gradlew :app:installDebug
./gradlew :findout:publishToMavenLocal
```

## Integración MiniJuegos

1. Repo Maven del juego en `settings.gradle.kts`
2. `implementation("pelkidev.com.mx.minijuegos:findout:1.0.0-SNAPSHOT")`
3. Registrar `FindOutGameModule` en `GameCatalog`

## Ajustar objetos ocultos

Edita `LevelCatalog` en `findout/.../domain/LevelConfig.kt` (`Rect` en píxeles de la imagen). Activa `debugShowHotspots` desde el ViewModel si necesitas ver las zonas.

## CI

Push a `main` → build/test + publish. Secret: `GH_PACKAGES_TOKEN` (`read:packages` + `write:packages`).
