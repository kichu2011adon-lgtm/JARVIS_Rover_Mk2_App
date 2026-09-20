J.A.R.V.I.S. Rover Mk2 — Cloud APK Build

This project is designed to build on GitHub Actions, so Android Studio is not required on your laptop.

1. Create a GitHub account.
2. Create a NEW PUBLIC repository named JARVIS_Rover_Mk2_App.
3. Upload all files/folders from this project, preserving paths.
4. Open the repository's Actions tab.
5. Select "Build JARVIS Rover APK".
6. Click "Run workflow".
7. When it finishes, open the workflow run and download the artifact named:
   JARVIS-Rover-Mk2-debug
8. Extract the artifact on your phone. Install app-debug.apk.

BLE UUIDs:
Service 7e400001-b5a3-f393-e0a9-e50e24dcca9e
Write   7e400002-b5a3-f393-e0a9-e50e24dcca9e
Notify  7e400003-b5a3-f393-e0a9-e50e24dcca9e

Commands:
MANUAL, AUTO, LINE, F, B, L, R, X, STOP, HORN, SPEED:0..100

IMPORTANT:
The ESP32 must run matching BLE firmware. The Android app cannot control the rover until the ESP32 advertises the matching service/characteristic.
