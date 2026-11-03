# Architecture audit

First pass, from reading the build files and platform code at commit 764d378 and measuring one device.

## Build structure

- Desktop: `CMakeLists.txt` and `cmake/*.cmake`. Downloads Windows upscaler SDKs, whisper.cpp and a Real-ESRGAN program.
- Switch: `switch/CMakeLists.txt`, a separate project with replacements in `src/platform/switch/`.
- Android: `android/app/src/main/cpp/CMakeLists.txt`, separate in the same way. `PT_ANDROID` marks its code; `PT_SWITCH` is not defined.

## Compatibility matrix

| Subsystem | Android | Notes |
|---|---|---|
| `src/game`, `engine/anim, assets, data, fs, physics, script, ui, vfx` | Reused, compiled | One change: float `from_chars` in the `--anim-test` tool, which NDK libc++ lacks |
| `engine/render` | Reused, starts on Adreno 830 | Swapchain transform, alpha and suboptimal handling changed |
| `engine/audio` | Reused, output opens | SDL3 output at 48 kHz. Tone test only: game audio not heard yet |
| `engine/platform/input.cpp` | Reused, compiled | No touch input yet |
| SDL3 | Official Android port, 3.4.16, shared | The Switch SDL replacement is not used |
| FSR, DLSS, XeSS, Streamline, OpenXR | Inert stubs | Out of scope for the first version |
| `http`, update check, LiveSplit, virtual pads | Inert stubs | The update check would point at PC releases |
| `os.cpp` | Replaced | `posix_spawn_file_actions_addchdir_np` needs API 34; child processes are not usable anyway |
| Microphone, whisper | Inert stubs | The word has a substitute: both triggers in `input.cpp`. No touch equivalent yet |
| Enhanced textures | Unavailable | Needs a desktop helper program; `RunProcess` reports not started |
| nxvk, lsfg-vk, Mesa, libnx | Not linked | |

## Dependencies

Same pinned versions as the other builds: Vulkan-Headers and volk 1.4.350, VMA 3.4.0, glm 1.0.3, ImGui 1.92.9b-docking, Lua 5.1.5, ogg 1.3.5, vorbis 1.3.7, bc7enc_rdo, HarfBuzz 14.6.0, SDL 3.4.16. `stb` follows `master` as elsewhere, so it is not reproducible. zlib comes from the system.

## Paths

`ExecutableDir()` resolves to `files/pt/runtime` (shaders and fonts unpacked from the APK). `PtActivity` passes `--game`, `--settings`, `--save-dir`, `--log` and `--mods` under `files/pt`.

## Risks

1. Surface loss on pause is not handled: likely failure on resume.
2. Other GPUs: the required feature set has no fallback, and BC textures have no conversion.
3. Memory: a shadow atlas of 8192x8192 is allocated at start. Device memory use is unmeasured.
4. The renderer assumes desktop-class passes: frame rate in real scenes is unknown.
