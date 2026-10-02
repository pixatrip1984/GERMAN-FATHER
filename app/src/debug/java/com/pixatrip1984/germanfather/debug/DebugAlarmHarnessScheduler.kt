package com.pixatrip1984.germanfather.debug

import com.pixatrip1984.germanfather.alarm.AlarmCoordinator
import com.pixatrip1984.germanfather.alarm.AlarmMetadataStore
import com.pixatrip1984.germanfather.alarm.AlarmPlatform
import com.pixatrip1984.germanfather.alarm.NextAlertSource
import com.pixatrip1984.germanfather.alarm.SchedulerStatus
import com.pixatrip1984.germanfather.schedule.AlertOccurrence
import java.time.Clock
import java.time.ZoneId

class DebugAlarmHarnessScheduler(
    private val platform: AlarmPlatform,
    private val metadataStore: AlarmMetadataStore,
    private val clock: Clock,
    private val zoneId: ZoneId,
) {
    fun schedule(delaySeconds: Int): SchedulerStatus {
        require(delaySeconds > 0) { "delaySeconds must be positive" }

        val at = clock.instant()
            .plusSeconds(delaySeconds.toLong())
            .atZone(zoneId)
        val coordinator = AlarmCoordinator(
            nextAlertSource = NextAlertSource {
                AlertOccurrence(
                    taskId = TEST_TASK_ID,
                    visual = TEST_VISUAL,
                    at = at,
                )
            },
            platform = platform,
            metadataStore = metadataStore,
        )
        return coordinator.reschedule()
    }

    companion object {
        const val DEFAULT_DELAY_SECONDS = 60
        const val TEST_TASK_ID = "GIMNASIO"
        const val TEST_VISUAL = "GIMNASIO.png"
    }
}
