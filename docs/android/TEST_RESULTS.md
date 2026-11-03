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
