package com.pixatrip1984.germanfather.alarm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmPlaybackLifecycleTest {
    @Test
    fun onlyCurrentOccurrenceIsConsideredActive() {
        AlarmPlaybackLifecycle.clear()
        AlarmPlaybackLifecycle.markActive("occurrence-a")

        assertTrue(AlarmPlaybackLifecycle.isActive("occurrence-a"))
        assertFalse(AlarmPlaybackLifecycle.isActive("occurrence-b"))

        AlarmPlaybackLifecycle.clear()
        assertFalse(AlarmPlaybackLifecycle.isActive("occurrence-a"))
    }
}
