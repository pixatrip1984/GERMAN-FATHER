package com.pixatrip1984.germanfather.alarm

data class AlarmRequest(
    val occurrenceId: String,
    val triggerEpochMillis: Long,
    val taskId: String,
    val visual: String,
)

data class AlarmMetadata(
    val occurrenceId: String,
    val triggerEpochMillis: Long,
)

sealed interface SchedulerStatus {
    data class Armed(val request: AlarmRequest) : SchedulerStatus
    data class NotArmed(val reason: NotArmedReason) : SchedulerStatus
}

enum class NotArmedReason {
    EXACT_ALARM_UNAVAILABLE,
    NO_FUTURE_ALERT,
}
