package utils

import kotlin.math.pow
import kotlin.math.round

fun Double.toFixed(decimals: Int): String {
    val factor = 10.0.pow(decimals)
    val rounded = round(this * factor) / factor
    val parts = rounded.toString().split(".")
    return if (decimals == 0) {
        parts[0] // iba celé číslo
    } else {
        val intPart = parts[0]
        val fracPart = if (parts.size > 1) {
            parts[1].padEnd(decimals, '0').take(decimals)
        } else {
            "0".repeat(decimals)
        }
        "$intPart.$fracPart"
    }
}
