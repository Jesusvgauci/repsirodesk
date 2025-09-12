package util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatDate(timestamp: Long): String {
    val ldt = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    fun pad(n: Int) = if (n < 10) "0$n" else "$n"
    return "${pad(ldt.dayOfMonth)}.${pad(ldt.monthNumber)}.${ldt.year} " +
            "${pad(ldt.hour)}:${pad(ldt.minute)}"
}
