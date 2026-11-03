# Build

Needs Android SDK platform 36, NDK 28.2.13676358, CMake 3.30.5 (SDK Manager), JDK 17 or newer (Android Studio's `jbr` was used), Git and network access for the first build.

Set `ANDROID_HOME` or write `android/local.properties` with `sdk.dir=<path>`.

```bash
cd android
./gradlew assembleDebug      # Windows: .\gradlew.bat assembleDebug
```

Output: `android/app/build/outputs/apk/debug/app-debug.apk`. Dependencies go to `android/.deps/`. A full first build took about 9 minutes.

`assembleRelease` has no signing configured and has not been run.

## Self test, without game files (debug builds)

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.codepdbh.ptevolved/.ImportActivity --ez pt_selftest true
adb logcat -s pt:*
```

Starts the renderer and scene renderer, presents 300 frames, plays a tone and exits.
