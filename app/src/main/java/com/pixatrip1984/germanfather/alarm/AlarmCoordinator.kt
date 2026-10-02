package com.pixatrip1984.germanfather.alarm

class AlarmCoordinator(
    private val nextAlertSource: NextAlertSource,
    private val platform: AlarmPlatform,
    private val metadataStore: AlarmMetadataStore,
) {
    fun reschedule(): SchedulerStatus {
        val existing = metadataStore.current()

        if (!platform.canScheduleExactAlarms()) {
            existing?.let { platform.cancel(it.occurrenceId) }
            metadataStore.clear()
            return SchedulerStatus.NotArmed(NotArmedReason.EXACT_ALARM_UNAVAILABLE)
        }

        val next = nextAlertSource.nextFutureAlert()
        if (next == null) {
            existing?.let { platform.cancel(it.occurrenceId) }
            metadataStore.clear()
            return SchedulerStatus.NotArmed(NotArmedReason.NO_FUTURE_ALERT)
        }

        val epochMillis = next.at.toInstant().toEpochMilli()
        val request = AlarmRequest(
            occurrenceId = OccurrenceIdentity.forAlarm(epochMillis, next.taskId, next.visual),
            triggerEpochMillis = epochMillis,
            taskId = next.taskId,
            visual = next.visual,
        )

        if (existing != null && existing.occurrenceId != request.occurrenceId) {
            platform.cancel(existing.occurrenceId)
        }

        platform.setAlarmClock(request)
        metadataStore.save(AlarmMetadata(request.occurrenceId, request.triggerEpochMillis))
        return SchedulerStatus.Armed(request)
    }
}
