package com.pixatrip1984.germanfather.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmDeliveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmIntents.ACTION_DELIVER) return

        val occurrenceId = intent.getStringExtra(AlarmIntents.EXTRA_OCCURRENCE_ID)
        val epoch = intent
            .takeIf { it.hasExtra(AlarmIntents.EXTRA_TRIGGER_EPOCH_MILLIS) }
            ?.getLongExtra(AlarmIntents.EXTRA_TRIGGER_EPOCH_MILLIS, 0L)

        val accepted = AlarmDeliveryGuard(SharedPreferencesAlarmMetadataStore(context))
            .consumeIfCurrent(occurrenceId, epoch)
        if (!accepted) return

        val serviceIntent = Intent(context, AlarmPlaybackService::class.java)
            .setAction(AlarmPlaybackService.ACTION_START)
            .putExtra(AlarmIntents.EXTRA_OCCURRENCE_ID, occurrenceId)
            .putExtra(
                AlarmIntents.EXTRA_TASK_ID,
                intent.getStringExtra(AlarmIntents.EXTRA_TASK_ID),
            )
            .putExtra(
                AlarmIntents.EXTRA_VISUAL,
                intent.getStringExtra(AlarmIntents.EXTRA_VISUAL),
            )

        try {
            context.startForegroundService(serviceIntent)
        } catch (error: Exception) {
            Log.e(TAG, "failed to start alarm foreground service occurrence=" + occurrenceId, error)
            AlarmRuntime.coordinator(context).reschedule()
        }
    }

    private companion object {
        const val TAG = "AlarmDeliveryReceiver"
    }
}
