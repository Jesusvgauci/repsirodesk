package calculators

import kotlin.math.abs

data class ABGAnalysisResult(
    val primaryDisorder: String,
    val expectedPaCO2_kPa: Double? = null,
    val expectedHCO3: Double? = null,
    val anionGap: Double? = null,
    val anionGapCorrected: Double? = null,
    val notes: List<String> = emptyList()
)

/**
 * Kompletná analýza ABR s kompenzáciou
 */
fun analyzeABG(
    ph: Double,
    paCO2_kPa: Double,
    hco3: Double,
    na: Double? = null,
    cl: Double? = null,
    albumin_g_per_L: Double? = null,
    respType: Int? = null // 0 = akútna, 1 = chronická
): ABGAnalysisResult {

    val KPA_TO_MMHG = 7.50062
    val MMHG_TO_KPA = 1.0 / KPA_TO_MMHG
    val pco2_mmHg = paCO2_kPa * KPA_TO_MMHG

    val acidemia = ph < 7.35
    val alkalemia = ph > 7.45
    var primary = "Nešpecifikované"
    val notes = mutableListOf<String>()
    var expectedPco2Kpa: Double? = null
    var expectedHco3: Double? = null

    if (acidemia) {
        primary = if (pco2_mmHg > 45 && hco3 >= 22.0) "Respiračná acidóza"
        else "Metabolická acidóza"
    } else if (alkalemia) {
        primary = if (pco2_mmHg < 35 && hco3 <= 26.0) "Respiračná alkalóza"
        else "Metabolická alkalóza"
    } else {
        primary = when {
            pco2_mmHg > 45 && hco3 > 26 -> "Kompenzovaná respiračná acidóza"
            pco2_mmHg < 35 && hco3 < 22 -> "Kompenzovaná respiračná alkalóza"
            else -> "Možná zmiešaná porucha"
        }
        notes += "pH v referenčnom rozmedzí – zvaž zmiešanú poruchu."
    }

    when (primary) {
        "Metabolická acidóza" -> {
            val exp_mmHg = 1.5 * hco3 + 8.0
            expectedPco2Kpa = exp_mmHg * MMHG_TO_KPA
            val low = exp_mmHg - 2
            val high = exp_mmHg + 2
            when {
                pco2_mmHg < low -> notes += "pCO₂ < Winter – pridružená respiračná alkalóza?"
                pco2_mmHg > high -> notes += "pCO₂ > Winter – pridružená respiračná acidóza?"
                else -> notes += "Respiračná kompenzácia primeraná (Winter)."
            }
        }
        "Metabolická alkalóza" -> {
            val exp_mmHg = 40.0 + 0.7 * (hco3 - 24.0)
            expectedPco2Kpa = exp_mmHg * MMHG_TO_KPA
            val low = exp_mmHg - 5
            val high = exp_mmHg + 5
            when {
                pco2_mmHg < low -> notes += "pCO₂ nižšia – pridružená respiračná alkalóza?"
                pco2_mmHg > high -> notes += "pCO₂ vyššia – pridružená respiračná acidóza?"
                else -> notes += "Respiračná kompenzácia primeraná."
            }
        }
        "Respiračná acidóza" -> {
            val deltaPco2 = pco2_mmHg - 40.0
            expectedHco3 = if (respType == 1) 24 + 3.5 * (deltaPco2 / 10.0)
            else 24 + 1.0 * (deltaPco2 / 10.0)
            when (respType) {
                0 -> if (hco3 > expectedHco3!! + 2) notes += "↑HCO₃ vyššie – pridružená metabolická alkalóza?"
                else notes += "Akútna resp. acidóza – HCO₃ v norme pre akútnu kompenzáciu."
                1 -> if (hco3 < expectedHco3!! - 2) notes += "HCO₃ nižší – pridružená metabolická acidóza?"
                else notes += "Chronická resp. acidóza – kompenzácia primeraná."
                null -> notes += "Uveď akútna/chronická pre presné posúdenie."
            }
        }
        "Respiračná alkalóza" -> {
            val deltaPco2 = 40.0 - pco2_mmHg
            expectedHco3 = if (respType == 1) 24 - 5 * (deltaPco2 / 10.0)
            else 24 - 2 * (deltaPco2 / 10.0)
            when (respType) {
                0 -> if (hco3 < expectedHco3!! - 2) notes += "HCO₃ nižší – pridružená metabolická acidóza?"
                else notes += "Akútna resp. alkalóza – kompenzácia primeraná."
                1 -> if (hco3 > expectedHco3!! + 2) notes += "HCO₃ vyšší – pridružená metabolická alkalóza?"
                else notes += "Chronická resp. alkalóza – kompenzácia primeraná."
                null -> notes += "Uveď akútna/chronická pre presné posúdenie."
            }
        }
    }

    // AG
    var ag: Double? = null
    var agCorr: Double? = null
    if (na != null && cl != null) {
        ag = na - cl - hco3
        if (albumin_g_per_L != null) {
            val alb_g_dL = albumin_g_per_L / 10.0
            agCorr = ag + 2.5 * (4.0 - alb_g_dL)
        }
    }

    return ABGAnalysisResult(
        primaryDisorder = primary,
        expectedPaCO2_kPa = expectedPco2Kpa,
        expectedHCO3 = expectedHco3,
        anionGap = ag,
        anionGapCorrected = agCorr,
        notes = notes
    )
}
