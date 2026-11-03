# Test results

## 2026-11-03, Galaxy S25 Ultra (SM-S938B), Android 17, Adreno 830, debug build

| Test | Result |
|---|---|
| `assembleDebug` | Pass; APK 41.1 MB with libmain, libSDL3 and libc++_shared for arm64-v8a, 73 shaders, 8 font files, no game data |
| Install and launch | Pass; import screen appears |
| Self test | Pass; exit code 0, window 2340x1080, one swapchain, 300 frames in 2459 ms |
| Scene renderer start | Pass; 655 ms, no Vulkan errors logged (no validation layer) |
| Presented image | Pass; screenshot shows the expected fill colour, upright |
| Audio output | Opened at 48 kHz. Device was muted, so the tone was not heard |

## Not run

Importing files, any game content, touch, gamepads, pause and resume, rotation, `assembleRelease`, the engine's unit tests on Android.

The desktop and Switch builds were not rebuilt after the shared-file changes (`main.cpp`, `vk_context`, `renderer.cpp`, `log.cpp`, `resource_path.cpp`, `anim_test.cpp`, `enhanced_textures.cpp`).

## 2026-11-03, same device, signed release build 0.1.0

| Test | Result |
|---|---|
| Launcher | Pass; shows the three files in `pt`, starts the game with Jugar |
| Scene rendering | Pass; textures, lights, shadows, subtitles in screenshots |
| Touch stick | Pass; player position changed (debug build) |
| Back to the game after leaving the app | Pass once; new Vulkan surface |
| Frame rate in the hallway | 40-48 fps, 20-24 ms GPU, 2.4 GB PSS |

Not run: file picker, gamepads, Jack button in its event, full playthrough, saves after restart.

The desktop and Switch builds have still not been rebuilt after the shared-file changes.

## 2026-11-03, same device, signed release build 0.1.1

| Test | Result |
|---|---|
| Resolution selector | Pass; the log reports 1170x540 and 1560x720 |
| Report button | Pass; opens the GitHub app's issue form filled in, log on the clipboard. Not sent |

Not run: menu touches below native resolution, the report button with a browser instead of the GitHub app.
