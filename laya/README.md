# GERMAN FATHER — LAYA package

This branch contains the LAYA authoring package only. It intentionally does not modify the main product baseline.

Package:
- packageId: german-father-android-v1
- base branch: main
- expected base HEAD: e84a81508a1bab57d421d7881e32022c5b0398d5
- repoRoot: C:\dev\GERMAN-FATHER
- entry file: laya\laya-entry.json
- start mode: new
- validation mode: commands

Why this is on a separate branch:
The LAYA contract requires a real observed headSha for the product base. Committing the package itself to main would move main and make that value stale. Keeping this package branch based on the observed main HEAD lets the package live in the same repository while main remains the exact execution baseline.

Local preparation:
1. git fetch origin
2. git switch laya/german-father-android-v1-package
3. Confirm the working tree is clean.
4. Launch your existing LAYA executable and provide C:\dev\GERMAN-FATHER as the repo and C:\dev\GERMAN-FATHER\laya\laya-entry.json as the package path when prompted.

The exact LAYA executable command is intentionally not invented here; use the launcher you already use.

Product scope:
Android 15 only, sideloaded APK, exact alarm-clock scheduling, full-screen lock-screen alarm, supplied image per task, persistent 1→2→3 MP3 rotation, no in-app dismiss/snooze, missed events are skipped, reboot restores only future events, weekends are silent, and schedule.json is the only editable timetable/task definition.

Acceptance:
Automatic checks cover schedule structure, media references, unit tests, lint, APK build and packaged assets. Physical Android 15 checks remain human because lock-screen behavior, speaker output and reboot behavior require the real phone and user-granted system access.
