package com.pixatrip1984.germanfather.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.pixatrip1984.germanfather.MainActivity

class AndroidAlarmPlatform(
    private val context: Context,
) : AlarmPlatform {
    private val alarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun canScheduleExactAlarms(): Boolean =
        alarmManager.canScheduleExactAlarms()

    override fun setAlarmClock(request: AlarmRequest) {
        val delivery = deliveryPendingIntent(request, PendingIntent.FLAG_UPDATE_CURRENT)
        val showIntent = PendingIntent.getActivity(
            context,
            SHOW_INTENT_REQUEST_CODE,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(request.triggerEpochMillis, showIntent),
            delivery,
        )
    }

    override fun cancel(occurrenceId: String) {
        val intent = baseDeliveryIntent(occurrenceId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DELIVERY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun deliveryPendingIntent(request: AlarmRequest, modeFlag: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            DELIVERY_REQUEST_CODE,
            baseDeliveryIntent(request.occurrenceId)
                .putExtra(AlarmIntents.EXTRA_OCCURRENCE_ID, request.occurrenceId)
                .putExtra(AlarmIntents.EXTRA_TRIGGER_EPOCH_MILLIS, request.triggerEpochMillis)
                .putExtra(AlarmIntents.EXTRA_TASK_ID, request.taskId)
                .putExtra(AlarmIntents.EXTRA_VISUAL, request.visual),
            modeFlag or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun baseDeliveryIntent(occurrenceId: String): Intent =
        Intent(context, AlarmDeliveryReceiver::class.java)
            .setAction(AlarmIntents.ACTION_DELIVER)
            .setData(
                Uri.Builder()
                    .scheme("germanfather")
                    .authority("alarm")
                    .appendPath(occurrenceId)
                    .build(),
            )

    private companion object {
        const val DELIVERY_REQUEST_CODE = 41001
        const val SHOW_INTENT_REQUEST_CODE = 41002
    }
}
