package com.pixatrip1984.germanfather.alarm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmDeliveryGuardTest {
    @Test
    fun acceptsOnlyMatchingOccurrenceAndEpochAndConsumesMetadata() {
        val store = Store(AlarmMetadata("id-1", 1234L))
        val guard = AlarmDeliveryGuard(store)

        assertTrue(guard.consumeIfCurrent("id-1", 1234L))
        assertNull(store.current())
    }

    @Test
    fun rejectsStaleOccurrenceIdentity() {
        val store = Store(AlarmMetadata("id-2", 1234L))
        val guard = AlarmDeliveryGuard(store)

        assertFalse(guard.consumeIfCurrent("id-1", 1234L))
        assertTrue(store.current() != null)
    }

    @Test
    fun rejectsTamperedTimestamp() {
        val store = Store(AlarmMetadata("id-1", 1234L))
        val guard = AlarmDeliveryGuard(store)

        assertFalse(guard.consumeIfCurrent("id-1", 9999L))
        assertTrue(store.current() != null)
    }

    @Test
    fun rejectsMissingDeliveryMetadata() {
        val store = Store(AlarmMetadata("id-1", 1234L))
        val guard = AlarmDeliveryGuard(store)

        assertFalse(guard.consumeIfCurrent(null, 1234L))
        assertFalse(guard.consumeIfCurrent("id-1", null))
        assertTrue(store.current() != null)
    }

    private class Store(initial: AlarmMetadata?) : AlarmMetadataStore {
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
