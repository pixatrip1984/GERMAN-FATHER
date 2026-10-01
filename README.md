# GERMAN FATHER

Initial Android scaffold for the Android 15-only sideloaded GERMAN FATHER application.

## Toolchain

- JDK 17
- Android SDK Platform 35
- Android Gradle Plugin 8.7.3
- Gradle 8.9 (committed wrapper)
- Kotlin 2.0.21
- Jetpack Compose

The application uses namespace and application id `com.pixatrip1984.germanfather`, with `minSdk = 35`, `targetSdk = 35`, and `compileSdk = 35`.

The repository-root `AUDIOS/` and `VISUAL/` directories are authoritative source materials and are intentionally not copied into the app in this scaffold task.

## Baseline gates

With JDK 17 and Android SDK 35 installed:

```text
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:lintDebug --no-daemon
./gradlew :app:assembleDebug --no-daemon
```

On Windows, use `gradlew.bat` instead of `./gradlew`.
