package calculators

data class PESIResult(
    val score: Int,
    val classGroup: String,
    val mortality: String
)

/**
 * Zjednodušený PESI score.
 */
fun calculatePESI(
    age: Int,
    male: Boolean,
    cancer: Boolean,
    heartFailure: Boolean,
    lungDisease: Boolean,
    pulse: Int,
    sbp: Int,
    rr: Int,
    temp: Double,
    confusion: Boolean,
    spo2: Int
): PESIResult {
    var score = age
    if (!male) score -= 10
    if (cancer) score += 30
    if (heartFailure) score += 10
    if (lungDisease) score += 10
    if (pulse >= 110) score += 20
    if (sbp < 100) score += 30
    if (rr >= 30) score += 20
    if (temp < 36.0) score += 20
    if (confusion) score += 60
    if (spo2 < 90) score += 20

    val (classGroup, mortality) = when {
        score <= 65 -> "I" to "~1%"
        score in 66..85 -> "II" to "~2%"
        score in 86..105 -> "III" to "~3-7%"
        score in 106..125 -> "IV" to "~11%"
        else -> "V" to "~25%"
    }

    return PESIResult(score, classGroup, mortality)
}
