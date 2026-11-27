package dev.jayden.wallpapergrabber;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FloatingCaptureService extends Service {
    private WindowManager windowManager;
    private View bubble;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        TextView button = new TextView(this);
        button.setText("📷");
        button.setTextSize(24);
        button.setGravity(Gravity.CENTER);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundColor(0xDD252525);
        button.setPadding(18, 12, 18, 12);
        button.setOnClickListener(v -> WallpaperSaver.save(this,
            android.app.WallpaperManager.FLAG_SYSTEM));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            android.os.Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 24;
        params.y = 220;

        bubble = button;
        windowManager.addView(bubble, params);
        button.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY;
            private float downX, downY;
            private boolean moved;
            @Override public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = params.x; startY = params.y;
                    downX = event.getRawX(); downY = event.getRawY(); moved = false;
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int dx = (int)(downX - event.getRawX());
                    int dy = (int)(event.getRawY() - downY);
                    if (Math.abs(dx) + Math.abs(dy) > 8) moved = true;
                    params.x = Math.max(0, startX + dx);
                    params.y = Math.max(0, startY + dy);
                    windowManager.updateViewLayout(bubble, params);
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!moved) WallpaperSaver.save(FloatingCaptureService.this,
                        android.app.WallpaperManager.FLAG_SYSTEM);
                    return true;
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        if (windowManager != null && bubble != null) windowManager.removeView(bubble);
        bubble = null;
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
