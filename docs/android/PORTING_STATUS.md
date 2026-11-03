# Porting status

Version 0.1.0, super experimental. Terms: Implemented, Compiled, Tested in development, Tested on real Android, Functional, Pending, Blocked.
Test device: Samsung Galaxy S25 Ultra (SM-S938B), Android 17, Adreno 830. Details: TEST_RESULTS.md.

| Phase | State | Evidence and gaps |
|---|---|---|
| A Audit | Done for the first pass | ARCHITECTURE_AUDIT.md. Shaders not yet audited one by one |
| B Minimal APK | Tested on real Android | Debug and signed release APKs install and run |
| C Vulkan | Tested on real Android | Real scenes render. No validation layer run yet |
| D File system | Partly tested on real Android | Reads the game files from the shared `pt` folder. The file picker has never been run with real files |
| E First scene | Tested on real Android | Textures, lights and shadows in the first rooms and the hallway |
| F Gameplay | In progress | Walking and camera tested; interactions, events and audio not checked in depth |
| G Touch HUD | Implemented, partly tested | Stick moves the player. Not configurable yet |
| H Optimization | Pending | No Android profiles; release build gives 40-48 fps in the hallway at native resolution |
| I Full validation | Pending | The game has not been finished on Android |

## Next concrete tasks

1. Play through to the end, checking events, audio, saves and the Jack button.
2. Android graphics profiles and a render size below native.
3. Find where the NaN light pixels come from.
4. Configurable HUD and a test of the file picker and gamepads.
