package io.github.codepdbh.ptevolved;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * First screen: copies the player's own game files into private storage through the system file picker (no storage
 * permission), then starts the game. Skipped once the files are in place.
 */
public class ImportActivity extends Activity {
    private static final int REQUEST_PICK = 1;

    private TextView status;
    private ProgressBar progress;
    private Button pick;
    private Button cancel;
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private Thread worker;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        PtPaths.ensureFolders(this);
        if (PtPaths.gameFilesPresent(this) || (BuildConfig.DEBUG && getIntent().getBooleanExtra(PtActivity.EXTRA_SELFTEST, false))) {
            startGame();
            return;
        }
        buildScreen();
    }

    private void buildScreen() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.BLACK);
        layout.setFitsSystemWindows(true);
        layout.setPadding(64, 64, 64, 64);

        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setText(describeMissing());
        layout.addView(status);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(1000);
        progress.setVisibility(View.INVISIBLE);
        layout.addView(progress, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        pick = new Button(this);
        pick.setText("Seleccionar archivos");
        pick.setOnClickListener(v -> openPicker());
        layout.addView(pick);

        cancel = new Button(this);
        cancel.setText("Cancelar");
        cancel.setVisibility(View.GONE);
        cancel.setOnClickListener(v -> cancelled.set(true));
        layout.addView(cancel);

        setContentView(layout);
    }

    private String describeMissing() {
        StringBuilder text = new StringBuilder("P.T. necesita los archivos de tu propia copia del juego (CUSA01127).\nFaltan:\n");
        File dir = PtPaths.game(this);
        for (String name : PtPaths.GAME_FILES) {
            if (!new File(dir, name).isFile()) text.append(name).append('\n');
        }
        return text.toString();
    }

    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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
        pick.setEnabled(false);
        cancel.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        worker = new Thread(() -> {
            String error = null;
            try {
                for (Uri uri : uris) copyOne(uri);
            } catch (IOException e) {
                error = e.getMessage();
            }
            final String message = error;
            runOnUiThread(() -> copyFinished(message));
        }, "pt-import");
        worker.start();
    }

    private void copyFinished(String error) {
        pick.setEnabled(true);
        cancel.setVisibility(View.GONE);
        progress.setVisibility(View.INVISIBLE);
        if (error == null && PtPaths.gameFilesPresent(this)) {
            startGame();
            return;
        }
        status.setText((error != null ? error + "\n\n" : "") + describeMissing());
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
        File target = new File(dir, name);
        if (!part.renameTo(target)) {
            part.delete();
            throw new IOException("No se puede guardar " + name);
        }
    }

    /** Only chunk1.psarc has a fixed signature known here; the engine validates the rest when it mounts them. */
    private static void checkSignature(String name, byte[] data, int length) throws IOException {
        if (name.equals("chunk1.psarc")
                && (length < 4 || data[0] != 'P' || data[1] != 'S' || data[2] != 'A' || data[3] != 'R')) {
            throw new IOException("chunk1.psarc no es un archivo PSARC válido");
        }
    }

    private void startGame() {
        Intent intent = new Intent(this, PtActivity.class);
        if (BuildConfig.DEBUG && getIntent().hasExtra(PtActivity.EXTRA_ARGS)) {
            intent.putExtra(PtActivity.EXTRA_ARGS, getIntent().getStringExtra(PtActivity.EXTRA_ARGS));
        }
        if (BuildConfig.DEBUG) {
            intent.putExtra(PtActivity.EXTRA_ENV, getIntent().getStringExtra(PtActivity.EXTRA_ENV));
            intent.putExtra(PtActivity.EXTRA_SELFTEST, getIntent().getBooleanExtra(PtActivity.EXTRA_SELFTEST, false));
        }
        startActivity(intent);
        finish();
    }
}
