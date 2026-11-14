package com.pes.travelexplorer.ui.trips

import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Locale
import java.util.TimeZone

fun validTripDates(start: String, end: String): Boolean {
    fun parse(value: String): Long? {
        if (!value.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"))) return null
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val position = ParsePosition(0)
        val date = format.parse(value, position) ?: return null
        return date.time.takeIf { position.index == value.length && format.format(date) == value }
    }
    val first = parse(start) ?: return false
    val last = parse(end) ?: return false
    return first <= last
}
