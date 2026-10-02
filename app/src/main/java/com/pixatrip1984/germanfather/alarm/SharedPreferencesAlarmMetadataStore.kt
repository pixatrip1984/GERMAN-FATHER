package com.pixatrip1984.germanfather.alarm

import android.content.Context

class SharedPreferencesAlarmMetadataStore(context: Context) : AlarmMetadataStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun current(): AlarmMetadata? {
        val occurrenceId = preferences.getString(KEY_OCCURRENCE_ID, null) ?: return null
        if (!preferences.contains(KEY_TRIGGER_EPOCH_MILLIS)) return null
        return AlarmMetadata(
            occurrenceId = occurrenceId,
            triggerEpochMillis = preferences.getLong(KEY_TRIGGER_EPOCH_MILLIS, 0L),
        )
    }

    override fun save(metadata: AlarmMetadata) {
        preferences.edit()
            .putString(KEY_OCCURRENCE_ID, metadata.occurrenceId)
            .putLong(KEY_TRIGGER_EPOCH_MILLIS, metadata.triggerEpochMillis)
            .apply()
    }

    override fun clear() {
        preferences.edit()
            .remove(KEY_OCCURRENCE_ID)
            .remove(KEY_TRIGGER_EPOCH_MILLIS)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "alarm_scheduler"
        const val KEY_OCCURRENCE_ID = "occurrence_id"
        const val KEY_TRIGGER_EPOCH_MILLIS = "trigger_epoch_millis"
    }
}
