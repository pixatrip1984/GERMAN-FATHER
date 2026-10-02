package com.pixatrip1984.germanfather.alarm

import android.app.ActivityOptions
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.pixatrip1984.germanfather.media.AudioRotation
import com.pixatrip1984.germanfather.media.SharedPreferencesAudioRotationStore
import com.pixatrip1984.germanfather.schedule.AndroidScheduleLoader
import com.pixatrip1984.germanfather.ui.alarm.AlarmActivity

class AlarmPlaybackService : Service() {
    private var player: MediaPlayer? = null
    private var activeOccurrenceId: String? = null
    private var finishing = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != ACTION_START) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        if (player != null || activeOccurrenceId != null) {
            return START_NOT_STICKY
        }

        val occurrenceId = intent.getStringExtra(AlarmIntents.EXTRA_OCCURRENCE_ID)
        val taskId = intent.getStringExtra(AlarmIntents.EXTRA_TASK_ID)
        val visual = intent.getStringExtra(AlarmIntents.EXTRA_VISUAL)
        if (occurrenceId.isNullOrBlank() || taskId.isNullOrBlank() || visual.isNullOrBlank()) {
            Log.e(TAG, "alarm playback rejected: missing occurrence/task/visual metadata")
            finishAlarm("missing delivery metadata", null)
            return START_NOT_STICKY
        }
        activeOccurrenceId = occurrenceId

        try {
            AlarmPlaybackStateStore(this).markActive(occurrenceId)
            createNotificationChannel()
            val notification = buildAlarmNotification(occurrenceId, taskId, visual)
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
            startPlayback(occurrenceId)
            maybeLaunchAlarmActivity(fullScreenIntentFor(occurrenceId, visual))
            AlarmWakeLock.release()
        } catch (error: Exception) {
            Log.e(TAG, "alarm playback failed for occurrence=" + occurrenceId, error)
            AlarmWakeLock.release()
            finishAlarm("startup failure", error)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun startPlayback(occurrenceId: String) {
        val schedule = AndroidScheduleLoader.load(assets)
        val rotation = AudioRotation(SharedPreferencesAudioRotationStore(this))
        val selection = rotation.current(schedule.audioRotation)

        val mediaPlayer = MediaPlayer()
        player = mediaPlayer
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        mediaPlayer.setOnCompletionListener {
            finishAlarm("completed", null)
        }
        mediaPlayer.setOnErrorListener { _, what, extra ->
            val failure = IllegalStateException(
                "MediaPlayer error what=" + what +
                    " extra=" + extra +
                    " occurrence=" + occurrenceId +
                    " asset=" + selection.assetName,
            )
            Log.e(TAG, failure.message, failure)
            finishAlarm("media player error", failure)
            true
        }

        assets.openFd(selection.assetName).use { descriptor ->
            mediaPlayer.setDataSource(
                descriptor.fileDescriptor,
                descriptor.startOffset,
                descriptor.length,
            )
        }
        mediaPlayer.prepare()
        mediaPlayer.start()
        rotation.advanceAfterPlaybackStarted(selection, schedule.audioRotation.size)
    }

    private fun buildAlarmNotification(
        occurrenceId: String,
        taskId: String,
        visual: String,
    ): Notification {
        val fullScreenIntent = fullScreenIntentFor(occurrenceId, visual)

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(taskId)
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(Notification.PRIORITY_MAX)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenIntent, true)
            .build()
    }

    private fun fullScreenIntentFor(
        occurrenceId: String,
        visual: String,
    ): PendingIntent =
        PendingIntent.getActivity(
            this,
            FULL_SCREEN_REQUEST_CODE,
            Intent(this, AlarmActivity::class.java)
                .putExtra(AlarmIntents.EXTRA_OCCURRENCE_ID, occurrenceId)
                .putExtra(AlarmIntents.EXTRA_VISUAL, visual)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP,
                ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun maybeLaunchAlarmActivity(fullScreenIntent: PendingIntent) {
        val notificationManager =
            requireNotNull(getSystemService(NotificationManager::class.java))
        if (!notificationManager.canUseFullScreenIntent()) {
            Log.w(TAG, "full-screen alarm access unavailable; notification fallback only")
            return
        }

        val keyguardManager =
            requireNotNull(getSystemService(KeyguardManager::class.java))
        val powerManager =
            requireNotNull(getSystemService(PowerManager::class.java))
        if (!keyguardManager.isKeyguardLocked && powerManager.isInteractive) {
            return
        }

        val options = ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(
                ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED,
            )
        runCatching {
            fullScreenIntent.send(
                this,
                0,
                null,
                null,
                null,
                null,
                options.toBundle(),
            )
        }.onFailure {
            Log.e(TAG, "direct locked-screen alarm activity launch failed", it)
        }
    }

    private fun createNotificationChannel() {
        val manager = requireNotNull(getSystemService(NotificationManager::class.java))
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarmas",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alarmas programadas"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun finishAlarm(reason: String, error: Throwable?) {
        if (finishing) return
        finishing = true

        if (error == null) {
            Log.i(TAG, "alarm playback finished: " + reason + " occurrence=" + activeOccurrenceId)
        } else {
            Log.e(TAG, "alarm playback cleanup: " + reason + " occurrence=" + activeOccurrenceId, error)
        }

        player?.runCatching {
            if (isPlaying) stop()
        }
        player?.release()
        player = null

        runCatching { AlarmPlaybackStateStore(this).clear() }
            .onFailure { Log.e(TAG, "failed to clear active playback state", it) }
        runCatching {
            sendBroadcast(
                Intent(AlarmUiSignals.ACTION_FINISHED)
                    .setPackage(packageName),
            )
        }.onFailure { Log.e(TAG, "failed to signal alarm UI completion", it) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        runCatching { AlarmRuntime.coordinator(this).reschedule() }
            .onFailure { Log.e(TAG, "failed to schedule next alarm", it) }
        stopSelf()
    }

    companion object {
        const val ACTION_START =
            "com.pixatrip1984.germanfather.action.START_ALARM_PLAYBACK"
        private const val CHANNEL_ID = "alarm_playback"
        private const val NOTIFICATION_ID = 7001
        private const val FULL_SCREEN_REQUEST_CODE = 7002
        private const val TAG = "AlarmPlaybackService"
    }
}
