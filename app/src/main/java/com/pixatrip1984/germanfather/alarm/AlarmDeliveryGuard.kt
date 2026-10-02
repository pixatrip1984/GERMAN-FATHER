package com.pixatrip1984.germanfather.alarm

class AlarmDeliveryGuard(
    private val metadataStore: AlarmMetadataStore,
) {
    fun consumeIfCurrent(occurrenceId: String?, intendedEpochMillis: Long?): Boolean {
        if (occurrenceId == null || intendedEpochMillis == null) return false
        val current = metadataStore.current() ?: return false
        if (current.occurrenceId != occurrenceId) return false
        if (current.triggerEpochMillis != intendedEpochMillis) return false

        metadataStore.clear()
        return true
    }
}
