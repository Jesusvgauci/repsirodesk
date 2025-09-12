package calculators

import kotlin.math.roundToInt

// ---------------- Pomocné ----------------------------------------------------

/** Ak je zadané v %, prepočítaj na pomer; ak je už pomer (0.70), nechaj tak. */
private fun normalizeRatio(x: Double?): Double? =
    x?.let { if (it > 1.5) it / 100.0 else it }  // 70 -> 0.70; 0.70 zostáva 0.70

private const val Z_LLN = -1.645

/**
 * Klasifikácia obštrukcie: preferuje z-skóre (LLN −1.645). Ak chýba, padá na absolútny pomer (hranica 0.70).
 */
private fun classifyObstructionLLN(
    ratioZPre: Double?, ratioZPost: Double?,
    ratioPreAbsOrPct: Double?, ratioPostAbsOrPct: Double?,
    bdPositive: Boolean
): String? {
    // 1) Prefer z-skóre
    if (ratioZPre != null && ratioZPost != null) {
        val preObstr = ratioZPre < Z_LLN
        val postObstr = ratioZPost < Z_LLN
        return when {
            !preObstr && !postObstr -> "bez obštrukcie"
            preObstr && !postObstr && bdPositive -> "reverzibilnú obštrukciu"
            else -> "ireverzibilnú obštrukciu"
        }
    }

    // 2) Fallback: absolútny pomer (%, alebo 0.70)
    val pre = normalizeRatio(ratioPreAbsOrPct)
    val post = normalizeRatio(ratioPostAbsOrPct)
    if (pre == null || post == null) return null

    val cut = 0.70
    return when {
        pre >= cut && post >= cut -> "bez obštrukcie"
        pre < cut && post >= cut && bdPositive -> "reverzibilnú obštrukciu"
        else -> "ireverzibilnú obštrukciu"
    }
}

// ---------------- ERS/ATS 2022 ----------------------------------------------
// Pozitívny, ak ΔFEV1 alebo ΔFVC > 10 % z PREDIKOVANEJ hodnoty.
fun interpretBronchodilationTest(
    fev1Pre: Double?, fev1Post: Double?, fev1Pred: Double?,
    fvcPre: Double?,  fvcPost: Double?,  fvcPred: Double?,
    // voliteľné: buď z-skóre alebo absolútny pomer (v % alebo 0.70)
    ratioPre: Double? = null, ratioPost: Double? = null,
    ratioZPre: Double? = null, ratioZPost: Double? = null
): String {
    if (fev1Pre == null || fev1Post == null || fev1Pred == null) {
        return "Na ERS/ATS 2022 zadaj FEV₁ pred, FEV₁ po a FEV₁ predikovanú (v litroch)."
    }

    fun pcOfPred(pre: Double, post: Double, pred: Double) = ((post - pre) / pred) * 100.0
    fun fmt1(x: Double) = ((x * 10).roundToInt() / 10.0).toString()

    val dFev1 = pcOfPred(fev1Pre, fev1Post, fev1Pred)
    val dFvc = if (fvcPre != null && fvcPost != null && fvcPred != null)
        pcOfPred(fvcPre, fvcPost, fvcPred) else null

    val positive = dFev1 > 10.0 || (dFvc != null && dFvc > 10.0)
    val verdict = if (positive) "BD test: pozitívny" else "BD test: negatívny"

    val fvcPart = dFvc?.let { "ΔFVC = ${fmt1(it)} % pred" } ?: "ΔFVC = —"
    val obs = classifyObstructionLLN(ratioZPre, ratioZPost, ratioPre, ratioPost, positive)
    val obsPart = obs?.let { " Záver podľa pomeru: $it." } ?: ""

    return "$verdict. ΔFEV₁ = ${fmt1(dFev1)} % pred, $fvcPart.$obsPart"
}

// ---------------- ATS/ERS 2005 ----------------------------------------------
// Pozitívny, ak ΔFEV1 alebo ΔFVC súčasne ≥ 12 % a ≥ 200 ml.
fun evaluateBd2005(
    fev1Pre: Double?, fev1Post: Double?,
    fvcPre: Double?,  fvcPost: Double?,
    // voliteľné: buď z-skóre alebo absolútny pomer
    ratioPre: Double? = null, ratioPost: Double? = null,
    ratioZPre: Double? = null, ratioZPost: Double? = null
): String {
    if (fev1Pre == null || fev1Post == null) {
        return "Na kritérium 2005 zadaj aspoň FEV₁ pred a po (v litroch). FVC je voliteľná."
    }

    fun dMl(pre: Double, post: Double) = (post - pre) * 1000.0
    fun dPct(pre: Double, post: Double) = ((post - pre) / pre) * 100.0
    fun r0(x: Double) = x.roundToInt().toString()
    fun r1(x: Double) = ((x * 10).roundToInt() / 10.0).toString()

    val dFevMl = dMl(fev1Pre, fev1Post)
    val dFevPct = dPct(fev1Pre, fev1Post)

    val hasFvc = (fvcPre != null && fvcPost != null)
    val dFvcMl = if (hasFvc) dMl(fvcPre!!, fvcPost!!) else null
    val dFvcPct = if (hasFvc) dPct(fvcPre!!, fvcPost!!) else null

    val posFev = dFevPct >= 12.0 && dFevMl >= 200.0
    val posFvc = (dFvcPct != null && dFvcMl != null && dFvcPct >= 12.0 && dFvcMl >= 200.0)
    val positive = posFev || posFvc

    val verdict = if (positive) "BD test (2005): pozitívny" else "BD test (2005): negatívny"
    val fvcPart = if (dFvcMl != null && dFvcPct != null)
        "ΔFVC = ${r0(dFvcMl)} ml (${r1(dFvcPct)} %)"
    else "ΔFVC = —"

    val obs = classifyObstructionLLN(ratioZPre, ratioZPost, ratioPre, ratioPost, positive)
    val obsPart = obs?.let { " Záver podľa pomeru: $it." } ?: ""

    return "$verdict. ΔFEV₁ = ${r0(dFevMl)} ml (${r1(dFevPct)} %), $fvcPart.$obsPart"
}
