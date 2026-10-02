package com.pixatrip1984.germanfather.schedule

import java.time.DayOfWeek
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleEngineTest {
    @Test
    fun mondayUsesPersonalAndNoonDisplayIsSchoolWhileAlertIsLeave() {
        val morning = ScheduleTestFixtures.engineAt("2026-09-28T08:00:00-06:00")
        assertEquals("PERSONAL", morning.currentDisplayState().taskId)
        assertEquals("GIMNASIO", morning.nextDisplayState()?.taskId)

        val noon = ScheduleTestFixtures.engineAt("2026-09-28T12:00:00-06:00")
        assertEquals("ESCUELA", noon.currentDisplayState().taskId)
        assertEquals("IRSE", noon.nextFutureAlert()?.taskId)
        assertEquals("IRSE.png", noon.nextFutureAlert()?.visual)
    }

    @Test
    fun tuesdayUsesGymAtEight() {
        val engine = ScheduleTestFixtures.engineAt("2026-09-29T08:00:00-06:00")
        assertEquals("GIMNASIO", engine.currentDisplayState().taskId)
        assertEquals("DUCHA", engine.nextDisplayState()?.taskId)
    }

    @Test
    fun wednesdayUsesPersonalAtEight() {
        val engine = ScheduleTestFixtures.engineAt("2026-09-30T08:00:00-06:00")
        assertEquals("PERSONAL", engine.currentDisplayState().taskId)
    }

    @Test
    fun thursdayUsesGymAtEight() {
        val engine = ScheduleTestFixtures.engineAt("2026-10-01T08:00:00-06:00")
        assertEquals("GIMNASIO", engine.currentDisplayState().taskId)
    }

    @Test
    fun fridayUsesPersonalAtEight() {
        val engine = ScheduleTestFixtures.engineAt("2026-10-02T08:00:00-06:00")
        assertEquals("PERSONAL", engine.currentDisplayState().taskId)
    }

    @Test
    fun saturdayHasNoCurrentScheduleAndRollsToMonday() {
        val engine = ScheduleTestFixtures.engineAt("2026-10-03T12:00:00-06:00")

        assertEquals(DisplayState.NO_SCHEDULE_TASK_ID, engine.currentDisplayState().taskId)
        assertEquals(DayOfWeek.MONDAY, engine.nextDisplayState()?.at?.dayOfWeek)
        assertEquals("CORRAL", engine.nextDisplayState()?.taskId)
        assertEquals(DayOfWeek.MONDAY, engine.nextFutureAlert()?.at?.dayOfWeek)
    }

    @Test
    fun sundayHasNoCurrentScheduleAndRollsToMonday() {
        val engine = ScheduleTestFixtures.engineAt("2026-10-04T12:00:00-06:00")

        assertEquals(DisplayState.NO_SCHEDULE_TASK_ID, engine.currentDisplayState().taskId)
        assertEquals(DayOfWeek.MONDAY, engine.nextDisplayState()?.at?.dayOfWeek)
        assertEquals("CORRAL", engine.nextFutureAlert()?.taskId)
    }

    @Test
    fun beforeFirstWeekdayEntryCurrentIsNoSchedule() {
        val engine = ScheduleTestFixtures.engineAt("2026-09-28T06:59:59-06:00")

        assertEquals(DisplayState.NO_SCHEDULE_TASK_ID, engine.currentDisplayState().taskId)
        assertEquals("CORRAL", engine.nextDisplayState()?.taskId)
        assertEquals("CORRAL", engine.nextFutureAlert()?.taskId)
    }

    @Test
    fun exactAlertBoundaryIsStillEligible() {
        val engine = ScheduleTestFixtures.engineAt("2026-09-28T07:00:00-06:00")

        assertEquals("CORRAL", engine.currentDisplayState().taskId)
        assertEquals("CORRAL", engine.nextFutureAlert()?.taskId)
        assertEquals(7, engine.nextFutureAlert()?.at?.hour)
        assertEquals(0, engine.nextFutureAlert()?.at?.minute)
    }

    @Test
    fun elapsedAlertIsSkippedImmediately() {
        val engine = ScheduleTestFixtures.engineAt("2026-09-28T07:00:01-06:00")

        assertEquals("TRASTES", engine.nextFutureAlert()?.taskId)
    }

    @Test
    fun afterFridayLastAlertCrossesWeekend() {
        val engine = ScheduleTestFixtures.engineAt("2026-10-02T23:10:01-06:00")

        assertEquals("DORMIR", engine.currentDisplayState().taskId)
        assertEquals(DayOfWeek.MONDAY, engine.nextFutureAlert()?.at?.dayOfWeek)
        assertEquals("CORRAL", engine.nextFutureAlert()?.taskId)
    }

    @Test
    fun calculationsHonorProvidedDeviceZone() {
        val zone = ZoneId.of("America/Mexico_City")
        val engine = ScheduleTestFixtures.engineAt("2026-09-28T12:00:00-06:00", zone)

        assertEquals("ESCUELA", engine.currentDisplayState().taskId)
        assertEquals(zone, engine.nextDisplayState()?.at?.zone)
    }
}
