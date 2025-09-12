package calculators

import utils.toFixed

data class LightResult(
    val isExudate: Boolean,
    val explanation: List<String>
)

fun evaluateLight(
    serumProtein: Double,
    pleuralProtein: Double,
    serumLDH: Double,
    pleuralLDH: Double,
    serumLDH_ULN: Double
): LightResult {
    val ratioProtein = pleuralProtein / serumProtein
    val ratioLDH = pleuralLDH / serumLDH
    val twoThirdsULN = (2.0 / 3.0) * serumLDH_ULN

    val criteriaMet = mutableListOf<String>()

    if (ratioProtein > 0.5) criteriaMet.add("Proteínové kritérium splnené (>0.5)")
    if (ratioLDH > 0.6) criteriaMet.add("LDH pomerové kritérium splnené (>0.6)")
    if (pleuralLDH > twoThirdsULN) criteriaMet.add("LDH > 2/3 ULN splnené")

    val exudate = criteriaMet.isNotEmpty()

    val explanation = mutableListOf<String>()
    explanation.add("PF/S proteín = ${ratioProtein.toFixed(2)} (kritérium >0.50)")
    explanation.add("PF/S LDH = ${ratioLDH.toFixed(2)} (kritérium >0.60)")
    explanation.add("Pleural LDH = ${pleuralLDH.toFixed(0)} vs 2/3 ULN = ${twoThirdsULN.toFixed(0)}")
    explanation.add(if (exudate) "➤ Výpotok je EXSUDÁT (splnené: ${criteriaMet.joinToString(", ")})"
    else "➤ Výpotok je TRANSUDÁT (žiadne kritérium nesplnené)")

    return LightResult(
        isExudate = exudate,
        explanation = explanation
    )
}
