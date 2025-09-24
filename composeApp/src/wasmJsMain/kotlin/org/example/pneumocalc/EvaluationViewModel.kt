package org.example.pneumocalc

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import calculators.*  // <- importuje aj Sex z calculators

class EvaluationViewModel {

    // ---------------- z-skóre (ATS/ERS 2022) ----------------
    val fev1Z = MutableStateFlow("")
    val fvcZ  = MutableStateFlow("")
    val ratioZ = MutableStateFlow("")
    val tlcZ  = MutableStateFlow("")
    val rvTlcZ = MutableStateFlow("")
    val dlcoZ = MutableStateFlow("")
    val kcoZ  = MutableStateFlow("")
    val vaZ   = MutableStateFlow("")
    val sRaw  = MutableStateFlow("")

    private val _zscoreResult = MutableStateFlow("")
    val zscoreResult: StateFlow<String> = _zscoreResult.asStateFlow()

    fun calculateZscore() {
        _zscoreResult.value = interpretValues(
            fev1  = fev1Z.value.toDoubleOrNull(),
            fvc   = fvcZ.value.toDoubleOrNull(),
            ratio = ratioZ.value.toDoubleOrNull(),
            tlc   = tlcZ.value.toDoubleOrNull(),
            rvTlc = rvTlcZ.value.toDoubleOrNull(),
            dlco  = dlcoZ.value.toDoubleOrNull(),
            kco   = kcoZ.value.toDoubleOrNull(),
            va    = vaZ.value.toDoubleOrNull(),
            sRaw  = sRaw.value.toDoubleOrNull()
        )
        addToHistory("Spirometria (z-skóre)", currentZInputs(), _zscoreResult.value)
    }

    // ---------------- % pred (ERS 2005) ----------------
    val fev1Pct = MutableStateFlow("")
    val fvcPct  = MutableStateFlow("")
    val ratioAbs = MutableStateFlow("")
    val tlcPct  = MutableStateFlow("")
    val rvTlcPct = MutableStateFlow("")
    val dlcoPct = MutableStateFlow("")
    val kcoPct  = MutableStateFlow("")
    val vaPct   = MutableStateFlow("")

    private val _percentResult = MutableStateFlow("")
    val percentResult: StateFlow<String> = _percentResult.asStateFlow()

    fun calculatePercent() {
        _percentResult.value = interpretValuesPercent(
            fev1Pct = fev1Pct.value.toDoubleOrNull(),
            fvcPct  = fvcPct.value.toDoubleOrNull(),
            ratioPct = ratioAbs.value.toDoubleOrNull(),
            tlcPct  = tlcPct.value.toDoubleOrNull(),
            rvTlcPct = rvTlcPct.value.toDoubleOrNull(),
            dlcoPct = dlcoPct.value.toDoubleOrNull(),
            kcoPct  = kcoPct.value.toDoubleOrNull(),
            vaPct   = vaPct.value.toDoubleOrNull(),
            sRaw    = sRaw.value.toDoubleOrNull()
        )
        addToHistory("Spirometria (% pred)", currentPercentInputs(), _percentResult.value)
    }

    // ---------------- BD test ----------------
    val fev1Pre  = MutableStateFlow("")
    val fev1Post = MutableStateFlow("")
    val fev1Pred = MutableStateFlow("")
    val fvcPre   = MutableStateFlow("")
    val fvcPost  = MutableStateFlow("")
    val fvcPred  = MutableStateFlow("")
    val ratioZPre  = MutableStateFlow("")
    val ratioZPost = MutableStateFlow("")

    private val _bd2022 = MutableStateFlow("")
    val bd2022: StateFlow<String> = _bd2022.asStateFlow()

    private val _bd2005 = MutableStateFlow("")
    val bd2005: StateFlow<String> = _bd2005.asStateFlow()

    fun calculateBd2022() {
        val base = interpretBronchodilationTest(
            fev1Pre  = fev1Pre.value.toDoubleOrNull(),
            fev1Post = fev1Post.value.toDoubleOrNull(),
            fev1Pred = fev1Pred.value.toDoubleOrNull(),
            fvcPre   = fvcPre.value.toDoubleOrNull(),
            fvcPost  = fvcPost.value.toDoubleOrNull(),
            fvcPred  = fvcPred.value.toDoubleOrNull()
        )

        val note = classifyObstructionReversibility(
            preZ  = ratioZPre.value.toDoubleOrNull(),
            postZ = ratioZPost.value.toDoubleOrNull()
        )

        _bd2022.value = listOf(base, note).filter { it.isNotBlank() }.joinToString(" ")
        addToHistory(
            "BD test (2022)",
            currentBdInputs(),
            _bd2022.value
        )
    }

