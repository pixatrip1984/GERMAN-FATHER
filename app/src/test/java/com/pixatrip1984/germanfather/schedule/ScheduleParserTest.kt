package com.pixatrip1984.germanfather.schedule

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ScheduleParserTest {
    @Test
    fun productionScheduleHasApprovedPolicyAndRotation() {
        val schedule = ScheduleTestFixtures.productionSchedule()

        assertEquals("device", schedule.timezoneMode)
        assertEquals("skip", schedule.missedAlarmPolicy)
        assertEquals(listOf("1.mp3", "2.mp3", "3.mp3"), schedule.audioRotation)
    }

    @Test
    fun rejectsMalformedJson() {
        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse("{", ScheduleTestFixtures.assetNames)
        }
    }

    @Test
    fun rejectsMalformedTime() {
        val broken = ScheduleTestFixtures.productionJson()
            .replaceFirst("\"07:00\"", "\"7:00\"")
        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse(broken, ScheduleTestFixtures.assetNames)
        }
    }

    @Test
    fun rejectsDuplicateTimelineTime() {
        val broken = ScheduleTestFixtures.productionJson()
            .replaceFirst("\"07:25\"", "\"07:00\"")
        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse(broken, ScheduleTestFixtures.assetNames)
        }
    }

    @Test
    fun rejectsDuplicateAlertTime() {
        val original = ScheduleTestFixtures.productionJson()
        val firstAlerts = original.indexOf("\"alerts\"")
        val broken = original.substring(0, firstAlerts) +
            original.substring(firstAlerts).replaceFirst("\"07:25\"", "\"07:00\"")
        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse(broken, ScheduleTestFixtures.assetNames)
        }
    }

    @Test
    fun rejectsUnknownDay() {
        val broken = ScheduleTestFixtures.productionJson()
            .replaceFirst("\"MONDAY\"", "\"FUNDAY\"")
        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse(broken, ScheduleTestFixtures.assetNames)
        }
    }

    @Test
    fun rejectsMissingTaskReference() {
        val broken = ScheduleTestFixtures.productionJson()
            .replaceFirst("\"taskId\": \"CORRAL\"", "\"taskId\": \"UNKNOWN\"")
        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse(broken, ScheduleTestFixtures.assetNames)
        }
    }

    @Test
    fun rejectsMissingVisualAsset() {
        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse(
                ScheduleTestFixtures.productionJson(),
                ScheduleTestFixtures.assetNames - "CORRAL.png",
            )
        }
    }

    @Test
    fun rejectsMissingAudioAsset() {
        assertThrows(ScheduleValidationException::class.java) {
            ScheduleParser.parse(
                ScheduleTestFixtures.productionJson(),
                ScheduleTestFixtures.assetNames - "1.mp3",
            )
        }
    }
}
