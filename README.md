# P.T. Android Evolved

P.T. (Silent Hills) ejecutándose de forma nativa en Android ARM64, con C++20, Vulkan y SDL3. Sin emulación ni streaming: es el motor reconstruido del [P.T. PC Port](https://github.com/LoreanXavier/pt-pc), a través de su [port para Nintendo Switch](https://github.com/r4lix/pt-nx), compilado para el teléfono.

> [!WARNING]
> **Versión súper experimental.** Solo se ha probado en un teléfono, el juego no se ha completado de principio a fin en Android y el rendimiento aún no está optimizado. Espera errores y cierres inesperados.

**No incluye contenido del juego.** Necesitas tu propia copia de P.T. para PS4 (versión de EE. UU., CUSA01127).

## Qué funciona en Android

| Función | Estado |
|---|---|
| APK nativa ARM64 con el motor y la lógica del juego | Funciona |
| Renderizado Vulkan 1.3 con texturas, luces y sombras | Funciona en el pasillo inicial |
| Lectura de los archivos originales desde la carpeta `pt` | Funciona |
| Launcher con estado de archivos y botón Jugar | Funciona; falta probar el selector de archivos |
| Joystick táctil, cámara táctil y botones | Movimiento comprobado; el resto, en pruebas |
| Menús originales con cruceta y botones virtuales | Implementado, en pruebas |
| Botón "Jack" para el evento de voz, sin micrófono | Implementado, sin probar en el evento |
| Volver al juego después de salir a otra app | Funciona en la prueba realizada |
| Mandos Bluetooth y USB | Sin probar |
| Audio del juego | Se inicia; falta comprobarlo a fondo |
| Guardado de partidas | Sin probar en Android |

## Limitaciones conocidas

- **Dispositivos:** solo se ha probado en un Samsung Galaxy S25 Ultra (Adreno 830, Android 17). Otros teléfonos pueden no funcionar. Sin soporte de Vulkan 1.3 y texturas BC, el juego avisa de lo que falta y no arranca.
- **Rendimiento:** unos 40-48 fps en el pasillo en la prueba realizada, a la resolución completa de la pantalla y sin perfiles gráficos ni optimizaciones para móvil. No se ha medido en sesiones largas ni en todo el juego.
- **Controles:** el tamaño y la posición de los botones todavía no se pueden configurar.
- **Imagen:** el Adreno 830 genera algunos píxeles de luz inválidos cuyo origen aún no se conoce. La exposición ya los ignora, pero podrían verse como artefactos.

## Requisitos

- Android 13 o superior, ARM64, con Vulkan 1.3 y texturas BC.
- 1,3 GB libres para los archivos del juego, más el espacio de la app.
- `chunk1.psarc`, `texture.qar` y `pathid_list_ps4.bin` de tu propia copia del juego.

## Instalación

1. Descarga la APK desde [Releases](https://github.com/codepdbh/pt-nx-android-evolved/releases) e instálala.
2. Copia los tres archivos a la carpeta `pt` de la memoria interna (`/storage/emulated/0/pt/`), o selecciónalos desde el launcher.
3. Abre la app y activa **Acceso a todos los archivos** cuando te lo pida. Lo necesita para leer esa carpeta.
4. Cuando los tres archivos aparezcan marcados, pulsa **Jugar**.

## Controles táctiles

- **Mitad izquierda:** joystick flotante para caminar.
- **Mitad derecha:** arrastra para mover la cámara.
- **✕:** interactuar. **Lupa:** zoom. **Pausa:** menú.
- **Jack:** aparece solo cuando el juego está escuchando.
- **Botón Atrás del sistema:** abre el menú de pausa.
- **En los menús:** toca las opciones directamente o usa la cruceta y los botones ✕ ○ △ □.

Al conectar un mando, los controles en pantalla se ocultan.

## Compilar

```bash
cd android
./gradlew assembleDebug      # En Windows: .\gradlew.bat assembleDebug
```

Los detalles y el estado de cada fase están en [docs/android](docs/android/): [BUILD.md](docs/android/BUILD.md), [PORTING_STATUS.md](docs/android/PORTING_STATUS.md) y [KNOWN_ISSUES.md](docs/android/KNOWN_ISSUES.md).

## Otras plataformas

Este repositorio parte de [pt-nx](https://github.com/r4lix/pt-nx) y conserva su código de Nintendo Switch: consulta [README-ADVANCED.md](README-ADVANCED.md) y [switch/README.md](switch/README.md). Para PC, consulta [README-PC.md](README-PC.md).

## Aviso

Este port se ha creado **con ayuda de herramientas de IA**, igual que el port de Switch en el que se basa. Puede contener errores, vulnerabilidades o funciones sin terminar. Úsalo bajo tu propia responsabilidad; no se garantiza soporte, estabilidad, compatibilidad ni seguridad.

## Créditos

- [LoreanXavier](https://github.com/LoreanXavier/pt-pc): el P.T. PC Port en el que se basa todo.
- [r4lix](https://github.com/r4lix/pt-nx): el port para Nintendo Switch del que parte este repositorio.
- [SDL](https://github.com/libsdl-org/SDL): ventana, entrada y audio en Android.
- P.T. por Kojima Productions (como 7780s Studio), © Konami.

El resto de componentes y sus licencias: [README-ADVANCED.md](README-ADVANCED.md#credits-and-licences) y [docs/android/LICENSES.md](docs/android/LICENSES.md).

P.T. es © Konami. Este es un proyecto de fans, sin afiliación ni respaldo de Konami.
