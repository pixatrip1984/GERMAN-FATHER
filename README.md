# GERMAN FATHER

Android 15-only sideloaded alarm application.

## Toolchain

- JDK 17
- Android SDK Platform 35
- Android Gradle Plugin 8.7.3
- Gradle 8.9 (committed wrapper)
- Kotlin 2.0.21
- Jetpack Compose
- Android 15 physical device for final alarm acceptance

The application uses namespace and application id `com.pixatrip1984.germanfather`, with `minSdk = 35`, `targetSdk = 35`, and `compileSdk = 35`.

The repository-root `AUDIOS/` and `VISUAL/` directories are authoritative source materials. Gradle packages those files as APK assets without creating hand-maintained copies.

## Windows prerequisites

Install JDK 17 and Android SDK Platform 35. Set `JAVA_HOME` to the JDK 17 root and make sure `adb.exe` is available on `PATH`.

Verify the host:

```powershell
java -version
adb version
adb devices
```

The Gradle wrapper does not install Java. A `JAVA_HOME is not set` message is a host setup problem, not a project configuration problem.

## Automatic gates

From the repository root:

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon
.\gradlew.bat :app:lintDebug --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon
```

The debug APK is produced at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Install the debug APK

With one Android 15 device visible in `adb devices`:

```powershell
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.pixatrip1984.germanfather/.MainActivity
```

## Required Android 15 access

Grant notification permission, or grant it from the app UI:

```powershell
adb shell pm grant com.pixatrip1984.germanfather android.permission.POST_NOTIFICATIONS
adb shell dumpsys package com.pixatrip1984.germanfather | findstr POST_NOTIFICATIONS
```

Open the system page for full-screen alarm access:

```powershell
adb shell am start -a android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT -d package:com.pixatrip1984.germanfather
```

Enable full-screen alarm access for GERMAN FATHER on that page. The normal app screen also reports when this capability is missing and opens the same system settings page.

Set the phone's **Alarm volume** manually to the desired level. The app intentionally does not change system alarm volume.

## Physical alarm harness

The harness exists only in the debug variant. It is not present in the main/release manifest and there is no production UI button for it.

Schedule one test alarm 60 seconds in the future:

```powershell
adb shell am start -n com.pixatrip1984.germanfather/.debug.TestAlarmHarnessActivity --ei delaySeconds 60
```

`delaySeconds` defaults to 60 when omitted and is clamped to at least 1 second.

Immediately lock the phone before the fire time. The debug occurrence replaces the currently armed production occurrence through the same production scheduler metadata and AlarmManager path. Delivery then follows the normal pipeline:

```text
AlarmManager -> AlarmDeliveryReceiver -> AlarmPlaybackService
             -> CATEGORY_ALARM full-screen notification -> AlarmActivity
```

For acceptance, verify on the physical Android 15 device that:

- the screen wakes and presents `GIMNASIO.png` over the lock screen;
- exactly one supplied MP3 plays through alarm audio usage;
- there is no in-app dismiss or snooze control;
- the activity, notification, and foreground service disappear when playback completes;
- repeated harness runs advance audio deterministically through `1.mp3`, `2.mp3`, `3.mp3`, then `1.mp3`.

If earlier tests have already advanced the persistent audio index, clear app data before the four-alarm sequence, then relaunch the app and restore the required permissions:

```powershell
adb shell pm clear com.pixatrip1984.germanfather
adb shell am start -n com.pixatrip1984.germanfather/.MainActivity
```

## Reboot and missed-alarm acceptance

Use a weekday window with one production alert that can be intentionally missed and another later alert still in the future.

1. Launch GERMAN FATHER with notification/full-screen access enabled so the next production alarm is armed.
2. Power the device off before the chosen alert.
3. Keep it off until after that alert time.
4. Boot the phone.
5. Confirm the missed occurrence does not play after boot.
6. Leave the phone running and confirm the next still-future production alarm fires normally.

Useful inspection commands after boot:

```powershell
adb shell dumpsys package com.pixatrip1984.germanfather | findstr RECEIVE_BOOT_COMPLETED
adb shell dumpsys alarm | findstr com.pixatrip1984.germanfather
```

The reboot/missed-alarm and lock-screen behavior are physical-device acceptance checks; JVM tests cannot establish those observations.

## Alarm presentation regression acceptance

Build the exact checkout under test:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --no-daemon
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

The APK under test must be exactly:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Capture app/runtime diagnostics before each physical test:

```powershell
adb logcat -c
adb logcat AlarmDeliveryReceiver:I AlarmPlaybackService:I AlarmActivity:I MainActivity:I AndroidRuntime:E *:S
```

Unlocked-device test: keep the device unlocked, run the debug harness with a short delay, and confirm one MP3 starts, AlarmActivity presents the scheduled image, there is no dismiss/snooze control, and the alarm UI closes when playback completes.

```powershell
adb shell am start -n com.pixatrip1984.germanfather/.debug.TestAlarmHarnessActivity --ei delaySeconds 5
```

Locked-device test:

```powershell
adb shell am start -n com.pixatrip1984.germanfather/.debug.TestAlarmHarnessActivity --ei delaySeconds 60
```

Lock the device immediately and wait at least 75 seconds. Confirm the screen wakes, AlarmActivity appears over the lock screen, the scheduled image is visible, exactly one MP3 plays, and no AndroidRuntime fatal exception occurs.

Expected diagnostic milestones include: `AlarmDeliveryReceiver received`, `AlarmPlaybackService start`, `Full-screen notification posted`, `playback started`, `AlarmActivity onCreate`, `visual asset opened`, `bitmap decoded`, `content view installed`, and `playback completed`.

Android intentionally uses an expanded heads-up notification instead of launching a full-screen intent while another app is actively being used. The explicit unlocked visual path is therefore provided when GERMAN FATHER itself is the visible foreground activity; the lock-screen path continues to use the alarm full-screen intent.