    private fun classifyObstructionReversibility(preZ: Double?, postZ: Double?): String {
        val LLN_Z = -1.645
        return when {
            preZ == null && postZ == null -> ""
            preZ != null && postZ == null ->
                if (preZ < LLN_Z)
                    "Východiskovo prítomná obštrukcia (FEV₁/FVC < LLN); reverzibilitu bez post-hodnoty nehodnotená."
                else
                    "Východiskovo bez obštrukcie (FEV₁/FVC ≥ LLN)."
            preZ == null && postZ != null ->
                "Reverzibilitu obštrukcie nevieme posúdiť (chýba východiskový pomer)."
            else -> {
                val pre = preZ!!
                val post = postZ!!
                when {
                    pre < LLN_Z && post >= LLN_Z -> "Reverzibilná obštrukcia (pomer po BD ≥ LLN)."
                    pre < LLN_Z && post < LLN_Z  -> "Ireverzibilná obštrukcia (pomer po BD < LLN)."
                    pre >= LLN_Z && post >= LLN_Z -> "Bez obštrukcie (pomer pred aj po BD ≥ LLN)."
                    pre >= LLN_Z && post < LLN_Z  -> "Obštrukcia sa objavila po BD (neobvyklé; skontroluj kvalitu merania)."
                    else -> ""
                }
            }
        }
    }

    fun calculateBd2005() {
        val base = evaluateBd2005(
            fev1Pre  = fev1Pre.value.toDoubleOrNull(),
            fev1Post = fev1Post.value.toDoubleOrNull(),
            fvcPre   = fvcPre.value.toDoubleOrNull(),
            fvcPost  = fvcPost.value.toDoubleOrNull()
        )

        val note = classifyObstructionReversibility(
            preZ  = ratioZPre.value.toDoubleOrNull(),
            postZ = ratioZPost.value.toDoubleOrNull()
        )

        _bd2005.value = listOf(base, note).filter { it.isNotBlank() }.joinToString(" ")
        addToHistory(
            "BD test (2005)",
            currentBdInputs(),
            _bd2005.value
        )
    }

    // ---------------- DLCO (Hb korekcia) ----------------
    // použijeme calculators.Sex, aby pasovalo do computeDlcoCorrection
    val hb = MutableStateFlow("")
    val dlcoMeasured = MutableStateFlow("")
    val dlcoPredicted = MutableStateFlow("")
    val sex = MutableStateFlow(Sex.Male)

    private val _dlco = MutableStateFlow<DlcoCorrectionResult?>(null)
    val dlco: StateFlow<DlcoCorrectionResult?> = _dlco.asStateFlow()

    fun calculateDlco() {
        _dlco.value = computeDlcoCorrection(
            hb       = hb.value.toDoubleOrNull(),
            sex      = sex.value,
            dlcoMeas = dlcoMeasured.value.toDoubleOrNull(),
            dlcoPred = dlcoPredicted.value.toDoubleOrNull()
        )
        addToHistory(
            "DLCO (Hb)",
            "Hb=${hb.value}, DLCOm=${dlcoMeasured.value}, DLCOpred=${dlcoPredicted.value}",
            _dlco.value?.summary ?: ""
        )
    }

    // ---------------- PPO (segmentová metóda) ----------------
    private val _ppo = MutableStateFlow<PpoResult?>(null)
    val ppo: StateFlow<PpoResult?> = _ppo.asStateFlow()

    fun calculatePpo(removedSegments: Int, obstructedSegments: Int?, fev1PctBase: Double?, dlcoPctBase: Double?) {
        _ppo.value = computePpoSegmentMethod(
            removedSegments = removedSegments,
            obstructedSegments = obstructedSegments,
            fev1Pct = fev1PctBase,
            dlcoPct = dlcoPctBase
        )
        addToHistory("PPO", "removed=$removedSegments, obstructed=$obstructedSegments, FEV1%=$fev1PctBase, DLCO%=$dlcoPctBase", _ppo.value?.summary ?: "")
    }

    // ---------------- História ----------------
    data class HistoryEntry(
        val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
        val type: String,
        val inputs: String,
        val result: String
    )

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    private fun addToHistory(type: String, inputs: String, result: String) {
        val e = HistoryEntry(type = type, inputs = inputs, result = result)
        _history.value = listOf(e) + _history.value.take(9)
    }

    private fun currentZInputs() = listOf(
        "FEV1=${fev1Z.value}", "FVC=${fvcZ.value}", "FEV1/FVC=${ratioZ.value}",
        "TLC=${tlcZ.value}", "RV/TLC=${rvTlcZ.value}", "DLCO=${dlcoZ.value}",
        "KCO=${kcoZ.value}", "VA=${vaZ.value}", "sRaw=${sRaw.value}"
    ).joinToString(", ")

    private fun currentPercentInputs() = listOf(
        "FEV1%=${fev1Pct.value}", "FVC%=${fvcPct.value}", "ratio=${ratioAbs.value}",
        "TLC%=${tlcPct.value}", "RV/TLC%=${rvTlcPct.value}",
        "DLCO%=${dlcoPct.value}", "KCO%=${kcoPct.value}", "VA%=${vaPct.value}", "sRaw=${sRaw.value}"
    ).joinToString(", ")

    private fun currentBdInputs() = listOf(
        "FEV1pre=${fev1Pre.value}", "FEV1post=${fev1Post.value}", "FEV1pred=${fev1Pred.value}",
        "FVCpre=${fvcPre.value}",   "FVCpost=${fvcPost.value}",   "FVCpred=${fvcPred.value}",
        "ratioZpre=${ratioZPre.value}", "ratioZpost=${ratioZPost.value}"
    ).joinToString(", ")
}
