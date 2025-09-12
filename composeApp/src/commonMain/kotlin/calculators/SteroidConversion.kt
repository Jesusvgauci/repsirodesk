package calculators

data class SteroidDrug(val name: String, val equivalentDose: Double)

private val steroidTable = listOf(
    SteroidDrug("Betametazon (IV)", 0.75),
    SteroidDrug("Kortizón (PO)", 25.0),
    SteroidDrug("Dexametazon (IV/PO)", 0.75),
    SteroidDrug("Hydrokortizón (IV/PO)", 20.0),
    SteroidDrug("Metylprednizolón (IV/PO)", 4.0),
    SteroidDrug("Prednizolón (PO)", 5.0),
    SteroidDrug("Prednison (PO)", 5.0),
    SteroidDrug("Triamcinolón (IV)", 4.0)
)

/**
 * Prepočíta ekvivalentnú dávku steroidu.
 */
fun convertSteroid(inputDrug: String, inputDose: Double, outputDrug: String): Double {
    val input = steroidTable.find { it.name == inputDrug } ?: return 0.0
    val output = steroidTable.find { it.name == outputDrug } ?: return 0.0
    if (inputDose <= 0) return 0.0

    val prednisonEquivalent = inputDose * (5.0 / input.equivalentDose)
    return prednisonEquivalent * (output.equivalentDose / 5.0)
}

/** Zoznam všetkých podporovaných liekov – použije sa v UI */
fun getAllSteroidDrugs(): List<String> = steroidTable.map { it.name }
