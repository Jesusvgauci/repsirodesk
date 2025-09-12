package calculators

data class STOPBANGResult(
    val score: Int,
    val risk: String
)

/**
 * STOP-BANG skóre pre obštrukčné spánkové apnoe.
 */
fun calculateSTOPBANG(
    snoring: Boolean,
    tired: Boolean,
    observed: Boolean,
    pressure: Boolean,
    bmi: Double,
    age: Int,
    neckCircumference: Double,
    male: Boolean
): STOPBANGResult {
    var score = 0
    if (snoring) score++
    if (tired) score++
    if (observed) score++
    if (pressure) score++
    if (bmi > 35) score++
    if (age > 50) score++
    if (neckCircumference > 40) score++
    if (male) score++

    val risk = when {
        score in 0..2 -> "Nízke riziko OSA"
        score in 3..4 -> "Stredné riziko OSA"
        else -> "Vysoké riziko OSA"
    }

    return STOPBANGResult(score, risk)
}
