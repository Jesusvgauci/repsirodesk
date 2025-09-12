package calculators

data class SPNResult(
    val probability: Double,
    val category: String
)

/**
 * Mayo Clinic model pre odhad malignity solitárneho nodulu.
 * Parametre: vek, fajčenie, anamnéza ca, priemer (mm), horný lalok, spikulácie.
 */
fun calculateSPN(
    age: Int,
    smoker: Boolean,
    cancerHistory: Boolean,
    diameterMm: Double,
    upperLobe: Boolean,
    spiculation: Boolean
): SPNResult {
    val x = -6.8272 +
            0.0391 * age +
            (if (smoker) 0.7917 else 0.0) +
            (if (cancerHistory) 1.3388 else 0.0) +
            0.1274 * diameterMm +
            (if (upperLobe) 0.7838 else 0.0) +
            (if (spiculation) 1.0407 else 0.0)

    val probability = 1.0 / (1.0 + kotlin.math.exp(-x)) * 100.0
    val category = when {
        probability < 5 -> "Nízka pravdepodobnosť"
        probability < 65 -> "Stredná pravdepodobnosť"
        else -> "Vysoká pravdepodobnosť"
    }

    return SPNResult(probability, category)
}
