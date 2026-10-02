package com.pixatrip1984.germanfather.schedule

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class ScheduleEngine(
    private val schedule: ScheduleConfig,
    private val clock: Clock,
    private val zoneId: ZoneId,
) {
    fun currentDisplayState(): DisplayState {
        val now = now()
        val day = schedule.days.getValue(now.dayOfWeek)
        val current = day.timeline.lastOrNull { entry ->
            !entry.time.isAfter(now.toLocalTime())
        } ?: return DisplayState.noSchedule()

        return DisplayState(
            taskId = current.taskId,
            startedAt = ZonedDateTime.of(now.toLocalDate(), current.time, zoneId),
        )
    }

    fun nextDisplayState(): DisplayOccurrence? {
        val now = now()
        return futureDates(now.toLocalDate()).firstNotNullOfOrNull { date ->
            schedule.days.getValue(date.dayOfWeek).timeline
                .asSequence()
                .map { entry -> DisplayOccurrence(entry.taskId, at(date, entry.time)) }
                .firstOrNull { occurrence -> occurrence.at.isAfter(now) }
        }
    }

    fun nextFutureAlert(): AlertOccurrence? {
        val now = now()
        return futureDates(now.toLocalDate()).firstNotNullOfOrNull { date ->
            schedule.days.getValue(date.dayOfWeek).alerts
                .asSequence()
                .map { entry -> AlertOccurrence(entry.taskId, entry.visual, at(date, entry.time)) }
                .firstOrNull { occurrence -> !occurrence.at.isBefore(now) }
        }
    }

    private fun now(): ZonedDateTime = clock.instant().atZone(zoneId)

    private fun at(date: LocalDate, time: java.time.LocalTime): ZonedDateTime =
        ZonedDateTime.of(date, time, zoneId)

    private fun futureDates(start: LocalDate): Sequence<LocalDate> = sequence {
        var date = start
        repeat(DAYS_IN_SEARCH_WINDOW) {
            yield(date)
            date = date.plusDays(1)
        }
    }

    private companion object {
        const val DAYS_IN_SEARCH_WINDOW = 8
    }
}
