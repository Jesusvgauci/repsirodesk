package calculators
data class BODEResult(
    val score: Int,
    val mortalityRisk: String,
    val details: List<String>
)
/**
 * Výpočet BODE indexu podľa BMI, FEV1 (% predikcie), mMRC skóre a 6MWD (m).
 * Body:
 * - BMI < 21 = 1 bod
 * - FEV1 (% pred) ≥65 = 0, 50–64 = 1, 36–49 = 2, ≤35 = 3
 * - mMRC: 0–1 = 0, 2 = 1, 3 = 2, 4 = 3
 * - 6MWD (m): ≥350 = 0, 250–349 = 1, 150–249 = 2, ≤149 = 3
 */
fun calculateBODE(bmi: Double, fev1: Double, mmrc: Int, sixMinuteWalk: Int): BODEResult {
    var score = 0
    val details = mutableListOf<String>()
    // BMI
    val bmiPoints = if (bmi < 21.0) 1 else 0
    score += bmiPoints
    details.add("BMI: $bmi → $bmiPoints b.")
    // FEV1
    val fevPoints = when {
        fev1 >= 65 -> 0
        fev1 in 50.0..64.9 -> 1
        fev1 in 36.0..49.9 -> 2
        else -> 3
    }
    score += fevPoints
    details.add("FEV₁ %: $fev1 → $fevPoints b.")
    // mMRC
    val mmrcPoints = when (mmrc) {
        0, 1 -> 0
        2 -> 1
        3 -> 2
        4 -> 3
        else -> 0
    }
    score += mmrcPoints
    details.add("mMRC: $mmrc → $mmrcPoints b.")
    // 6MWD
    val walkPoints = when {
        sixMinuteWalk >= 350 -> 0
        sixMinuteWalk in 250..349 -> 1
        sixMinuteWalk in 150..249 -> 2
        else -> 3
    }
    score += walkPoints
    details.add("6MWD: $sixMinuteWalk m → $walkPoints b.")
    // Riziko mortality (len orientačne)
    val risk = when {
        score <= 2 -> "Nízke riziko"
        score in 3..4 -> "Stredné riziko"
        score in 5..6 -> "Vyššie riziko"
        else -> "Vysoké riziko"
    }
    return BODEResult(score, risk, details)
}