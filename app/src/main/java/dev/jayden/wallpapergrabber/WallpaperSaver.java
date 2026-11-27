package dev.jayden.wallpapergrabber;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class WallpaperSaver {
    private WallpaperSaver() {}

    static void save(Context context, int which) {
        try {
            if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                Toast.makeText(context, "Grant All files access first.", Toast.LENGTH_LONG).show();
                return;
            }

            Drawable drawable = android.app.WallpaperManager.getInstance(context).getDrawable(which);
            if (drawable == null) {
                Toast.makeText(context, "No readable wallpaper image was returned. Live wallpapers may not be exportable.", Toast.LENGTH_LONG).show();
                return;
            }

            Bitmap bitmap;
            if (drawable instanceof BitmapDrawable && ((BitmapDrawable) drawable).getBitmap() != null) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            } else {
                int w = Math.max(1, drawable.getIntrinsicWidth());
                int h = Math.max(1, drawable.getIntrinsicHeight());
                if (w > 12000 || h > 12000) throw new IllegalStateException("Wallpaper dimensions are unexpectedly large.");
                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, w, h);
                drawable.draw(canvas);
            }

            String kind = which == android.app.WallpaperManager.FLAG_LOCK ? "lock" : "home";
            String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
            String name = "wallpaper-" + kind + "-" + stamp + ".png";

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Wallpaper Grabber");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new IllegalStateException("Could not create the output image.");
            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                if (out == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw new IllegalStateException("Could not write PNG image.");
                }
            } catch (Exception e) {
                context.getContentResolver().delete(uri, null, null);
                throw e;
            }
            ContentValues done = new ContentValues();
            done.put(MediaStore.Images.Media.IS_PENDING, 0);
            context.getContentResolver().update(uri, done, null, null);
            Toast.makeText(context, "Saved to Pictures/Wallpaper Grabber", Toast.LENGTH_LONG).show();
        } catch (SecurityException e) {
            Toast.makeText(context, "Android blocked wallpaper access. Check that All files access is enabled.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Could not save wallpaper: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
