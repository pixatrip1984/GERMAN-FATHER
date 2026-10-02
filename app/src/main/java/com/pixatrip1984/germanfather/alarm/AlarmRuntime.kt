package com.pixatrip1984.germanfather.alarm

import android.content.Context
import com.pixatrip1984.germanfather.schedule.AndroidScheduleLoader
import com.pixatrip1984.germanfather.schedule.ScheduleEngine
import java.time.Clock
import java.time.Duration
import java.time.ZoneId

object AlarmRuntime {
    fun coordinator(context: Context): AlarmCoordinator {
        val applicationContext = context.applicationContext
        val baseClock = Clock.systemDefaultZone()
        val zoneId = ZoneId.systemDefault()
        val schedule = AndroidScheduleLoader.load(applicationContext.assets)
        val nextAlertSource = NextAlertSource {
            ScheduleEngine(
                schedule = schedule,
                clock = Clock.offset(baseClock, Duration.ofMillis(1)),
                zoneId = zoneId,
            ).nextFutureAlert()
        }

        return AlarmCoordinator(
            nextAlertSource = nextAlertSource,
            platform = AndroidAlarmPlatform(applicationContext),
            metadataStore = SharedPreferencesAlarmMetadataStore(applicationContext),
        )
    }
}
