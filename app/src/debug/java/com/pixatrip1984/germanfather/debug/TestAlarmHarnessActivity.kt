package com.pixatrip1984.germanfather.debug

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.pixatrip1984.germanfather.alarm.AndroidAlarmPlatform
import com.pixatrip1984.germanfather.alarm.SchedulerStatus
import com.pixatrip1984.germanfather.alarm.SharedPreferencesAlarmMetadataStore
import java.time.Clock
import java.time.ZoneId

class TestAlarmHarnessActivity : Activity() {
    private var scheduled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduled = savedInstanceState?.getBoolean(STATE_SCHEDULED) ?: false
    }

    override fun onResume() {
        super.onResume()
        if (scheduled) {
            finish()
            return
        }
        scheduled = true

        val requestedDelay = intent.getIntExtra(
            EXTRA_DELAY_SECONDS,
            DebugAlarmHarnessScheduler.DEFAULT_DELAY_SECONDS,
        )
        val delaySeconds = requestedDelay.coerceAtLeast(1)

        val status = DebugAlarmHarnessScheduler(
            platform = AndroidAlarmPlatform(applicationContext),
            metadataStore = SharedPreferencesAlarmMetadataStore(applicationContext),
            clock = Clock.systemDefaultZone(),
            zoneId = ZoneId.systemDefault(),
        ).schedule(delaySeconds)

        when (status) {
            is SchedulerStatus.Armed ->
                Log.i(TAG, "debug alarm armed for " + status.request.triggerEpochMillis)
            is SchedulerStatus.NotArmed ->
                Log.e(TAG, "debug alarm not armed: " + status.reason)
        }
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_SCHEDULED, scheduled)
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val EXTRA_DELAY_SECONDS = "delaySeconds"
        const val STATE_SCHEDULED = "scheduled"
        const val TAG = "TestAlarmHarness"
    }
}
