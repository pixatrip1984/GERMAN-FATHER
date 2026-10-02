package com.pixatrip1984.germanfather.alarm

import com.pixatrip1984.germanfather.schedule.AlertOccurrence

fun interface NextAlertSource {
    fun nextFutureAlert(): AlertOccurrence?
}

interface AlarmPlatform {
    fun canScheduleExactAlarms(): Boolean
    fun setAlarmClock(request: AlarmRequest)
    fun cancel(occurrenceId: String)
}

interface AlarmMetadataStore {
    fun current(): AlarmMetadata?
    fun save(metadata: AlarmMetadata)
    fun clear()
}
