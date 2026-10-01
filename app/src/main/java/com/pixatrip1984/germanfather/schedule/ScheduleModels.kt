package com.pixatrip1984.germanfather.schedule

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime

data class ScheduleConfig(
    val schemaVersion: Int,
    val timezoneMode: String,
    val missedAlarmPolicy: String,
    val audioRotation: List<String>,
    val tasks: Set<String>,
    val days: Map<DayOfWeek, DaySchedule>,
)

data class DaySchedule(
    val timeline: List<TimelineEntry>,
    val alerts: List<AlertEntry>,
)

data class TimelineEntry(
    val time: LocalTime,
    val taskId: String,
)

data class AlertEntry(
    val time: LocalTime,
    val taskId: String,
    val visual: String,
)

data class DisplayState(
    val taskId: String,
    val startedAt: ZonedDateTime?,
) {
    companion object {
        const val NO_SCHEDULE_TASK_ID = "SIN_HORARIO"
        fun noSchedule() = DisplayState(NO_SCHEDULE_TASK_ID, null)
    }
}

data class DisplayOccurrence(
    val taskId: String,
    val at: ZonedDateTime,
)

data class AlertOccurrence(
    val taskId: String,
    val visual: String,
    val at: ZonedDateTime,
)
