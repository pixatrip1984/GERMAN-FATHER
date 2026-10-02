package com.pixatrip1984.germanfather.alarm

import android.content.Context

class AlarmUiRescheduleGate(context: Context) {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun suppressNextUiResume() {
        preferences.edit().putBoolean(KEY_SUPPRESS_NEXT_UI_RESUME, true).apply()
    }

    fun consumeSuppression(): Boolean {
        if (!preferences.getBoolean(KEY_SUPPRESS_NEXT_UI_RESUME, false)) {
            return false
        }
        preferences.edit().remove(KEY_SUPPRESS_NEXT_UI_RESUME).apply()
        return true
    }

    fun clear() {
        preferences.edit().remove(KEY_SUPPRESS_NEXT_UI_RESUME).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "alarm_ui_reschedule_gate"
        const val KEY_SUPPRESS_NEXT_UI_RESUME = "suppress_next_ui_resume"
    }
}
