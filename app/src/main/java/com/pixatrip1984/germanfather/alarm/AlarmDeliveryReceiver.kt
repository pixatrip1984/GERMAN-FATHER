package com.pixatrip1984.germanfather.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmDeliveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmIntents.ACTION_DELIVER) return

        val occurrenceId = intent.getStringExtra(AlarmIntents.EXTRA_OCCURRENCE_ID)
        val epoch = intent
            .takeIf { it.hasExtra(AlarmIntents.EXTRA_TRIGGER_EPOCH_MILLIS) }
            ?.getLongExtra(AlarmIntents.EXTRA_TRIGGER_EPOCH_MILLIS, 0L)

        AlarmDeliveryGuard(SharedPreferencesAlarmMetadataStore(context))
            .consumeIfCurrent(occurrenceId, epoch)

        // Audio and full-screen delivery are intentionally implemented by the next task.
    }
}
