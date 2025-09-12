package calculators

import kotlin.math.pow
import kotlin.math.roundToInt

// ⬇️ Multiplatform-safe formátovanie: 12.34 -> fixed(1) == "12.3"
private fun Double.fixed(digits: Int): String {
    require(digits >= 0) { "digits must be >= 0" }
    val factor = 10.0.pow(digits).toInt()
    val scaled = (this * factor).roundToInt()  // bezpečné zaokrúhlenie
    val whole = scaled / factor
    val frac = scaled % factor
    return if (digits == 0) {
        "$whole"
    } else {
        "$whole.${frac.toString().padStart(digits, '0')}"
    }
}

data class FleischnerResult(
    val recommendation: String,
    val notes: List<String> = emptyList()
)

fun evaluateFleischner(
    sizeMm: Int,
    type: String,
    highRisk: Boolean,
    isMultiple: Boolean = false,
    solidComponentMm: Int? = null,
    suspiciousFeatures: Boolean = false,
    thickSlice: Boolean = false,
    incompleteCT: Boolean = false,
    ageUnder35: Boolean = false,
    knownMalignancy: Boolean = false,
    immunocompromised: Boolean = false,
    lungScreeningPopulation: Boolean = false,
    brockRiskProbability: Double? = null,
    brockHighRiskCutoffPercent: Double = 5.0
): FleischnerResult {

    val t = type.trim().lowercase()
    val s = sizeMm.coerceAtLeast(0)

    val generalNotes = mutableListOf<String>()

    // Ak je Brock riziko k dispozícii, prepisuje boolean highRisk
    val highRiskFromBrock = brockRiskProbability?.let { (it * 100.0) >= brockHighRiskCutoffPercent }
    val effectiveHighRisk = highRiskFromBrock ?: highRisk

    brockRiskProbability?.let {
        generalNotes += "Brock riziko: ${(it * 100).fixed(1)} % " +
                if (effectiveHighRisk) "(HIGH ≥ ${brockHighRiskCutoffPercent.toInt()} %)"
                else "(LOW < ${brockHighRiskCutoffPercent.toInt()} %)"
    }

    // Technické poznámky
    if (thickSlice && s >= 6) {
        generalNotes += "Hrubé rezy: odporúčaná CT hrudníka s tenkými rezmi (≤1.5 mm)."
    }
    if (incompleteCT) {
        if (s > 8 || suspiciousFeatures) {
            generalNotes += "Neúplné CT: pri >8 mm alebo podozrivom náleze doplň kompletné CT hrudníka."
        } else {
            generalNotes += "Neúplné CT: pri ≤8 mm možno postupovať podľa Fleischner, ale preferuj kompletné CT."
        }
    }

    fun rec(text: String, extra: List<String> = emptyList()): FleischnerResult {
        return FleischnerResult(
            text,
            notes = generalNotes + extra + listOf(
                "Meraj priemer (dlhá+krátka os)/2, celé mm.",
                "Za rast považuj ≥2 mm alebo ≥25 % objemu."
            )
        )
    }

    // --- Logika podľa Fleischner (s použitím effectiveHighRisk) ---

    if (t == "solid") {
        return if (!isMultiple) {
            when {
                s < 6 -> if (effectiveHighRisk)
                    rec("Solídny uzol <6 mm: voliteľné CT o 12 mesiacov (vysoké riziko).")
                else
                    rec("Solídny uzol <6 mm: kontrola sa nevyžaduje (nízke riziko).")

                s in 6..8 -> if (effectiveHighRisk)
                    rec("Solídny uzol 6–8 mm: CT o 6–12 mesiacov, potom CT o 18–24 mesiacov (vysoké riziko).")
                else
                    rec("Solídny uzol 6–8 mm: CT o 6–12 mesiacov, zvážiť CT o 18–24 mesiacov (nízke riziko).")

                else -> rec("Solídny uzol >8 mm: CT o 3 mesiace, PET-CT alebo biopsia.")
            }
        } else {
            when {
                s < 6 -> if (effectiveHighRisk)
                    rec("Viacnásobné solídne uzly <6 mm: voliteľné CT o 12 mesiacov.")
                else
                    rec("Viacnásobné solídne uzly <6 mm: kontrola sa nevyžaduje.")

                s in 6..8 -> if (effectiveHighRisk)
                    rec("Viacnásobné solídne uzly 6–8 mm: CT o 3–6 mesiacov, potom 18–24 mesiacov.")
                else
                    rec("Viacnásobné solídne uzly 6–8 mm: CT o 3–6 mesiacov, zvážiť 18–24 mesiacov.")

                else -> rec("Viacnásobné solídne uzly >8 mm: CT o 3–6 mesiacov, potom 18–24 mesiacov; riadiť sa najpodozrivejším.")
            }
        }
    }

    if (t == "ground-glass" || t == "ggn" || t == "groundglass") {
        return if (!isMultiple) {
            if (s < 6)
                rec("Jeden ground-glass uzol <6 mm: kontrola sa nevyžaduje.")
            else
                rec("Jeden ground-glass uzol ≥6 mm: CT o 6–12 mesiacov; ak pretrváva, CT každé 2 roky do 5 rokov.")
        } else {
            if (s < 6)
                rec("Viacnásobné subsolídne uzly <6 mm: CT o 3–6 mesiacov; ak stabilné, sledovanie podľa rizika.")
            else
                rec("Viacnásobné subsolídne uzly ≥6 mm: CT o 3–6 mesiacov; ďalší postup podľa najpodozrivejšieho.")
        }
    }

    if (t == "part-solid" || t == "partsolid") {
        return if (!isMultiple) {
            if (s < 6)
                rec("Jeden part-solid uzol <6 mm: sledovanie sa zvyčajne nevyžaduje.")
            else {
                val sc = solidComponentMm
                if (sc == null)
                    rec("Jeden part-solid uzol ≥6 mm: CT o 3–6 mesiacov; ďalší postup závisí od veľkosti solidnej časti.")
                else if (sc < 6)
                    rec("Jeden part-solid uzol ≥6 mm so solidnou časťou <6 mm: CT o 3–6 mesiacov; ak pretrváva, ročné CT do 5 rokov.")
                else
                    rec("Jeden part-solid uzol so solidnou časťou ≥6 mm: zváž PET-CT alebo biopsiu.")
            }
        } else {
            if (s < 6)
                rec("Viacnásobné subsolídne uzly <6 mm: CT o 3–6 mesiacov; ak stabilné, zvážiť CT o 2 a 4 roky.")
            else
                rec("Viacnásobné subsolídne uzly ≥6 mm: CT o 3–6 mesiacov; ďalší postup podľa najpodozrivejšieho.")
        }
    }

    return rec("Neznámy typ nodulu – uveď „solid“, „ground-glass“ alebo „part-solid“.")
}
