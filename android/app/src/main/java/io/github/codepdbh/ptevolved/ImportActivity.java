package io.github.codepdbh.ptevolved;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The launcher: the state of the game files in the "pt" folder of the shared storage, a way to bring them there with the system file
 * picker, and the button that starts the game.
 */
public class ImportActivity extends Activity {
    private static final int REQUEST_PICK = 1;
    private static final int INK = Color.rgb(226, 226, 222);
    private static final int MUTED = Color.rgb(136, 136, 132);
    private static final int BLOOD = Color.rgb(164, 28, 28);
    private static final int GOOD = Color.rgb(132, 178, 132);

    private TextView status;
    private ProgressBar progress;
    private Button pick;
    private Button cancel;
    private Button play;
    private final TextView[] fileRows = new TextView[PtPaths.GAME_FILES.length];
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private Thread worker;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        PtPaths.ensureFolders(this);
        // test runs start at once (debug builds only)
        if (BuildConfig.DEBUG && (getIntent().getBooleanExtra(PtActivity.EXTRA_SELFTEST, false) || getIntent().hasExtra(PtActivity.EXTRA_ENV))) {
            startGame();
            return;
        }
        buildScreen();
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private GradientDrawable box(int fill, int stroke) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(dp(4));
        if (stroke != 0) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private Button button(String text, int fill, int stroke) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(INK);
        b.setAllCaps(false);
        b.setBackground(box(fill, stroke));
        b.setStateListAnimator(null);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        return b;
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(dp(40), dp(8), dp(40), dp(8));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setGravity(Gravity.CENTER);
        ImageView logo = new ImageView(this);
        logo.setImageResource(getApplicationInfo().icon);
        logo.setBackgroundColor(Color.WHITE);  // the logo is black on transparent
        logo.setPadding(dp(16), dp(16), dp(16), dp(16));  // its corners would be cut by the circle
        logo.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) { outline.setOval(0, 0, view.getWidth(), view.getHeight()); }
        });
        logo.setClipToOutline(true);
        brand.addView(logo, new LinearLayout.LayoutParams(dp(136), dp(136)));
        TextView tag = new TextView(this);
        tag.setText("SÚPER EXPERIMENTAL");
        tag.setTextColor(INK);
        tag.setTextSize(11);
        tag.setLetterSpacing(0.14f);
        tag.setTypeface(Typeface.DEFAULT_BOLD);
        tag.setBackground(box(BLOOD, 0));
        tag.setPadding(dp(10), dp(4), dp(10), dp(4));
        LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tagParams.topMargin = dp(16);
        brand.addView(tag, tagParams);
        TextView version = new TextView(this);
        version.setText("Versión " + BuildConfig.VERSION_NAME);
        version.setTextColor(MUTED);
        version.setTextSize(12);
        version.setPadding(0, dp(8), 0, 0);
        version.setGravity(Gravity.CENTER);
        brand.addView(version);
        root.addView(brand, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText("P.T. Android Evolved");
        title.setTextColor(INK);
        title.setTextSize(24);
        title.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        panel.addView(title);
        status = new TextView(this);
        status.setTextColor(MUTED);
        status.setTextSize(13);
        status.setPadding(0, dp(4), 0, dp(10));
        panel.addView(status);
        for (int i = 0; i < fileRows.length; ++i) {
            fileRows[i] = new TextView(this);
            fileRows[i].setTextSize(13);
            fileRows[i].setTypeface(Typeface.MONOSPACE);
            fileRows[i].setPadding(0, dp(2), 0, dp(2));
            panel.addView(fileRows[i]);
        }
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(1000);
        progress.setVisibility(View.INVISIBLE);
        panel.addView(progress, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(14)));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        pick = button("Seleccionar archivos", Color.rgb(28, 28, 30), Color.rgb(70, 70, 72));
        pick.setOnClickListener(v -> {
            if (Environment.isExternalStorageManager()) {
                openPicker();
            } else {
                startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
            }
        });
        actions.addView(pick, new LinearLayout.LayoutParams(0, dp(52), 1f));
        cancel = button("Cancelar", Color.rgb(28, 28, 30), Color.rgb(70, 70, 72));
        cancel.setVisibility(View.GONE);
        cancel.setOnClickListener(v -> cancelled.set(true));
        actions.addView(cancel, new LinearLayout.LayoutParams(0, dp(52), 1f));
        play = button("JUGAR", BLOOD, 0);
        play.setTextSize(17);
        play.setTypeface(Typeface.DEFAULT_BOLD);
        play.setLetterSpacing(0.2f);
        play.setOnClickListener(v -> startGame());
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(0, dp(52), 1f);
        playParams.leftMargin = dp(12);
        actions.addView(play, playParams);
        panel.addView(actions);
        root.addView(panel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f));

        // fitsSystemWindows replaces a view's own padding: it goes on a parent
        LinearLayout screen = new LinearLayout(this);
        screen.setGravity(Gravity.CENTER_VERTICAL);
        screen.setBackgroundColor(Color.rgb(8, 8, 9));
        screen.setFitsSystemWindows(true);
        screen.addView(root, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        setContentView(screen);
    }

    /** Also runs on the way back from the system's permission screen. */
    @Override
    protected void onResume() {
        super.onResume();
        if (status != null && (worker == null || !worker.isAlive())) {
            refresh(null);
        }
    }

    private static boolean looksValid(File file) {
        if (!file.isFile() || file.length() == 0) return false;
        if (!file.getName().equals("chunk1.psarc")) return true;
        byte[] head = new byte[4];
        try (InputStream in = new FileInputStream(file)) {
            return in.read(head) == 4 && head[0] == 'P' && head[1] == 'S' && head[2] == 'A' && head[3] == 'R';
        } catch (IOException e) {
            return false;
        }
    }

    private void refresh(String error) {
        boolean permission = Environment.isExternalStorageManager();
        boolean ready = permission;
        File dir = PtPaths.game(this);
        if (permission) dir.mkdirs();
        for (int i = 0; i < fileRows.length; ++i) {
            String name = PtPaths.GAME_FILES[i];
            File file = new File(dir, name);
            boolean present = permission && file.isFile();
            boolean valid = present && looksValid(file);
            ready = ready && valid;
            String state = !present ? "falta" : !valid ? "no válido" : file.length() >= (1 << 20) ? (file.length() >> 20) + " MB" : (file.length() >> 10) + " KB";
            fileRows[i].setText((valid ? "✓  " : "✗  ") + name + "  ·  " + state);
            fileRows[i].setTextColor(valid ? GOOD : MUTED);
        }
        play.setEnabled(ready);
        play.setAlpha(ready ? 1f : 0.35f);
        pick.setEnabled(true);
        pick.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.GONE);
        progress.setVisibility(View.INVISIBLE);
        pick.setText(permission ? "Seleccionar archivos" : "Dar permiso");
        String text = !permission ? "Activa \"Acceso a todos los archivos\" para leer la carpeta pt de la memoria interna."
                      : ready ? "Todo listo. Usa audífonos y apaga la luz."
                              : "Necesita los archivos de tu propia copia del juego (CUSA01127), en la carpeta pt.";
        status.setText(error != null ? error : text);
    }

    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:pt"));
        startActivityForResult(intent, REQUEST_PICK);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != REQUEST_PICK || result != RESULT_OK || data == null) return;
        List<Uri> uris = new ArrayList<>();
        ClipData clip = data.getClipData();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount(); ++i) uris.add(clip.getItemAt(i).getUri());
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }
        startCopy(uris);
    }

    private void startCopy(List<Uri> uris) {
        cancelled.set(false);
        pick.setVisibility(View.GONE);
        cancel.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        play.setEnabled(false);
        play.setAlpha(0.35f);
        worker = new Thread(() -> {
            String error = null;
            try {
                for (Uri uri : uris) copyOne(uri);
            } catch (IOException e) {
                error = e.getMessage();
            }
            final String message = error;
            runOnUiThread(() -> refresh(message));
        }, "pt-import");
        worker.start();
    }

    private void copyOne(Uri uri) throws IOException {
        String name = null;
        long size = -1;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                int sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeColumn >= 0 && !cursor.isNull(sizeColumn)) size = cursor.getLong(sizeColumn);
            }
        }
        if (name == null || !Arrays.asList(PtPaths.GAME_FILES).contains(name)) {
            throw new IOException("Archivo no reconocido: " + name);
        }
        File dir = PtPaths.game(this);
        File target = new File(dir, name);
        if (target.isFile() && target.length() == size) {
            return;  // already in place: most likely the very file picked in the pt folder
        }
        if (size > dir.getUsableSpace()) {
            throw new IOException("No hay espacio suficiente para " + name);
        }
        File part = new File(dir, name + ".part");
        final String shown = name;
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(part)) {
            if (in == null) throw new IOException("No se puede abrir " + name);
            byte[] buffer = new byte[1 << 20];
            long copied = 0;
            int read;
            boolean first = true;
            while ((read = in.read(buffer)) > 0) {
                if (cancelled.get()) throw new IOException("Importación cancelada");
                if (first) {
                    checkSignature(name, buffer, read);
                    first = false;
                }
                out.write(buffer, 0, read);
                copied += read;
                final long done = copied;
                final long total = size;
                runOnUiThread(() -> {
                    status.setText("Copiando " + shown + " (" + (done >> 20) + " MB)");
                    if (total > 0) progress.setProgress((int) (done * 1000 / total));
                });
            }
            if (size >= 0 && copied != size) throw new IOException(name + " está incompleto");
            if (copied == 0) throw new IOException(name + " está vacío");
        } catch (IOException e) {
            part.delete();
            throw e;
        }
        target.delete();
        if (!part.renameTo(target)) {
            part.delete();
            throw new IOException("No se puede guardar " + name);
        }
    }

    /** Only chunk1.psarc has a fixed signature at its start; the engine validates the rest when it mounts them. */
    private static void checkSignature(String name, byte[] data, int length) throws IOException {
        if (name.equals("chunk1.psarc")
                && (length < 4 || data[0] != 'P' || data[1] != 'S' || data[2] != 'A' || data[3] != 'R')) {
            throw new IOException("chunk1.psarc no es un archivo PSARC válido");
        }
    }

    private void startGame() {
        Intent intent = new Intent(this, PtActivity.class);
        if (BuildConfig.DEBUG) {
            intent.putExtra(PtActivity.EXTRA_ENV, getIntent().getStringExtra(PtActivity.EXTRA_ENV));
            intent.putExtra(PtActivity.EXTRA_SELFTEST, getIntent().getBooleanExtra(PtActivity.EXTRA_SELFTEST, false));
            if (getIntent().hasExtra(PtActivity.EXTRA_ARGS)) {
                intent.putExtra(PtActivity.EXTRA_ARGS, getIntent().getStringExtra(PtActivity.EXTRA_ARGS));
            }
        }
        startActivity(intent);
        finish();
    }
}
