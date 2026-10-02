package com.pixatrip1984.germanfather.alarm

object OccurrenceIdentity {
    fun forAlarm(triggerEpochMillis: Long, taskId: String, visual: String): String =
        "$triggerEpochMillis|$taskId|$visual"
}
