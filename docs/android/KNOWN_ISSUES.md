# Known issues

- Adreno 830 produces NaN pixels in the diffuse light buffer, source unknown. They once made the whole picture a flat sky colour through the exposure, which now ignores them; they could still show as artefacts or spread through bloom.
- The file picker in the launcher has not been run with real files.
- Only `chunk1.psarc` has its signature checked by the launcher. A wrong `texture.qar` is only caught when the engine mounts it.
- The on-screen controls cannot be configured yet.
- The log reports the platform as "linux" (`update::Platform()`).
- The process stays cached after the activity exits. Relaunching in that process is untested.
- Enhanced textures and microphone recognition are unavailable.
