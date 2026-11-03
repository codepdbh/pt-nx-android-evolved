package io.github.codepdbh.ptevolved;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/** The game files are in the "pt" folder of the shared storage; settings, saves, logs and runtime files in private storage. */
final class PtPaths {
    static final String[] GAME_FILES = {"chunk1.psarc", "texture.qar", "pathid_list_ps4.bin"};

    private PtPaths() {}

    static File root(Context context) { return new File(context.getFilesDir(), "pt"); }
    static File game(Context context) { return new File(Environment.getExternalStorageDirectory(), "pt"); }
    /** What the engine calls the executable's folder: shaders/ and fonts/ unpacked from the APK. */
    static File runtime(Context context) { return new File(root(context), "runtime"); }

    static boolean gameFilesPresent(Context context) {
        File dir = game(context);
        for (String name : GAME_FILES) {
            File file = new File(dir, name);
            if (!file.isFile() || file.length() == 0) return false;
        }
        return true;
    }

    static void ensureFolders(Context context) {
        File root = root(context);
        for (String name : new String[] {"save", "cache", "logs", "mods", "runtime"}) {
            new File(root, name).mkdirs();
        }
    }
}
