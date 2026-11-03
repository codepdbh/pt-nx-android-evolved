package io.github.codepdbh.ptevolved;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** The license texts of everything in the APK, packed from third_party/android_notices (android/app/build.gradle). */
public class LicensesActivity extends Activity {
    private String[] names = new String[0];
    private boolean showingText;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        try {
            String[] listed = getAssets().list("licenses");
            if (listed != null) names = listed;
        } catch (IOException e) {
            names = new String[0];
        }
        showList();
    }

    private void showList() {
        showingText = false;
        ListView list = new ListView(this);
        list.setBackgroundColor(Color.rgb(8, 8, 9));
        list.setFitsSystemWindows(true);
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));
        list.setOnItemClickListener((parent, view, position, id) -> showText(names[position]));
        setContentView(list);
    }

    private void showText(String name) {
        TextView text = new TextView(this);
        text.setTextColor(Color.rgb(226, 226, 222));
        text.setTextSize(12);
        text.setTypeface(Typeface.MONOSPACE);
        int padding = Math.round(16 * getResources().getDisplayMetrics().density);
        text.setPadding(padding, padding, padding, padding);
        text.setText(read(name));
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(8, 8, 9));
        scroll.setFitsSystemWindows(true);
        scroll.addView(text);
        setContentView(scroll);
        showingText = true;
    }

    private String read(String name) {
        try (InputStream in = getAssets().open("licenses/" + name)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1 << 16];
            int count;
            while ((count = in.read(buffer)) > 0) out.write(buffer, 0, count);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "No se puede leer " + name;
        }
    }

    @Override
    public void onBackPressed() {
        if (showingText) {
            showList();
        } else {
            super.onBackPressed();
        }
    }
}
