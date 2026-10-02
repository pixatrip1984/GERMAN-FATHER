package com.pixatrip1984.germanfather.alarm

import android.content.Context

class AlarmPlaybackStateStore(context: Context) {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun markActive(occurrenceId: String) {
        check(
            preferences.edit()
                .putString(KEY_ACTIVE_OCCURRENCE_ID, occurrenceId)
                .commit(),
        ) { "Unable to persist active alarm occurrence" }
    }

    fun clear() {
        check(
            preferences.edit()
                .remove(KEY_ACTIVE_OCCURRENCE_ID)
                .commit(),
        ) { "Unable to clear active alarm occurrence" }
    }

    fun isActive(occurrenceId: String?): Boolean =
        occurrenceId != null &&
            preferences.getString(KEY_ACTIVE_OCCURRENCE_ID, null) == occurrenceId

    private companion object {
        const val PREFERENCES_NAME = "alarm_playback_state"
        const val KEY_ACTIVE_OCCURRENCE_ID = "active_occurrence_id"
    }
}
