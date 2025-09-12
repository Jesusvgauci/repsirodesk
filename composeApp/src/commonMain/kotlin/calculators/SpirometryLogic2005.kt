package calculators

fun interpretValuesPercent(
    fev1Pct: Double?,
    fvcPct: Double?,
    ratioPct: Double?,   // ← TERAZ v percentách (napr. 70)
    tlcPct: Double?,
    rvTlcPct: Double?,
    dlcoPct: Double?,
    kcoPct: Double?,
    vaPct: Double?,
    sRaw: Double?
): String {
    if (fev1Pct == null || fvcPct == null || ratioPct == null) {
        return "Na vyhodnotenie % režimu zadaj FEV₁ %pred, FVC %pred a FEV₁/FVC (%)"
    }

    // ak by niekto omylom zadal 0.7 → 70.0
    val ratioPercent = if (ratioPct <= 1.2) ratioPct * 100.0 else ratioPct

    // prahy podľa tvojich pravidiel
    val obstructionCutPct = 70.0
    val normalPct = 80.0
    val hyperInflPct = 120.0

    fun obstructionSeverityLabel(p: Double): String = when {
        p >= 80      -> "incipientnú"
        p >= 70      -> "ľahkú incipientnú"
        p >= 60      -> "strednú"
        p >= 50      -> "stredne ťažkú"
        p >= 35      -> "ťažkú"
        else         -> "veľmi ťažkú"
    }
    fun restrictionSeverityLabel(p: Double): String = when {
        p >= 80      -> "ľahkú (podľa FEV₁ v norme – klinicky skôr mierny rozsah)"
        p >= 70      -> "ľahkú"
        p >= 60      -> "strednú"
        p >= 50      -> "stredne ťažkú"
        p >= 35      -> "ťažkú"
        else         -> "veľmi ťažkú"
    }
    fun dlcoStr(p: Double?) = when {
        p == null -> "DLCO nehodnotená"
        p >= 80   -> "DLCO v norme"
        p >= 60   -> "DLCO znížená – mierne"
        p >= 40   -> "DLCO znížená – stredne ťažko"
        else      -> "DLCO znížená – ťažko"
    }
    fun flagPct(name: String, p: Double?) = when {
        p == null -> "$name nehodnotená"
        p >= 80   -> "$name v norme"
        else      -> "$name znížené"
    }

    val hasObstruction = ratioPercent < obstructionCutPct
    val hasRestriction = tlcPct?.let { it < normalPct } ?: false

    val vent = when {
        hasObstruction && hasRestriction -> {
            val obs = obstructionSeverityLabel(fev1Pct)
            val res = restrictionSeverityLabel(fev1Pct)
            "kombinovanú ventilačnú poruchu – obštrukčnú ($obs) a restrikčnú ($res)"
        }
        hasObstruction -> {
            val sev = obstructionSeverityLabel(fev1Pct)
            "obštrukčnú ventilačnú poruchu ($sev; podľa FEV₁ %pred)"
        }
        !hasObstruction && tlcPct != null && tlcPct < normalPct -> {
            val sev = restrictionSeverityLabel(fev1Pct)
            "restrikčnú ventilačnú poruchu ($sev; podľa FEV₁ %pred)"
        }
        !hasObstruction && tlcPct != null && tlcPct >= normalPct &&
                (fev1Pct < normalPct || fvcPct < normalPct) -> {
            "nešpecifickú ventilačnú poruchu (pomer normálny, TLC v norme, znížené FEV₁/FVC)"
        }
        !hasObstruction && tlcPct == null && fvcPct < normalPct -> {
            "možnú restrikciu – pomer je normálny, FVC znížené; odporúča sa doplniť bodypletyzmografiu (TLC)"
        }
        else -> "bez ventilačnej poruchy"
    }

    val hyper = when {
        rvTlcPct == null        -> "hyperinflácia nehodnotená"
        rvTlcPct > hyperInflPct -> "s hyperinfláciou"
        else                    -> "bez hyperinflácie"
    }
    val resist = when {
        sRaw == null -> "odpory nehodnotené"
        sRaw > 1.2   -> "odpory zvýšené"
        else         -> "odpory v norme"
    }
    val dlcoPart = dlcoStr(dlcoPct)
    val kcoPart  = flagPct("KCO", kcoPct)
    val vaPart   = flagPct("VA",  vaPct)

    val allNormal =
        vent == "bez ventilačnej poruchy" &&
                (rvTlcPct == null || rvTlcPct <= hyperInflPct) &&
                (sRaw == null || sRaw <= 1.2) &&
                (dlcoPct == null || dlcoPct >= normalPct) &&
                (kcoPct == null || kcoPct >= normalPct) &&
                (vaPct  == null || vaPct  >= normalPct) &&
                fev1Pct >= normalPct && fvcPct >= normalPct && ratioPercent >= obstructionCutPct &&
                (tlcPct == null || tlcPct >= normalPct)

    if (allNormal) {
        return "Nález svedčí pre normálnu funkciu pľúc bez ventilačnej poruchy, hyperinflácie, zvýšených odporov či poruchy difúzie."
    }

    return buildString {
        append("Nález svedčí pre "); append(vent); append(", ")
        append(hyper); append(", ")
        append(resist); append(", ")
        append(dlcoPart); append(", ")
        append(kcoPart); append(", ")
        append(vaPart); append(".")
    }
}
