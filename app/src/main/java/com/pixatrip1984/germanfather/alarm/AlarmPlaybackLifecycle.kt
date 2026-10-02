package com.pixatrip1984.germanfather.alarm

object AlarmPlaybackLifecycle {
    @Volatile
    private var activeOccurrenceId: String? = null

    fun markActive(occurrenceId: String) {
        activeOccurrenceId = occurrenceId
    }

    fun clear() {
        activeOccurrenceId = null
    }

    fun isActive(occurrenceId: String?): Boolean =
        occurrenceId != null && activeOccurrenceId == occurrenceId
}
