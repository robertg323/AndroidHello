# AndroidHello

A simple Android PDF reader built with Kotlin, Jetpack Compose, and Material3.

## Features

- Open PDF files from device storage via system file picker
- Page-by-page navigation with top menu bar (File > Open / Close)
- Bottom navigation bar with first/previous/next/last page buttons
- Editable page number field for direct page jumping
- Keyboard navigation: PgUp, PgDown, Home, End
- Vertical scrolling for tall pages
- Uses Android's built-in PdfRenderer (no external dependencies)

## Setup

### Prerequisites

1. **Install Android Studio** from https://developer.android.com/studio
   - It bundles JDK 17, Android SDK, Gradle, and the emulator
2. During first-run setup, accept SDK licenses and install the **API 35** platform

### Generate Gradle Wrapper

After installing Android Studio, open a terminal in the project root and run:

```bash
# On Windows (if Gradle is on PATH via Android Studio):
gradle wrapper --gradle-version 8.11.1

# Or simply open the project in Android Studio - it will generate the wrapper automatically
```

This creates `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar`.

### Build

```bash
# Windows
gradlew.bat assembleDebug

# Mac/Linux
./gradlew assembleDebug
```

The APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

### Run

- **Emulator**: Create an AVD in Android Studio with API 35, then run the app
- **Physical device**: Enable Developer Options and USB Debugging, connect via USB, then run `gradlew.bat installDebug`

## Tech Stack

| Component | Version |
|---|---|
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.1.0 |
| Gradle | 8.11.1 |
| Compose BOM | 2024.12.01 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 |
