package com.pixatrip1984.germanfather.permissions

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager

data class AlarmCapabilities(
    val notificationsGranted: Boolean,
    val fullScreenIntentGranted: Boolean,
    val exactAlarmAvailable: Boolean,
)

object AlarmCapabilityReader {
    fun read(context: Context): AlarmCapabilities {
        val notificationsGranted =
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        val notificationManager =
            requireNotNull(context.getSystemService(NotificationManager::class.java))
        val alarmManager =
            requireNotNull(context.getSystemService(AlarmManager::class.java))

        return AlarmCapabilities(
            notificationsGranted = notificationsGranted,
            fullScreenIntentGranted = notificationManager.canUseFullScreenIntent(),
            exactAlarmAvailable = alarmManager.canScheduleExactAlarms(),
        )
    }
}
