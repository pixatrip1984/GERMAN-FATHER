package com.pixatrip1984.germanfather.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OccurrenceIdentityTest {
    @Test
    fun identityIsDeterministicForSameOccurrence() {
        val first = OccurrenceIdentity.forAlarm(1234L, "TASK", "TASK.png")
        val second = OccurrenceIdentity.forAlarm(1234L, "TASK", "TASK.png")

        assertEquals(first, second)
    }

    @Test
    fun timestampTaskAndVisualAllParticipateInIdentity() {
        val base = OccurrenceIdentity.forAlarm(1234L, "TASK", "TASK.png")

        assertNotEquals(base, OccurrenceIdentity.forAlarm(1235L, "TASK", "TASK.png"))
        assertNotEquals(base, OccurrenceIdentity.forAlarm(1234L, "OTHER", "TASK.png"))
        assertNotEquals(base, OccurrenceIdentity.forAlarm(1234L, "TASK", "OTHER.png"))
    }
}
