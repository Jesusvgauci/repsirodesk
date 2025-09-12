package calculators

data class PSIResult(
    val score: Int,
    val riskClass: String,
    val mortality: String,
    val recommendation: String
)

/**
 * Zjednodušený PSI/PORT skóre (bez laboratórnych detailov).
 * Parametre: vek, pohlavie, komorbidity, vitálne funkcie.
 */
fun calculatePSI(
    age: Int,
    male: Boolean,
    nursingHome: Boolean,
    neoplastic: Boolean,
    liver: Boolean,
    heartFailure: Boolean,
    cerebrovascular: Boolean,
    renal: Boolean,
    rr: Int,
    sbp: Int,
    temp: Double,
    pulse: Int,
    confusion: Boolean
): PSIResult {
    var score = age

    if (!male) score -= 10
    if (nursingHome) score += 10
    if (neoplastic) score += 30
    if (liver) score += 20
    if (heartFailure) score += 10
    if (cerebrovascular) score += 10
    if (renal) score += 10
    if (rr >= 30) score += 20
    if (sbp < 90) score += 20
    if (temp < 35.0 || temp > 40.0) score += 15
    if (pulse >= 125) score += 10
    if (confusion) score += 20

    val (riskClass, mortality, recommendation) = when {
        score <= 50 -> Triple("I", "<1%", "Ambulantná liečba")
        score in 51..70 -> Triple("II", "<1%", "Ambulantná liečba")
        score in 71..90 -> Triple("III", "~1-3%", "Krátka hospitalizácia alebo sledovanie")
        score in 91..130 -> Triple("IV", "~8-9%", "Hospitalizácia")
        else -> Triple("V", "~27-30%", "Hospitalizácia, zvážiť JIS")
    }

    return PSIResult(score, riskClass, mortality, recommendation)
}
