package calculators

data class RESECT90Result(
    val score: Int,
    val risk: String
)

/**
 * RESECT-90: jednoduchý model 90-dňovej mortality po resekcii pľúc.
 * (Zjednodušená verzia pre klinickú pomôcku).
 */
fun calculateRESECT90(
    age: Int,
    male: Boolean,
    fev1Percent: Int,
    dlcoPercent: Int,
    pneumonectomy: Boolean
): RESECT90Result {
    var score = 0
    if (age >= 70) score += 2
    if (male) score += 1
    if (fev1Percent < 60) score += 2
    if (dlcoPercent < 60) score += 2
    if (pneumonectomy) score += 3

    val risk = when (score) {
        0,1 -> "Nízke riziko (<2%)"
        2,3 -> "Stredné riziko (~5%)"
        4,5 -> "Vyššie riziko (~10%)"
        else -> "Vysoké riziko (>15%)"
    }

    return RESECT90Result(score, risk)
}
