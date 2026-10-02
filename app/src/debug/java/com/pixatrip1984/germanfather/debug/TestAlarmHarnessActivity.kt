package com.pixatrip1984.germanfather.debug

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.pixatrip1984.germanfather.alarm.AndroidAlarmPlatform
import com.pixatrip1984.germanfather.alarm.AlarmUiRescheduleGate
import com.pixatrip1984.germanfather.alarm.SchedulerStatus
import com.pixatrip1984.germanfather.alarm.SharedPreferencesAlarmMetadataStore
import java.time.Clock
import java.time.ZoneId

class TestAlarmHarnessActivity : Activity() {
    private var scheduled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduled = savedInstanceState?.getBoolean(STATE_SCHEDULED) ?: false
        if (!scheduled) {
            scheduled = true
            scheduleAlarm()
        }
        finish()
    }

    private fun scheduleAlarm() {
        val requestedDelay = intent.getIntExtra(
            EXTRA_DELAY_SECONDS,
            DebugAlarmHarnessScheduler.DEFAULT_DELAY_SECONDS,
        )
        val delaySeconds = requestedDelay.coerceAtLeast(1)
        val taskId = intent.getStringExtra(EXTRA_TASK_ID)
            ?: DebugAlarmHarnessScheduler.TEST_TASK_ID
        val visual = intent.getStringExtra(EXTRA_VISUAL)
            ?: DebugAlarmHarnessScheduler.TEST_VISUAL

        val rescheduleGate = AlarmUiRescheduleGate(applicationContext)
        rescheduleGate.suppressNextUiResume()

        val status = DebugAlarmHarnessScheduler(
            platform = AndroidAlarmPlatform(applicationContext),
            metadataStore = SharedPreferencesAlarmMetadataStore(applicationContext),
            clock = Clock.systemDefaultZone(),
            zoneId = ZoneId.systemDefault(),
        ).schedule(
            delaySeconds = delaySeconds,
            taskId = taskId,
            visual = visual,
        )

        when (status) {
            is SchedulerStatus.Armed ->
                Log.i(TAG, "debug alarm armed for " + status.request.triggerEpochMillis)
            is SchedulerStatus.NotArmed -> {
                rescheduleGate.clear()
                Log.e(TAG, "debug alarm not armed: " + status.reason)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_SCHEDULED, scheduled)
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val EXTRA_DELAY_SECONDS = "delaySeconds"
        const val EXTRA_TASK_ID = "taskId"
        const val EXTRA_VISUAL = "visual"

        private const val STATE_SCHEDULED = "scheduled"
        private const val TAG = "TestAlarmHarness"
    }
}
