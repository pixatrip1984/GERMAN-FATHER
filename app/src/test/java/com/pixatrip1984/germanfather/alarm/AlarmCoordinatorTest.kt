package com.pixatrip1984.germanfather.alarm

import com.pixatrip1984.germanfather.schedule.AlertOccurrence
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmCoordinatorTest {
    @Test
    fun armsExactlyTheEpochReturnedByScheduleSource() {
        val occurrence = occurrence("2026-10-05T07:00:00-06:00", "CORRAL", "CORRAL.png")
        val platform = FakeAlarmPlatform()
        val store = FakeAlarmMetadataStore()
        val coordinator = AlarmCoordinator(NextAlertSource { occurrence }, platform, store)

        val status = coordinator.reschedule()

        assertTrue(status is SchedulerStatus.Armed)
        assertEquals(1, platform.scheduled.size)
        assertEquals(occurrence.at.toInstant().toEpochMilli(), platform.scheduled.single().triggerEpochMillis)
        assertEquals(platform.scheduled.single().occurrenceId, store.current()?.occurrenceId)
    }

    @Test
    fun replacingOccurrenceCancelsOnlyPreviouslyArmedIdentity() {
        val old = AlarmMetadata("old-occurrence", 10L)
        val store = FakeAlarmMetadataStore(old)
        val platform = FakeAlarmPlatform()
        val coordinator = AlarmCoordinator(
            NextAlertSource { occurrence("2026-10-05T07:25:00-06:00", "TRASTES", "TRASTES.png") },
            platform,
            store,
        )

        coordinator.reschedule()

        assertEquals(listOf("old-occurrence"), platform.cancelled)
        assertEquals(1, platform.scheduled.size)
    }

    @Test
    fun exactAlarmUnavailableIsReportedAndMetadataIsCleared() {
        val store = FakeAlarmMetadataStore(AlarmMetadata("armed", 100L))
        val platform = FakeAlarmPlatform(canSchedule = false)
        val coordinator = AlarmCoordinator(
            NextAlertSource { occurrence("2026-10-05T07:00:00-06:00", "CORRAL", "CORRAL.png") },
            platform,
            store,
        )

        val status = coordinator.reschedule()

        assertEquals(
            SchedulerStatus.NotArmed(NotArmedReason.EXACT_ALARM_UNAVAILABLE),
            status,
        )
        assertEquals(listOf("armed"), platform.cancelled)
        assertEquals(null, store.current())
        assertTrue(platform.scheduled.isEmpty())
    }

    @Test
    fun noFutureAlertCancelsAnyOldAlarmAndReportsNotArmed() {
        val store = FakeAlarmMetadataStore(AlarmMetadata("armed", 100L))
        val platform = FakeAlarmPlatform()
        val coordinator = AlarmCoordinator(NextAlertSource { null }, platform, store)

        val status = coordinator.reschedule()

        assertEquals(SchedulerStatus.NotArmed(NotArmedReason.NO_FUTURE_ALERT), status)
        assertEquals(listOf("armed"), platform.cancelled)
        assertEquals(null, store.current())
    }

    @Test
    fun sameOccurrenceIsIdempotentlyRearmedWithoutCancellation() {
        val alert = occurrence("2026-10-05T07:00:00-06:00", "CORRAL", "CORRAL.png")
        val epoch = alert.at.toInstant().toEpochMilli()
        val id = OccurrenceIdentity.forAlarm(epoch, alert.taskId, alert.visual)
        val store = FakeAlarmMetadataStore(AlarmMetadata(id, epoch))
        val platform = FakeAlarmPlatform()

        AlarmCoordinator(NextAlertSource { alert }, platform, store).reschedule()

        assertTrue(platform.cancelled.isEmpty())
        assertEquals(1, platform.scheduled.size)
        assertEquals(id, platform.scheduled.single().occurrenceId)
    }

    private fun occurrence(local: String, taskId: String, visual: String): AlertOccurrence =
        AlertOccurrence(
            taskId = taskId,
            visual = visual,
            at = ZonedDateTime.parse(local + "[America/Monterrey]"),
        )

    private class FakeAlarmPlatform(
        private val canSchedule: Boolean = true,
    ) : AlarmPlatform {
        val scheduled = mutableListOf<AlarmRequest>()
        val cancelled = mutableListOf<String>()

        override fun canScheduleExactAlarms(): Boolean = canSchedule

        override fun setAlarmClock(request: AlarmRequest) {
            scheduled += request
        }

        override fun cancel(occurrenceId: String) {
            cancelled += occurrenceId
        }
    }

    private class FakeAlarmMetadataStore(
        initial: AlarmMetadata? = null,
    ) : AlarmMetadataStore {
        private var value = initial

        override fun current(): AlarmMetadata? = value

        override fun save(metadata: AlarmMetadata) {
            value = metadata
        }

        override fun clear() {
            value = null
        }
    }
}
