package com.pixatrip1984.germanfather.debug

import com.pixatrip1984.germanfather.alarm.AlarmMetadata
import com.pixatrip1984.germanfather.alarm.AlarmMetadataStore
import com.pixatrip1984.germanfather.alarm.AlarmPlatform
import com.pixatrip1984.germanfather.alarm.AlarmRequest
import com.pixatrip1984.germanfather.alarm.NotArmedReason
import com.pixatrip1984.germanfather.alarm.SchedulerStatus
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugAlarmHarnessSchedulerTest {
    private val zoneId = ZoneId.of("America/Monterrey")
    private val clock = Clock.fixed(
        Instant.parse("2026-10-02T12:00:00Z"),
        zoneId,
    )

    @Test
    fun schedulesOneGimnasioOccurrenceThroughAlarmPlatform() {
        val platform = FakePlatform(canSchedule = true)
        val store = InMemoryMetadataStore()
        val status = DebugAlarmHarnessScheduler(
            platform = platform,
            metadataStore = store,
            clock = clock,
            zoneId = zoneId,
        ).schedule(60)

        assertTrue(status is SchedulerStatus.Armed)
        val request = platform.scheduled.single()
        assertEquals("GIMNASIO", request.taskId)
        assertEquals("GIMNASIO.png", request.visual)
        assertEquals(clock.instant().plusSeconds(60).toEpochMilli(), request.triggerEpochMillis)
        assertEquals(request.occurrenceId, store.current()?.occurrenceId)
        assertEquals(request.triggerEpochMillis, store.current()?.triggerEpochMillis)
    }

    @Test
    fun replacesExistingProductionOccurrenceRatherThanAddingASecondAlarm() {
        val platform = FakePlatform(canSchedule = true)
        val store = InMemoryMetadataStore(
            AlarmMetadata("production-occurrence", 123L),
        )

        DebugAlarmHarnessScheduler(
            platform = platform,
            metadataStore = store,
            clock = clock,
            zoneId = zoneId,
        ).schedule(30)

        assertEquals(listOf("production-occurrence"), platform.cancelled)
        assertEquals(1, platform.scheduled.size)
    }

    @Test
    fun exactAlarmUnavailableDoesNotPretendHarnessWasScheduled() {
        val platform = FakePlatform(canSchedule = false)
        val store = InMemoryMetadataStore()

        val status = DebugAlarmHarnessScheduler(
            platform = platform,
            metadataStore = store,
            clock = clock,
            zoneId = zoneId,
        ).schedule(60)

        assertEquals(
            SchedulerStatus.NotArmed(NotArmedReason.EXACT_ALARM_UNAVAILABLE),
            status,
        )
        assertTrue(platform.scheduled.isEmpty())
        assertNull(store.current())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonPositiveDelay() {
        DebugAlarmHarnessScheduler(
            platform = FakePlatform(canSchedule = true),
            metadataStore = InMemoryMetadataStore(),
            clock = clock,
            zoneId = zoneId,
        ).schedule(0)
    }

    private class FakePlatform(
        private val canSchedule: Boolean,
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

    private class InMemoryMetadataStore(
        private var metadata: AlarmMetadata? = null,
    ) : AlarmMetadataStore {
        override fun current(): AlarmMetadata? = metadata

        override fun save(metadata: AlarmMetadata) {
            this.metadata = metadata
        }

        override fun clear() {
            metadata = null
        }
    }
}
