# Known issues

- Game files are not on the test device. `tools`' documented route for a fake PKG (`installer/Extractor` with LibOrbisPkg) has not been run, and whether the local PKG opens with it is unknown. A store PKG is refused by design.
- Surface loss on pause and resume is not handled.
- No touch controls.
- The log reports the platform as "linux" (`update::Platform()`).
- The self test's window is not fullscreen: system bars stay visible. The game path's fullscreen is untested.
- The process stays cached after the activity exits. Relaunching in that process is untested.
- `ImportActivity` checks a signature only for `chunk1.psarc`. A wrong `texture.qar` is only caught when the engine mounts it, and the player then has no way back to the import screen.
- `assets/icono.png` has no adaptive icon version. Its small text will be hard to read at launcher size.

- Adreno 830 produces NaN pixels in the diffuse light buffer. Their source is not found: the exposure ignores them now, but they could still show in the image or spread through bloom.
