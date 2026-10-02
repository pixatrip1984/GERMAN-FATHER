package com.pixatrip1984.germanfather.schedule

import java.io.File
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

internal object ScheduleTestFixtures {
    val assetNames = setOf(
        "1.mp3", "2.mp3", "3.mp3",
        "COMER.png", "CORRAL.png", "DORMIR.png", "DUCHA.png", "GIMNASIO.png",
        "IRSE.png", "PERSONAL.png", "TAREA.png", "TRASTES.png",
        "schedule.json",
    )

    fun productionJson(): String {
        val candidates = listOf(
            File("src/main/assets/schedule.json"),
            File("app/src/main/assets/schedule.json"),
        )
        return candidates.firstOrNull(File::isFile)?.readText()
            ?: error("Unable to locate production schedule.json from " + File(".").absolutePath)
    }

    fun productionSchedule(): ScheduleConfig =
        ScheduleParser.parse(productionJson(), assetNames)

    fun engineAt(localDateTime: String, zoneId: ZoneId = ZoneId.of("America/Monterrey")): ScheduleEngine {
        val instant = ZonedDateTime.parse(localDateTime + "[" + zoneId.id + "]").toInstant()
        return ScheduleEngine(productionSchedule(), Clock.fixed(instant, zoneId), zoneId)
    }
}
