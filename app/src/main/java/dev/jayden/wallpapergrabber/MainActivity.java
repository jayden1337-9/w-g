package dev.jayden.wallpapergrabber;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView status;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 36, 28, 28);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFFF5F5F5);

        TextView title = new TextView(this);
        title.setText("Wallpaper Grabber");
        title.setTextSize(27);
        title.setTextColor(0xFF171717);
        root.addView(title, matchWrap());

        TextView info = new TextView(this);
        info.setText("Save your current home or lock-screen wallpaper as a PNG. Android may restrict access to some live wallpapers.");
        info.setTextSize(15);
        info.setTextColor(0xFF444444);
        info.setPadding(0, 16, 0, 20);
        root.addView(info, matchWrap());

        status = new TextView(this);
        status.setTextSize(14);
        status.setTextColor(0xFF333333);
        root.addView(status, matchWrap());

        addButton(root, "Grant All files access", () -> {
            try {
                Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } catch (Exception e) {
                startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            }
        });

        addButton(root, "Allow floating button", () -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });

        addButton(root, "Start floating button", () -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Allow Display over other apps first.", Toast.LENGTH_LONG).show();
                return;
            }
            startService(new Intent(this, FloatingCaptureService.class));
            Toast.makeText(this, "Floating button started.", Toast.LENGTH_SHORT).show();
            finish();
        });

        addButton(root, "Save home wallpaper now", () -> WallpaperSaver.save(this, android.app.WallpaperManager.FLAG_SYSTEM));

        addButton(root, "Save lock-screen wallpaper", () -> WallpaperSaver.save(this, android.app.WallpaperManager.FLAG_LOCK));

        addButton(root, "Stop floating button", () -> {
            stopService(new Intent(this, FloatingCaptureService.class));
            Toast.makeText(this, "Floating button stopped.", Toast.LENGTH_SHORT).show();
        });

        setContentView(root);
        refreshStatus();
    }

    private ViewGroup.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void addButton(LinearLayout root, String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = 8;
        root.addView(b, p);
    }

    private void refreshStatus() {
        if (status == null) return;
        boolean files = Build.VERSION.SDK_INT < 30 || EnvironmentAccess.hasAllFiles(this);
        boolean overlay = Settings.canDrawOverlays(this);
        status.setText("All files access: " + (files ? "granted" : "not granted")
            + "\nFloating overlay: " + (overlay ? "allowed" : "not allowed")
            + "\n\nSaved images: Pictures/Wallpaper Grabber");
    }
}
