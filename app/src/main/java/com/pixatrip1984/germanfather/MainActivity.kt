package com.pixatrip1984.germanfather

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.pixatrip1984.germanfather.alarm.AlarmIntents
import com.pixatrip1984.germanfather.alarm.AlarmRuntime
import com.pixatrip1984.germanfather.alarm.AlarmUiSignals
import com.pixatrip1984.germanfather.alarm.AlarmUiRescheduleGate
import com.pixatrip1984.germanfather.permissions.AlarmCapabilityReader
import com.pixatrip1984.germanfather.schedule.AndroidScheduleLoader
import com.pixatrip1984.germanfather.schedule.ScheduleEngine
import com.pixatrip1984.germanfather.ui.alarm.AlarmActivity
import com.pixatrip1984.germanfather.ui.main.MainScreen
import com.pixatrip1984.germanfather.ui.main.MainScreenState
import com.pixatrip1984.germanfather.ui.main.MainScreenStateFactory
import java.time.Clock
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    private val screenState = mutableStateOf<MainScreenState?>(null)
    private val alarmPresentationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != AlarmUiSignals.ACTION_PRESENT) return
            val occurrenceId = intent.getStringExtra(AlarmIntents.EXTRA_OCCURRENCE_ID)
            val visual = intent.getStringExtra(AlarmIntents.EXTRA_VISUAL)
            Log.i(TAG, "foreground alarm presentation occurrenceId=$occurrenceId visual=$visual")
            startActivity(
                Intent(this@MainActivity, AlarmActivity::class.java)
                    .putExtra(AlarmIntents.EXTRA_OCCURRENCE_ID, occurrenceId)
                    .putExtra(AlarmIntents.EXTRA_VISUAL, visual),
            )
        }
    }
    private var alarmPresentationReceiverRegistered = false

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
                )
            }
        }
        refreshScreen()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(
            alarmPresentationReceiver,
            IntentFilter(AlarmUiSignals.ACTION_PRESENT),
            Context.RECEIVER_NOT_EXPORTED,
        )
        alarmPresentationReceiverRegistered = true
    }

    override fun onResume() {
        super.onResume()
        if (AlarmUiRescheduleGate(this).consumeSuppression()) {
            return
        }
        refreshScreen()
    }

    override fun onStop() {
        if (alarmPresentationReceiverRegistered) {
            unregisterReceiver(alarmPresentationReceiver)
            alarmPresentationReceiverRegistered = false
        }
        super.onStop()
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

    private fun openFullScreenIntentSettings() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Uri.parse("package:$packageName"),
            ),
        )
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}
