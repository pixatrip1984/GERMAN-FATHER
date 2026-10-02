package com.pixatrip1984.germanfather.ui.main

import com.pixatrip1984.germanfather.alarm.AlarmRequest
import com.pixatrip1984.germanfather.alarm.NotArmedReason
import com.pixatrip1984.germanfather.alarm.SchedulerStatus
import com.pixatrip1984.germanfather.permissions.AlarmCapabilities
import com.pixatrip1984.germanfather.schedule.DaySchedule
import com.pixatrip1984.germanfather.schedule.ScheduleConfig
import com.pixatrip1984.germanfather.schedule.ScheduleEngine
import com.pixatrip1984.germanfather.schedule.TimelineEntry
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenStateFactoryTest {
    private val zoneId = ZoneId.of("America/Monterrey")

    @Test
    fun weekendShowsNoScheduleAndStillFindsNextWeekdayTask() {
        val schedule = scheduleWithMondayTask()
        val clock = Clock.fixed(
            Instant.parse("2026-10-04T15:00:00Z"),
            zoneId,
        )
        val engine = ScheduleEngine(schedule, clock, zoneId)
        val now = clock.instant().atZone(zoneId)

        val state = MainScreenStateFactory.create(
            schedule = schedule,
            engine = engine,
            now = now,
            capabilities = AlarmCapabilities(
                notificationsGranted = true,
                fullScreenIntentGranted = true,
                exactAlarmAvailable = true,
            ),
            schedulerStatus = SchedulerStatus.Armed(
                AlarmRequest(
                    occurrenceId = "future",
                    triggerEpochMillis = 1L,
                    taskId = "NEXT",
                    visual = "NEXT.png",
                ),
            ),
        )

        assertTrue(state.timeline.isEmpty())
        assertEquals("SIN_HORARIO", state.currentTask)
        assertEquals("NEXT", state.nextTask)
        assertEquals("MONDAY 08:00", state.nextTaskTime)
        assertTrue(state.schedulerArmed)
    }

    @Test
    fun capabilityStateIsExposedWithoutClaimingAlarmIsArmed() {
        val schedule = scheduleWithMondayTask()
        val clock = Clock.fixed(
            Instant.parse("2026-10-05T13:30:00Z"),
            zoneId,
        )
        val engine = ScheduleEngine(schedule, clock, zoneId)

        val state = MainScreenStateFactory.create(
            schedule = schedule,
            engine = engine,
            now = clock.instant().atZone(zoneId),
            capabilities = AlarmCapabilities(
                notificationsGranted = false,
                fullScreenIntentGranted = false,
                exactAlarmAvailable = false,
            ),
            schedulerStatus = SchedulerStatus.NotArmed(
                NotArmedReason.EXACT_ALARM_UNAVAILABLE,
            ),
        )

        assertFalse(state.notificationsGranted)
        assertFalse(state.fullScreenIntentGranted)
        assertFalse(state.exactAlarmAvailable)
        assertFalse(state.schedulerArmed)
    }

    @Test
    fun todaysTimelineComesFromScheduleConfig() {
        val schedule = scheduleWithMondayTask()
        val clock = Clock.fixed(
            Instant.parse("2026-10-05T13:30:00Z"),
            zoneId,
        )

        val state = MainScreenStateFactory.create(
            schedule = schedule,
            engine = ScheduleEngine(schedule, clock, zoneId),
            now = clock.instant().atZone(zoneId),
            capabilities = AlarmCapabilities(true, true, true),
            schedulerStatus = SchedulerStatus.Armed(
                AlarmRequest("id", 1L, "NEXT", "NEXT.png"),
            ),
        )

        assertEquals(listOf(TimelineRow("08:00", "NEXT")), state.timeline)
    }

    private fun scheduleWithMondayTask(): ScheduleConfig {
        val empty = DaySchedule(timeline = emptyList(), alerts = emptyList())
        val days = DayOfWeek.values().associateWith { empty }.toMutableMap()
        days[DayOfWeek.MONDAY] = DaySchedule(
            timeline = listOf(
                TimelineEntry(LocalTime.of(8, 0), "NEXT"),
            ),
            alerts = emptyList(),
        )
        return ScheduleConfig(
            schemaVersion = 1,
            timezoneMode = "device",
            missedAlarmPolicy = "skip",
            audioRotation = listOf("audio.mp3"),
            tasks = setOf("NEXT"),
            days = days,
        )
    }
}
