# Requirements

- arm64-v8a, Android 13 or newer (`minSdk 33`): the renderer needs Vulkan 1.3, which devices ship from that version on. The manifest requires it.
- The features listed in VULKAN_COMPATIBILITY.md. The game names any that are missing and does not start.
- The player's own `chunk1.psarc`, `texture.qar` and `pathid_list_ps4.bin` (CUSA01127). None are in the APK.
- No storage or microphone permission.

Only one device has been tested. This is not a compatibility claim for others.
