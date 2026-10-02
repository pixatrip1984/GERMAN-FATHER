package com.pixatrip1984.germanfather.ui.main

import com.pixatrip1984.germanfather.alarm.SchedulerStatus
import com.pixatrip1984.germanfather.permissions.AlarmCapabilities
import com.pixatrip1984.germanfather.schedule.ScheduleConfig
import com.pixatrip1984.germanfather.schedule.ScheduleEngine
import java.time.DayOfWeek
import java.time.ZonedDateTime

data class TimelineRow(
    val time: String,
    val taskId: String,
)

data class MainScreenState(
    val timeline: List<TimelineRow>,
    val currentTask: String,
    val nextTask: String,
    val nextTaskTime: String?,
    val notificationsGranted: Boolean,
    val fullScreenIntentGranted: Boolean,
    val exactAlarmAvailable: Boolean,
    val schedulerArmed: Boolean,
)

object MainScreenStateFactory {
    fun create(
        schedule: ScheduleConfig,
        engine: ScheduleEngine,
        now: ZonedDateTime,
        capabilities: AlarmCapabilities,
        schedulerStatus: SchedulerStatus,
    ): MainScreenState {
        val daySchedule = schedule.days.getValue(now.dayOfWeek)
        val timeline = daySchedule.timeline.map { entry ->
            TimelineRow(
                time = entry.time.toString(),
                taskId = entry.taskId,
            )
        }
        val current = engine.currentDisplayState()
        val next = engine.nextDisplayState()

        return MainScreenState(
            timeline = timeline,
            currentTask = current.taskId,
            nextTask = next?.taskId ?: current.taskId,
            nextTaskTime = next?.at?.let { occurrence ->
                formatNextTime(occurrence, now.dayOfWeek)
            },
            notificationsGranted = capabilities.notificationsGranted,
            fullScreenIntentGranted = capabilities.fullScreenIntentGranted,
            exactAlarmAvailable = capabilities.exactAlarmAvailable,
            schedulerArmed = schedulerStatus is SchedulerStatus.Armed,
        )
    }

    private fun formatNextTime(
        occurrence: ZonedDateTime,
        today: DayOfWeek,
    ): String =
        if (occurrence.dayOfWeek == today) {
            occurrence.toLocalTime().toString()
        } else {
            occurrence.dayOfWeek.name + " " + occurrence.toLocalTime()
        }
}
