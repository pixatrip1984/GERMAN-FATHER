package com.pixatrip1984.germanfather.ui.alarm

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import com.pixatrip1984.germanfather.alarm.AlarmIntents
import com.pixatrip1984.germanfather.alarm.AlarmPlaybackLifecycle
import com.pixatrip1984.germanfather.alarm.AlarmUiSignals

class AlarmActivity : Activity() {
    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AlarmUiSignals.ACTION_FINISHED) {
                finishAndRemoveTask()
            }
        }
    }
    private var receiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.insetsController?.hide(
            WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars(),
        )

        val occurrenceId = intent.getStringExtra(AlarmIntents.EXTRA_OCCURRENCE_ID)
        if (!AlarmPlaybackLifecycle.isActive(occurrenceId)) {
            finishAndRemoveTask()
            return
        }

        val visual = intent.getStringExtra(AlarmIntents.EXTRA_VISUAL)
        if (visual.isNullOrBlank()) {
            finishAndRemoveTask()
            return
        }

        val bitmap = runCatching {
            assets.open(visual).use { input -> BitmapFactory.decodeStream(input) }
        }.getOrNull()
        if (bitmap == null) {
            finishAndRemoveTask()
            return
        }

        setContentView(
            ImageView(this).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(bitmap)
            },
        )

        registerReceiver(
            finishReceiver,
            IntentFilter(AlarmUiSignals.ACTION_FINISHED),
            Context.RECEIVER_NOT_EXPORTED,
        )
        receiverRegistered = true
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(finishReceiver)
            receiverRegistered = false
        }
        super.onDestroy()
    }
}
