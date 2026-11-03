package io.github.codepdbh.ptevolved;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import org.libsdl.app.SDLActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Runs the engine: SDL loads libmain.so and calls its main() (src/main.cpp) on the SDL thread. */
public class PtActivity extends SDLActivity {
    private static final String TAG = "PtActivity";
    static final String EXTRA_ARGS = "pt_args";
    static final String EXTRA_SELFTEST = "pt_selftest";
    static final String EXTRA_ENV = "pt_env";
    static final String PREFERENCES = "launcher";
    static final String KEY_RENDER_HEIGHT = "render_height";  // 0: the screen's own

    @Override
    protected void onCreate(Bundle state) {
        PtPaths.ensureFolders(this);
        // the renderer's PT_* test switches are environment variables: --es pt_env "PT_A=0,PT_B=1" (debug builds only)
        String env = BuildConfig.DEBUG ? getIntent().getStringExtra(EXTRA_ENV) : null;
        if (env != null) {
            for (String pair : env.split(",")) {
                String[] parts = pair.split("=", 2);
                if (parts.length != 2) continue;
                try {
                    Os.setenv(parts[0].trim(), parts[1].trim(), true);
                } catch (ErrnoException e) {
                    Log.e(TAG, "cannot set " + pair, e);
                }
            }
        }
        int renderHeight = getSharedPreferences(PREFERENCES, MODE_PRIVATE).getInt(KEY_RENDER_HEIGHT, 0);
        try {
            Os.setenv("PT_RENDER_HEIGHT", Integer.toString(renderHeight), true);
        } catch (ErrnoException e) {
            Log.e(TAG, "cannot set the resolution", e);
        }
        if (BuildConfig.DEBUG && getIntent().getBooleanExtra(EXTRA_SELFTEST, false)) {
            try {
                Os.setenv("PT_SELFTEST", "1", true);
            } catch (ErrnoException e) {
                Log.e(TAG, "cannot request the self test", e);
            }
        }
        try {
            unpackRuntimeAssets();
        } catch (IOException | PackageManager.NameNotFoundException e) {
            Log.e(TAG, "cannot unpack the runtime files", e);
        }
        super.onCreate(state);
    }

    @Override
    protected String[] getLibraries() {
        return new String[] {"SDL3", "main"};
    }

    @Override
    protected String[] getArguments() {
        File root = PtPaths.root(this);
        List<String> args = new ArrayList<>(Arrays.asList(
                "--game", PtPaths.game(this).getPath(),
                "--settings", new File(root, "pt.ini").getPath(),
                "--save-dir", new File(root, "save").getPath(),
                "--log", new File(root, "logs/pt.log").getPath(),
                "--mods", new File(root, "mods").getPath(),
                "--no-update-check"));
        // test runs: adb shell am start ... --es pt_args "--stage x" (debug builds only)
        String extra = BuildConfig.DEBUG ? getIntent().getStringExtra(EXTRA_ARGS) : null;
        if (extra != null && !extra.trim().isEmpty()) {
            args.addAll(Arrays.asList(extra.trim().split("\\s+")));
        }
        return args.toArray(new String[0]);
    }

    /** The engine reads shaders and fonts as plain files: copied out of the APK once per installed build. */
    private void unpackRuntimeAssets() throws IOException, PackageManager.NameNotFoundException {
        File runtime = PtPaths.runtime(this);
        File stamp = new File(runtime, ".stamp");
        String build = Long.toString(getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime);
        if (stamp.isFile() && build.equals(new String(Files.readAllBytes(stamp.toPath()), StandardCharsets.UTF_8))) {
            return;
        }
        AssetManager assets = getAssets();
        byte[] buffer = new byte[1 << 16];
        for (String folder : new String[] {"shaders", "fonts"}) {
            File target = new File(runtime, folder);
            target.mkdirs();
            String[] names = assets.list(folder);
            if (names == null) continue;
            for (String name : names) {
                try (InputStream in = assets.open(folder + "/" + name);
                     OutputStream out = new FileOutputStream(new File(target, name))) {
                    int read;
                    while ((read = in.read(buffer)) > 0) out.write(buffer, 0, read);
                }
            }
        }
        Files.write(stamp.toPath(), build.getBytes(StandardCharsets.UTF_8));
    }
}
