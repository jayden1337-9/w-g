# Wallpaper Grabber

A small Android app that saves the current system wallpaper as a PNG.

## Important Android 14 note

Android 14 restricts wallpaper reads. This app uses Android's documented `WallpaperManager.getDrawable(which)` API and requests **All files access** (`MANAGE_EXTERNAL_STORAGE`), which Android documents as the supported permission for reading wallpaper images on Android 14. Granting it is optional and controlled by Android Settings. If the device denies access, the app will show an error rather than silently saving a default image.

Live wallpapers may not expose their rendered image. Lock-screen wallpaper may also be unavailable when it shares a live wallpaper engine or has no separate image.

## Use

1. Install the debug APK from the GitHub Actions artifact.
2. Open Wallpaper Grabber.
3. Grant **All files access** and **Display over other apps** when prompted.
4. Tap **Start floating button**.
5. Tap the floating camera button to save the home wallpaper. Use the app's **Save lock-screen wallpaper** button for the lock screen.

Images are saved to **Pictures/Wallpaper Grabber**.

No Shizuku, root, network access, or account sign-in is used.

## Build

Open the project in Android Studio, or run `gradle assembleDebug` with Gradle 8.9 and JDK 17.
