package com.pixatrip1984.germanfather.ui.alarm

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageView
import com.pixatrip1984.germanfather.alarm.AlarmIntents
import com.pixatrip1984.germanfather.alarm.AlarmPlaybackStateStore
import com.pixatrip1984.germanfather.alarm.AlarmUiSignals

class AlarmActivity : Activity() {
    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AlarmUiSignals.ACTION_FINISHED) {
                Log.i(TAG, "AlarmActivity finish signal received")
                finishAndRemoveTask()
            }
        }
    }
    private var receiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val occurrenceId = intent.getStringExtra(AlarmIntents.EXTRA_OCCURRENCE_ID)
        val visual = intent.getStringExtra(AlarmIntents.EXTRA_VISUAL)
        Log.i(TAG, "AlarmActivity onCreate occurrenceId=$occurrenceId visual=$visual")

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!AlarmPlaybackStateStore(this).isActive(occurrenceId)) {
            Log.w(TAG, "AlarmActivity rejected inactive occurrenceId=$occurrenceId")
            finishAndRemoveTask()
            return
        }
        if (visual.isNullOrBlank()) {
            Log.e(TAG, "AlarmActivity missing visual occurrenceId=$occurrenceId")
            finishAndRemoveTask()
            return
        }

        registerReceiver(
            finishReceiver,
            IntentFilter(AlarmUiSignals.ACTION_FINISHED),
            Context.RECEIVER_NOT_EXPORTED,
        )
        receiverRegistered = true

        try {
            val imageView = ImageView(this).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            val bitmap = assets.open(visual).use { input ->
                Log.i(TAG, "visual asset opened visual=$visual")
                BitmapFactory.decodeStream(input)
            }
            if (bitmap == null) {
                Log.e(TAG, "bitmap decode returned null visual=$visual")
                finishAndRemoveTask()
                return
            }
            Log.i(TAG, "bitmap decoded visual=$visual")

            imageView.setImageBitmap(bitmap)
            setContentView(imageView)
            Log.i(TAG, "content view installed visual=$visual")

            imageView.post {
                val controller = imageView.windowInsetsController
                if (controller == null) {
                    Log.w(TAG, "WindowInsetsController unavailable after view attach")
                    return@post
                }
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(
                    WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars(),
                )
                Log.i(TAG, "system bars hidden after content view attach")
            }
        } catch (error: Exception) {
            Log.e(TAG, "AlarmActivity visual render failed visual=$visual", error)
            finishAndRemoveTask()
            return
        }

        if (!AlarmPlaybackStateStore(this).isActive(occurrenceId)) {
            Log.i(TAG, "AlarmActivity playback ended during render")
            finishAndRemoveTask()
        }
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(finishReceiver)
            receiverRegistered = false
        }
        super.onDestroy()
    }

    private companion object {
        const val TAG = "AlarmActivity"
    }
}
