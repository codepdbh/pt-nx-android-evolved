# Requirements

- arm64-v8a, Android 13 or newer (`minSdk 33`): the renderer needs Vulkan 1.3, which devices ship from that version on. The manifest requires it.
- The features listed in VULKAN_COMPATIBILITY.md. The game names any that are missing and does not start.
- The player's own `chunk1.psarc`, `texture.qar` and `pathid_list_ps4.bin` (CUSA01127), in the `pt` folder of the shared storage (`/storage/emulated/0/pt`). None are in the APK.
- The "all files access" permission (`MANAGE_EXTERNAL_STORAGE`), to read that folder. Google Play restricts this permission, so a Play release would need another approach. No microphone permission.

Settings, saves, logs and unpacked runtime files stay in private storage: uninstalling the app deletes them, not the `pt` folder.

Only one device has been tested. This is not a compatibility claim for others.
