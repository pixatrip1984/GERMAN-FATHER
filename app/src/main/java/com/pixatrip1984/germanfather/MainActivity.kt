package com.pixatrip1984.germanfather

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import com.pixatrip1984.germanfather.alarm.AlarmRuntime
import com.pixatrip1984.germanfather.alarm.AlarmUiRescheduleGate
import com.pixatrip1984.germanfather.permissions.AlarmCapabilityReader
import com.pixatrip1984.germanfather.schedule.AndroidScheduleLoader
import com.pixatrip1984.germanfather.schedule.ScheduleEngine
import com.pixatrip1984.germanfather.ui.main.MainScreen
import com.pixatrip1984.germanfather.ui.main.MainScreenState
import com.pixatrip1984.germanfather.ui.main.MainScreenStateFactory
import java.time.Clock
import java.time.Duration
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    private val screenState = mutableStateOf<MainScreenState?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            refreshScreen()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state = screenState.value
            if (state == null) {
                MaterialTheme {
                    Surface {
                        Text("GERMAN FATHER")
                    }
                }
            } else {
                MainScreen(
                    state = state,
                    onRequestNotifications = {
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    },
                    onOpenFullScreenSettings = ::openFullScreenIntentSettings,
                    showDebugTestButton = isDebuggable(),
                    onTestNextAlarm = ::testNextAlarm,
                )
            }
        }
        refreshScreen()
    }

    override fun onResume() {
        super.onResume()
        if (AlarmUiRescheduleGate(this).consumeSuppression()) {
            return
        }
        refreshScreen()
    }

    private fun refreshScreen() {
        val schedule = AndroidScheduleLoader.load(assets)
        val zoneId = ZoneId.systemDefault()
        val clock = Clock.system(zoneId)
        val engine = ScheduleEngine(
            schedule = schedule,
            clock = clock,
            zoneId = zoneId,
        )
        val schedulerStatus = AlarmRuntime.coordinator(this).reschedule()
        val capabilities = AlarmCapabilityReader.read(this)
        val now = clock.instant().atZone(zoneId)

        screenState.value = MainScreenStateFactory.create(
            schedule = schedule,
            engine = engine,
            now = now,
            capabilities = capabilities,
            schedulerStatus = schedulerStatus,
        )
    }

    private fun testNextAlarm() {
        if (!isDebuggable()) return

        val zoneId = ZoneId.systemDefault()
        val baseClock = Clock.system(zoneId)
        val schedule = AndroidScheduleLoader.load(assets)
        val nextAlert = ScheduleEngine(
            schedule = schedule,
            clock = Clock.offset(baseClock, Duration.ofMillis(1)),
            zoneId = zoneId,
        ).nextFutureAlert()

        if (nextAlert == null) {
            Log.e(TAG, "No future alert available for debug preview")
            return
        }

        startActivity(
            Intent().apply {
                component = ComponentName(
                    this@MainActivity,
                    packageName + ".debug.TestAlarmHarnessActivity",
                )
                putExtra(EXTRA_DELAY_SECONDS, DEBUG_PREVIEW_DELAY_SECONDS)
                putExtra(EXTRA_TASK_ID, nextAlert.taskId)
                putExtra(EXTRA_VISUAL, nextAlert.visual)
            },
        )
    }

    private fun isDebuggable(): Boolean =
        applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    private fun openFullScreenIntentSettings() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Uri.parse("package:$packageName"),
            ),
        )
    }

    private companion object {
        const val DEBUG_PREVIEW_DELAY_SECONDS = 5
        const val EXTRA_DELAY_SECONDS = "delaySeconds"
        const val EXTRA_TASK_ID = "taskId"
        const val EXTRA_VISUAL = "visual"
        const val TAG = "MainActivity"
    }
}
