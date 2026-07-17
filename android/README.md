# Bling Tides Android + Wear OS

This folder contains a native Android project that wraps your existing tide web app and adds Wear OS support.

## What is included

- `app` (phone):
  - Loads your existing web chart UI in a `WebView` from `app/src/main/assets/web`.
  - Fetches NOAA tide prediction data natively.
  - Sends the latest tide points to the watch with Google Play Services Wear Data Layer (`/tides/latest`).
- `wear` (watch):
  - Receives synced tide data from phone.
  - Displays tide points in a watch-friendly list.

## Galaxy Watch 7 compatibility

Galaxy Watch 7 runs Wear OS, and this project targets Wear OS APIs:

- Wear module `minSdk = 30` (suitable for modern Wear OS devices).
- Uses official Data Layer API (`com.google.android.gms:play-services-wearable`).

## Run in Android Studio

1. Open the `android` folder in Android Studio.
2. Let Gradle sync.
3. Run `app` on an Android phone/emulator.
4. Run `wear` on a Wear OS device/emulator (Galaxy Watch 7 for real device testing).
5. In the phone app, tap **Sync Watch** to send data to the watch.

## Pairing requirements

- Phone and watch must be paired with Wear OS app.
- Both apps must be installed and signed from the same project build.
- Internet access is required on the phone app for NOAA API calls.

## Notes

- The web chart still uses Chart.js from CDN, so phone internet connection is required for chart rendering.
- The watch view currently shows a compact list of predicted heights. This can be upgraded to a watch chart or Tile/Complication next.
