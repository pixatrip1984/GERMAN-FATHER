package com.pixatrip1984.germanfather.schedule

import android.content.res.AssetManager

object AndroidScheduleLoader {
    fun load(assetManager: AssetManager): ScheduleConfig {
        val json = assetManager.open("schedule.json").bufferedReader().use { it.readText() }
        val availableAssets = assetManager.list("").orEmpty().toSet()
        return ScheduleParser.parse(json, availableAssets)
    }
}
