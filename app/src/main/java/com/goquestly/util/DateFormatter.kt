package com.goquestly.util

import java.text.DateFormat
import java.util.Date
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun formatDateTime(instant: kotlin.time.Instant): String {
    val dateFormat = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT,
    )
    val millis = instant.toEpochMilliseconds()
    return dateFormat.format(Date(millis))
}
