package calculators

data class GenevaResult(
    val score: Int,
    val risk: String
)

/**
 * Revised Geneva Score pre PE (Wicki 2001, klinc. upravený).
 * Premenné:
 * - Vek >65 r. (1 bod)
 * - Predchádzajúca VTE (3 b.)
 * - Operácia alebo fraktúra v poslednom mesiaci (2 b.)
 * - Aktívna malignita (2 b.)
 * - Jednostranná bolesť DK (3 b.)
 * - Hemoptýza (2 b.)
 * - Srdcová frekvencia 75–94 (3 b.), ≥95 (5 b.)
 * - Bolesť pri palpácii DK a jednostranný opuch (4 b.)
 */
fun calculateGeneva(
    age65: Boolean,
    prevVTE: Boolean,
    surgeryFracture: Boolean,
    cancer: Boolean,
    legPain: Boolean,
    hemoptysis: Boolean,
    hr: Int,
    legSwelling: Boolean
): GenevaResult {
    var score = 0

    if (age65) score += 1
    if (prevVTE) score += 3
    if (surgeryFracture) score += 2
    if (cancer) score += 2
    if (legPain) score += 3
    if (hemoptysis) score += 2
    if (hr in 75..94) score += 3
    if (hr >= 95) score += 5
    if (legSwelling) score += 4

    val risk = when (score) {
        in 0..3 -> "Nízka pravdepodobnosť"
        in 4..10 -> "Stredná pravdepodobnosť"
        else -> "Vysoká pravdepodobnosť"
    }

    return GenevaResult(score, risk)
}
