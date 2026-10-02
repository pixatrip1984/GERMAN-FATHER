package com.pixatrip1984.germanfather.alarm

import android.content.Context
import android.os.PowerManager

object AlarmWakeLock {
    @Volatile
    private var wakeLock: PowerManager.WakeLock? = null

    fun acquire(context: Context) {
        synchronized(this) {
            if (wakeLock?.isHeld == true) return
            val powerManager =
                requireNotNull(context.getSystemService(PowerManager::class.java))
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                context.packageName + ":alarm_handoff",
            ).apply {
                setReferenceCounted(false)
                acquire(HANDOFF_TIMEOUT_MILLIS)
            }
        }
    }

    fun release() {
        synchronized(this) {
            wakeLock?.let { lock ->
                if (lock.isHeld) {
                    lock.release()
                }
            }
            wakeLock = null
        }
    }

    private const val HANDOFF_TIMEOUT_MILLIS = 60_000L
}
