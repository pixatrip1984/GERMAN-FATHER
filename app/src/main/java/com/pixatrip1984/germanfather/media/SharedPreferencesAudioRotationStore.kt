package com.pixatrip1984.germanfather.media

import android.content.Context

class SharedPreferencesAudioRotationStore(context: Context) : AudioRotationIndexStore {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun currentIndex(): Int = preferences.getInt(KEY_NEXT_INDEX, 0)

    override fun saveNextIndex(index: Int) {
        preferences.edit().putInt(KEY_NEXT_INDEX, index).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "alarm_audio_rotation"
        const val KEY_NEXT_INDEX = "next_index"
    }
}
