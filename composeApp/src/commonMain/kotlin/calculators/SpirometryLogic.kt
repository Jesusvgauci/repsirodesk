package calculators

import kotlin.math.abs

private const val LLN = -1.645
private const val ULN = 1.645

private fun severityFromZ(z: Double): String = when {
    z >= -2.5 -> "mierneho stupňa"
    z >= -3.5 -> "stredne ťažkého stupňa"
    else -> "ťažkého stupňa"
}

fun interpretValues(
    fev1: Double?, fvc: Double?, ratio: Double?,
    tlc: Double?, rvTlc: Double?, dlco: Double?,
    kco: Double?, va: Double?, sRaw: Double?
): String {
    if (fev1 == null || fvc == null || ratio == null) {
        return "Na vyhodnotenie je potrebné zadať aspoň FEV₁, FVC a FEV₁/FVC."
    }

    val hasObstruction = ratio < LLN
    val hasRestriction = tlc?.let { it < LLN } ?: false
    val lowFVC = fvc < LLN
    val lowFEV1 = fev1 < LLN
    val normalFEV1 = fev1 >= LLN
    val normalFVC = fvc >= LLN
    val normalRatio = ratio >= LLN
    val normalTLC = tlc?.let { it >= LLN } ?: false

    val vent = when {
        // kombinovaná – iba skrátený zápis + stupeň podľa horšieho z FEV1 vs TLC
        hasObstruction && hasRestriction -> {
            val worse = if (tlc != null && abs(tlc) > abs(fev1)) tlc else fev1
            val sev = severityFromZ(worse)
            "kombinovaná ventilačná porucha $sev"
        }
        // incipientná obštrukcia pri normálnych FEV1 aj FVC
        hasObstruction && normalFEV1 && normalFVC ->
            "incipientná obštrukčná ventilačná porucha"
        // obštrukcia
        hasObstruction -> {
            val sev = severityFromZ(fev1)
            "obštrukčná ventilačná porucha $sev"
        }
        // restrikcia – podtyp podľa RV/TLC; ak chýba, bez podtypu
        hasRestriction -> {
            val sev = severityFromZ(tlc!!)
            when {
                rvTlc == null -> "restrikčná ventilačná porucha $sev"
                rvTlc > ULN -> "komplexná restrikčná ventilačná porucha $sev"
                else -> "jednoduchá restrikčná ventilačná porucha $sev"
            }
        }
        // možná restrikcia pri nízkej FVC, normálnom pomere a chýbajúcom TLC
        normalRatio && lowFVC && tlc == null ->
            "možná reštrikcia – odporúčané doplniť bodypletyzmografiu"
        // nešpecifický vzor: pomer v norme, (FEV1 alebo FVC < LLN) a TLC v norme
        normalRatio && (lowFEV1 || lowFVC) && normalTLC ->
            "nešpecifický vzor"
        else -> "bez ventilačnej poruchy"
    }

    // Hyperinflácia podľa RV/TLC – textový formát podľa tvojich pravidiel
    val hyper = when {
        rvTlc == null -> "hyperinflácia nehodnotená"
        rvTlc > ULN -> "s hyperinfláciou"
        else -> "bez hyperinflácie"
    }

    // sRaw: > 1.2 zvýšené, inak v norme, ak chýba – nehodnotené
    val resist = when {
        sRaw == null -> "odpory nehodnotené"
        sRaw > 1.2 -> "zvýšené odpory v dýchacích cestách"
        else -> "bez zvýšených odporov v dýchacích cestách"
    }

    fun gradeDlco(z: Double) = when {
        z > ULN -> "DLCO abnormálne vysoké"
        z < LLN && z >= -2.5 -> "DLCO mierne redukované"
        z < -2.5 && z >= -3.5 -> "DLCO stredne ťažko redukované"
        z < -3.5 -> "DLCO ťažko redukované"
        else -> "DLCO v norme"
    }

    fun flag(name: String, z: Double?) = when {
        z == null -> "$name nehodnotené"
        z > ULN -> "$name zvýšené"
        z < LLN -> "$name znížené"
        else -> "$name v norme"
    }

    val dlcoPart = dlco?.let(::gradeDlco) ?: "DLCO nehodnotené"
    val kcoPart = flag("KCO", kco)
    val vaPart = flag("VA", va)

    val allNormal =
        vent == "bez ventilačnej poruchy" &&
                (rvTlc == null || rvTlc in LLN..ULN) &&
                (sRaw == null || sRaw <= 1.2) &&
                (dlco == null || dlco in LLN..ULN) &&
                (kco == null || kco in LLN..ULN) &&
                (va == null || va in LLN..ULN) &&
                fev1 >= LLN && fvc >= LLN && ratio >= LLN &&
                (tlc == null || tlc >= LLN)

    if (allNormal) {
        // špeciálna veta pre úplne normálny nález (presná formulácia)
        return "Zadané údaje svedčia pre normálnu funkciu pľúc bez známok ventilačnej poruchy, hyperinflácie, poruchy odporov či difúzie."
    }

    // jednotná veta: ventilačný nález → hyperinflácia → odpory → difúzia (DLCO, KCO, VA)
    return buildString {
        append("Vo funkčnom vyšetrení pľúc ")
        append(vent); append(", ")
        append(hyper); append(", ")
        append(resist); append(", ")
        append(dlcoPart); append(", ")
        append(kcoPart); append(", ")
        append(vaPart); append(".")
    }
}
