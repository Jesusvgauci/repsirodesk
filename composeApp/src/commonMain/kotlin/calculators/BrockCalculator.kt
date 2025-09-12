package calculators

import kotlin.math.exp
import kotlin.math.pow

data class BrockInputs(
    val age: Int,                 // roky
    val female: Boolean,          // 1 = žena, 0 = muž
    val famHx: Boolean,           // rodinná anamnéza Ca pulm.
    val emphysema: Boolean,       // emfyzém na CT
    val diameterMm: Double,       // priemer uzla (avg long+short), mm
    val type: NoduleType,         // SOLID / PART_SOLID / NON_SOLID
    val upperLobe: Boolean,       // horný lalok
    val noduleCount: Int,         // počet uzlov
    val spiculation: Boolean      // spikulácie
)

enum class NoduleType { SOLID, PART_SOLID, NON_SOLID }

object BrockCalculator {
    /**
     * Full Brock (PanCan) model so spikuláciou a centrovaním veľkosti:
     * logit(p) = 0.0287*(Age-62) + 0.6011*Female + 0.2961*FamHx + 0.2953*Emphysema
     *            -5.3854 * [ (diameter_cm)^(-0.5) - 1.58113883 ]
     *            + 0.377*PartSolid - 0.1276*NonSolid
     *            + 0.6581*UpperLobe - 0.0824*(NoduleCount-4)
     *            + 0.7729*Spiculation - 6.7892
     *
     * Kde diameter_cm = diameterMm / 10.
     * Pri diameter <= 0 funkcia vráti NaN.
     */
    fun probability(inputs: BrockInputs): Double {
        if (inputs.diameterMm <= 0.0) return Double.NaN

        val female = if (inputs.female) 1.0 else 0.0
        val famHx = if (inputs.famHx) 1.0 else 0.0
        val emph = if (inputs.emphysema) 1.0 else 0.0
        val partSolid = if (inputs.type == NoduleType.PART_SOLID) 1.0 else 0.0
        val nonSolid = if (inputs.type == NoduleType.NON_SOLID) 1.0 else 0.0
        val upper = if (inputs.upperLobe) 1.0 else 0.0
        val spic = if (inputs.spiculation) 1.0 else 0.0

        val diameterCm = inputs.diameterMm / 10.0
        val sizeTerm = diameterCm.pow(-0.5) - 1.58113883

        val logit =
            0.0287 * (inputs.age - 62) +
                    0.6011 * female +
                    0.2961 * famHx +
                    0.2953 * emph +
                    -5.3854 * sizeTerm +
                    0.377  * partSolid +
                    -0.1276 * nonSolid +
                    0.6581 * upper +
                    -0.0824 * (inputs.noduleCount - 4) +
                    0.7729 * spic +
                    -6.7892

        val odds = exp(logit)
        return odds / (1.0 + odds)
    }

    /** Pomocná kategorizácia podľa prahu (default 5 %) */
    fun category(prob: Double, highRiskCutoffPercent: Double = 5.0): RiskCategory =
        if (prob * 100.0 >= highRiskCutoffPercent) RiskCategory.HIGH else RiskCategory.LOW
}

enum class RiskCategory { LOW, HIGH }

// --- Jednoduché sanity testy ---
// 4 mm, muž, 30 r., bez rizík -> ~0.06 % (LOW)
// 8 mm, žena, 65 r., part-solid, horný lalok, spikulácie -> ~28 % (HIGH)
