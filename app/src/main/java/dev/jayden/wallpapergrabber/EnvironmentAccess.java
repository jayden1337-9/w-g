package dev.jayden.wallpapergrabber;

import android.content.Context;
import android.os.Environment;

final class EnvironmentAccess {
    private EnvironmentAccess() {}
    static boolean hasAllFiles(Context context) {
        return Environment.isExternalStorageManager();
    }
}
