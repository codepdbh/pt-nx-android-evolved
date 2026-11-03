# Porting status

Terms: Implemented, Compiled, Tested in development, Tested on real Android, Functional, Pending, Blocked.
Test device: Samsung Galaxy S25 Ultra (SM-S938B), Android 17, Adreno 830. Details: TEST_RESULTS.md.

| Phase | State | Evidence and gaps |
|---|---|---|
| A Audit | Done for the first pass | ARCHITECTURE_AUDIT.md. Shaders not yet audited one by one |
| B Minimal APK | Tested on real Android | Builds, installs, loads libmain.so, SDL3 starts, logs in Logcat, exits with code 0 |
| C Vulkan | Partly tested on real Android | Device, swapchain, scene renderer start and a presented image. No validation layer run; scene pipelines are not all exercised without game data |
| D File system | Partly tested on real Android | The engine reads the real archives from private storage, copied with adb. The picker flow is still untested |
| E First scene | Tested on real Android | The first scene renders with textures and lighting, confirmed by the player. Low frame rate, about 24 fps at 2340x1080 in the last measurement |
| F Gameplay | Blocked | Needs the game files, and G without a gamepad |
| G Touch HUD | Pending | Nothing implemented. Gamepads go through SDL3's normal path, untested |
| H Optimization | Pending | No Android profiles. First run applies the Low preset; Switch half-resolution paths not enabled |
| I Full validation | Pending | |

## Next concrete tasks

1. Touch HUD and menus adapted to Android, then a playable test.
2. Handle surface loss and restoration on pause and resume.
3. Touch HUD through `InputDevice`: joystick into `left_stick`, camera into `right_stick`, buttons into raw bits, a button for `voice_keyword_pressed`.
4. Android graphics profiles and a render size below native.


