package com.pixatrip1984.germanfather.schedule

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class ScheduleRepositoryTest {
    @Test
    fun loadsProductionScheduleFromTheEditableJsonSource() {
        val schedule = ScheduleTestFixtures.productionSchedule()

        assertEquals(7, schedule.days.size)
        assertEquals("device", schedule.timezoneMode)
        assertEquals("skip", schedule.missedAlarmPolicy)
        assertEquals(listOf("1.mp3", "2.mp3", "3.mp3"), schedule.audioRotation)
        assertTrue(schedule.days.getValue(DayOfWeek.SATURDAY).timeline.isEmpty())
        assertTrue(schedule.days.getValue(DayOfWeek.SATURDAY).alerts.isEmpty())
        assertTrue(schedule.days.getValue(DayOfWeek.SUNDAY).timeline.isEmpty())
        assertTrue(schedule.days.getValue(DayOfWeek.SUNDAY).alerts.isEmpty())
    }

    @Test
    fun rejectsMissingReferencedMediaAtTheDataBoundary() {
        val json = ScheduleTestFixtures.productionJson()

        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse(json, ScheduleTestFixtures.assetNames - "IRSE.png")
        }
        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse(json, ScheduleTestFixtures.assetNames - "1.mp3")
        }
    }

    @Test
    fun rejectsMalformedProductionData() {
        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse("{", ScheduleTestFixtures.assetNames)
        }
    }
}
