# Physical Android 15 acceptance

This checklist is supplementary to the HUMAN-FEEDBACK instructions embedded in laya-entry.json.

## Full-screen alarm
1. Build app-debug.apk through the declared Gradle gate.
2. Install it on the Android 15 target phone and launch GERMAN FATHER once.
3. Grant notifications. If the app reports full-screen alarm access missing, use its system-settings action and enable it.
4. Set Android Alarm volume to the desired high level. The application must not change system volume itself.
5. With adb available, run:
   adb shell am start -n com.pixatrip1984.germanfather/.debug.TestAlarmHarnessActivity --ei delaySeconds 60
6. Lock the phone immediately.
7. The real production delivery path must wake/present GIMNASIO.png over the lock screen, play exactly one supplied MP3, expose no in-app dismiss/snooze, and close itself when playback ends.
8. Repeat enough times to verify persistent round-robin playback.

## Reboot and missed alarm
1. Ensure a real future timetable alarm is armed.
2. Power the phone off before one scheduled occurrence and leave it off until that occurrence has passed.
3. Boot and unlock.
4. The elapsed occurrence must not play.
5. The next still-future scheduled occurrence must fire normally, demonstrating boot-time reconstruction.

A green build alone does not satisfy these physical checks.
