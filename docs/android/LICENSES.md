# Licenses

This is an engineering inventory, checked against the license files of the pinned sources. It is not legal advice.

## The APK as a whole

The port's own code is MIT (`LICENSE`). The APK links no GPL component: nxvk, Mesa and lsfg-vk belong to the Switch build only, so the GPL terms of the Switch `.nro` do not carry over. `third_party/lsfg-vk` stays in the repository under its own license, but is not compiled for Android.

From the next release on, the APK carries the texts below, shown from the launcher's **Licencias** button (`third_party/android_notices`). **The published versions 0.1.0 and 0.1.1 carry only the fonts' notices.** The screen has been built but not yet opened on a device.

## What the APK contains

| Component | Version | License | Notice file |
|---|---|---|---|
| Port's own code | this repository | MIT | `LICENSE` |
| SDL3 | 3.4.16 | zlib | `SDL3.txt` |
| HIDAPI, inside SDL3 | SDL's copy | BSD-style, chosen among its three options | `HIDAPI-BSD.txt` |
| volk | 1.4.350 | MIT | `volk.txt` |
| Vulkan Memory Allocator | 3.4.0 | MIT | `VulkanMemoryAllocator.txt` |
| Vulkan-Headers | 1.4.350 | Apache-2.0 or MIT per file | `Vulkan-Headers.txt` |
| GLM | 1.0.3 | MIT, or the Happy Bunny license | `GLM.txt` |
| Dear ImGui | 1.92.9b-docking | MIT | `ImGui.txt` |
| stb | `master` at build time (not pinned) | MIT or Unlicense | `stb.txt` |
| Lua | 5.1.5 | MIT | `Lua.txt` |
| libogg | 1.3.5 | BSD-3-Clause | `libogg.txt` |
| libvorbis | 1.3.7 | BSD-3-Clause | `libvorbis.txt` |
| ww2ogg packed codebooks, in the binary | hash-pinned download | BSD-3-Clause | `ww2ogg.txt` |
| bc7enc_rdo, `bc7enc.cpp` only | pinned commit | MIT or Unlicense | `bc7enc_rdo.txt` |
| HarfBuzz | 14.6.0 | "Old MIT" | `HarfBuzz.txt` |
| LLVM libc++ (`libc++_shared.so`) | NDK 28.2.13676358 | Apache-2.0 with LLVM exceptions | `LLVM-NDK-NOTICE.txt`, from the NDK at build time |
| Noto fonts | see `assets/fonts/README.md` | SIL OFL 1.1 | `OFL-*.txt` |

The codebook file's license comes from ww2ogg's `COPYING` on its `master` branch, where the build downloads the file. Whether every byte of that data originates under that license was not traced further.

bc7enc_rdo's `bc7e.ispc` (Apache-2.0) and LodePNG are in its repository but not compiled here.

## Game content and branding

The APK and the repository hold no P.T. game files. The player supplies their own, and nothing here decrypts store packages.

Open point, not settled by any license above: the launcher icon and logo in `assets/icono.png` spell "P.T." in the style of the game's title, and the app's name uses "P.T.". P.T. and its marks belong to Konami, and this project has no permission from them. Whether to keep that branding is a decision for the project's owner. The README states the project is unaffiliated.

## When something changes

A new dependency or version needs its notice in `third_party/android_notices` and a row here. To check what the APK really links, list the libraries in `lib/arm64-v8a` and the sources in the Android `CMakeLists.txt`.
