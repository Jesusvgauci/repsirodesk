package calculators

data class SpO2FiO2Result(
    val ratio: Double,
    val interpretation: String
)

/**
 * SpO₂/FiO₂ ratio – jednoduchý odhad oxigenácie.
 * @param spo2 saturácia O₂ v %
 * @param fio2 frakcia kyslíka (napr. 0.21 pri vzduchu, 0.5 pri 50% O₂)
 */
fun calculateSpO2FiO2(spo2: Int, fio2: Double): SpO2FiO2Result {
    val ratio = spo2 / fio2
    val interpretation = when {
        ratio >= 315 -> "Normálne / mierne znížené"
        ratio in 235.0..314.9 -> "Stredné poškodenie oxigenácie"
        else -> "Ťažké poškodenie oxigenácie"
    }
    return SpO2FiO2Result(ratio, interpretation)
}
