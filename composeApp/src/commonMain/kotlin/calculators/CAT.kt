package calculators

data class CATResult(
    val total: Int,
    val impact: String
)

/**
 * Výpočet COPD Assessment Test (CAT).
 * @param answers – zoznam 8 odpovedí (0–5).
 */
fun calculateCAT(answers: List<Int>): CATResult {
    val total = answers.sum()
    val impact = when (total) {
        in 0..9 -> "Nízky vplyv"
        in 10..20 -> "Stredný vplyv"
        in 21..30 -> "Vysoký vplyv"
        else -> "Veľmi vysoký vplyv"
    }
    return CATResult(total, impact)
}
