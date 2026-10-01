package com.pixatrip1984.germanfather.schedule

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.time.DayOfWeek
import java.time.LocalTime

class ScheduleValidationException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)

object ScheduleParser {
    private val timePattern = Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$")

    fun parse(json: String, availableAssets: Set<String>): ScheduleConfig {
        val root = try {
            JsonParser.parseString(json).asJsonObject
        } catch (error: Exception) {
            throw ScheduleValidationException("Malformed schedule JSON", error)
        }

        val schemaVersion = root.requiredInt("schemaVersion")
        val timezoneMode = root.requiredString("timezoneMode")
        val missedAlarmPolicy = root.requiredString("missedAlarmPolicy")
        requireValid(timezoneMode == "device", "timezoneMode must be device")
        requireValid(missedAlarmPolicy == "skip", "missedAlarmPolicy must be skip")

        val audioRotation = root.requiredArray("audioRotation").mapIndexed { index, element ->
            element.requiredString("audioRotation[$index]")
        }
        requireValid(audioRotation.isNotEmpty(), "audioRotation must not be empty")
        requireValid(audioRotation.distinct().size == audioRotation.size, "audioRotation contains duplicates")
        audioRotation.forEach { audio ->
            requireValid(audio in availableAssets, "Missing referenced audio asset: $audio")
        }

        val taskElements = root.requiredArray("tasks")
        val tasks = taskElements.mapIndexed { index, element ->
            element.requiredString("tasks[$index]")
        }
        requireValid(tasks.isNotEmpty(), "tasks must not be empty")
        requireValid(tasks.distinct().size == tasks.size, "Duplicate task id")
        val taskSet = tasks.toSet()

        val rawDays = root.requiredObject("days")
        val parsedDays = linkedMapOf<DayOfWeek, DaySchedule>()
        rawDays.entrySet().forEach { (dayName, dayElement) ->
            val day = try {
                DayOfWeek.valueOf(dayName)
            } catch (error: IllegalArgumentException) {
                throw ScheduleValidationException("Unknown day: $dayName", error)
            }
            requireValid(day !in parsedDays, "Duplicate day: $dayName")
            val dayObject = dayElement.requireObject("days.$dayName")
            parsedDays[day] = parseDay(dayName, dayObject, taskSet, availableAssets)
        }
        requireValid(
            parsedDays.keys == DayOfWeek.values().toSet(),
            "days must contain exactly all seven weekdays",
        )

        return ScheduleConfig(
            schemaVersion = schemaVersion,
            timezoneMode = timezoneMode,
            missedAlarmPolicy = missedAlarmPolicy,
            audioRotation = audioRotation,
            tasks = taskSet,
            days = parsedDays,
        )
    }

    private fun parseDay(
        dayName: String,
        day: JsonObject,
        tasks: Set<String>,
        availableAssets: Set<String>,
    ): DaySchedule {
        val timeline = day.requiredArray("timeline").mapIndexed { index, element ->
            val entry = element.requireObject("$dayName.timeline[$index]")
            val time = parseTime(entry.requiredString("time"), "$dayName.timeline[$index].time")
            val taskId = entry.requiredString("taskId")
            requireValid(taskId in tasks, "Missing task id referenced by $dayName timeline: $taskId")
            TimelineEntry(time, taskId)
        }
        requireUniqueTimes(dayName, "timeline", timeline.map { it.time })

        val alerts = day.requiredArray("alerts").mapIndexed { index, element ->
            val entry = element.requireObject("$dayName.alerts[$index]")
            val time = parseTime(entry.requiredString("time"), "$dayName.alerts[$index].time")
            val taskId = entry.requiredString("taskId")
            val visual = entry.requiredString("visual")
            requireValid(taskId in tasks, "Missing task id referenced by $dayName alert: $taskId")
            requireValid(visual in availableAssets, "Missing referenced visual asset: $visual")
            AlertEntry(time, taskId, visual)
        }
        requireUniqueTimes(dayName, "alerts", alerts.map { it.time })

        return DaySchedule(
            timeline = timeline.sortedBy { it.time },
            alerts = alerts.sortedBy { it.time },
        )
    }

    private fun parseTime(raw: String, location: String): LocalTime {
        requireValid(timePattern.matches(raw), "Malformed time at $location: $raw")
        return try {
            LocalTime.parse(raw)
        } catch (error: Exception) {
            throw ScheduleValidationException("Malformed time at $location: $raw", error)
        }
    }

    private fun requireUniqueTimes(day: String, kind: String, times: List<LocalTime>) {
        requireValid(times.distinct().size == times.size, "Duplicate $kind time in $day")
    }

    private fun requireValid(condition: Boolean, message: String) {
        if (!condition) throw ScheduleValidationException(message)
    }

    private fun JsonObject.requiredString(name: String): String {
        val element = get(name) ?: throw ScheduleValidationException("Missing $name")
        return element.requiredString(name)
    }

    private fun JsonElement.requiredString(location: String): String {
        if (!isJsonPrimitive || !asJsonPrimitive.isString) {
            throw ScheduleValidationException("$location must be a string")
        }
        return asString.takeIf { it.isNotBlank() }
            ?: throw ScheduleValidationException("$location must not be blank")
    }

    private fun JsonObject.requiredInt(name: String): Int {
        val element = get(name) ?: throw ScheduleValidationException("Missing $name")
        return try {
            element.asInt
        } catch (error: Exception) {
            throw ScheduleValidationException("$name must be an integer", error)
        }
    }

    private fun JsonObject.requiredArray(name: String) =
        get(name)?.takeIf { it.isJsonArray }?.asJsonArray
            ?: throw ScheduleValidationException("Missing or invalid $name")

    private fun JsonObject.requiredObject(name: String) =
        get(name)?.requireObject(name)
            ?: throw ScheduleValidationException("Missing $name")

    private fun JsonElement.requireObject(location: String): JsonObject =
        takeIf { it.isJsonObject }?.asJsonObject
            ?: throw ScheduleValidationException("$location must be an object")
}
