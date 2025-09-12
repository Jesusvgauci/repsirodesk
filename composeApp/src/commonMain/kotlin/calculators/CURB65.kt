package calculators

data class CURB65Result(
    val score: Int,
    val risk: String,
    val recommendation: String
)

/**
 * CURB-65 skóre pre mortalitu pri CAP.
 * Kritériá (1 bod každé):
 * - Confusion
 * - Urea > 7 mmol/L
 * - Respiračná frekvencia ≥ 30/min
 * - Krvný tlak (systolický <90 alebo diastolický ≤60)
 * - Vek ≥ 65 rokov
 */
fun calculateCURB65(
    confusion: Boolean,
    ureaHigh: Boolean,
    rrHigh: Boolean,
    lowBP: Boolean,
    age65: Boolean
): CURB65Result {
    val score = listOf(confusion, ureaHigh, rrHigh, lowBP, age65).count { it }

    val risk = when (score) {
        0, 1 -> "Nízke riziko (≤3% mortalita)"
        2 -> "Stredné riziko (~9% mortalita)"
        else -> "Vysoké riziko (22–40% mortalita)"
    }

    val recommendation = when (score) {
        0, 1 -> "Ambulantná liečba"
        2 -> "Zvážiť krátku hospitalizáciu alebo bližšie sledovanie"
        else -> "Hospitalizácia, zvážiť JIS"
    }

    return CURB65Result(score, risk, recommendation)
}
