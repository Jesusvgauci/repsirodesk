package calculators

import kotlin.math.roundToInt
import kotlin.math.max

// --- DLCO (Hb korekcia) ---

enum class Sex { Male, Female }

data class DlcoCorrectionResult(
    val correctedAbsolute: Double,   // upravená absolútna DLCO
    val correctedPercent: Double?,   // upravená DLCO v % pred (ak je dostupná predikcia)
    val summary: String              // text na zobrazenie/kopírovanie
)

/**
 * Vstup Hb môže byť v g/L (bežne 100–180) alebo v g/dL (10–18).
 * Heuristika: ak hb > 30, berieme to ako g/L a delíme 10.
 * Hb_ref: 14.6 g/dL (muž), 13.4 g/dL (žena).
 *
 * correctedAbs = DLCO_meas * (Hb_ref / Hb_g/dL)
 * correctedPct = (correctedAbs / DLCO_pred) * 100   (ak je DLCO_pred zadaná)
 */
fun computeDlcoCorrection(
    hb: Double?,
    sex: Sex,
    dlcoMeas: Double?,
    dlcoPred: Double?
): DlcoCorrectionResult? {
    if (hb == null || dlcoMeas == null) return null

    val hb_gPerdL = if (hb > 30.0) hb / 10.0 else hb
    val hbRef = if (sex == Sex.Male) 14.6 else 13.4

    val correctedAbs = dlcoMeas * (hbRef / hb_gPerdL)
    val correctedPct = dlcoPred?.let { pred -> if (pred > 0.0) (correctedAbs / pred) * 100.0 else null }

    fun r1(x: Double) = ((x * 10).roundToInt() / 10.0)

    val summary = buildString {
        append("Upravená DLCO (absolútne): ${r1(correctedAbs)}")
        if (correctedPct != null) append("\nUpravená DLCO (% pred): ${r1(correctedPct)} %")
    }

    return DlcoCorrectionResult(
        correctedAbsolute = correctedAbs,
        correctedPercent = correctedPct,
        summary = summary
    )
}

// --- PPO (segmentová metóda) ---

data class PpoResult(
    val ppoFev1Pct: Double?,
    val ppoDlcoPct: Double?,
    val summary: String
)

/**
 * Segmentová metóda (19 segmentov):
 * - Funkčné segmenty pred operáciou = 19 - obštruované
 * - Zostávajúce funkčné po operácii = funkčné - odstránené (nie pod 0)
 * - Faktor zachovania = zostávajúce / funkčné
 * - ppoFEV1% = východiskové FEV1% * faktor (ak je FEV1% dostupné)
 * - ppoDLCO% = východiskové DLCO% * faktor (ak je DLCO% dostupné)
 */
fun computePpoSegmentMethod(
    removedSegments: Int,
    obstructedSegments: Int?,
    fev1Pct: Double?,
    dlcoPct: Double?
): PpoResult {
    val total = 19.0
    val obstructed = (obstructedSegments ?: 0).coerceIn(0, 19).toDouble()
    val functionalBefore = (total - obstructed).coerceAtLeast(0.0)
    val remaining = max(0.0, functionalBefore - removedSegments)
    val factor = if (functionalBefore > 0) remaining / functionalBefore else 0.0

    val ppoFev = fev1Pct?.let { it * factor }
    val ppoDlco = dlcoPct?.let { it * factor }

    fun r1(x: Double) = ((x * 10).roundToInt() / 10.0)

    val summary = buildString {
        append("Funkčné segmenty pred operáciou: ${functionalBefore.toInt()} z 19; ")
        append("zostávajúce po resekcii: ${remaining.toInt()} (faktor ${r1(factor * 100)} %).")
        if (ppoFev != null) append("\nppoFEV₁ ≈ ${r1(ppoFev)} % pred")
        if (ppoDlco != null) append("\nppoDLCO ≈ ${r1(ppoDlco)} % pred")
    }

    return PpoResult(ppoFev1Pct = ppoFev, ppoDlcoPct = ppoDlco, summary = summary)
}
