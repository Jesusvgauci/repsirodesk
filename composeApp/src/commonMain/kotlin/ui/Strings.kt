package ui


object S {
    const val evaluate = "Vyhodnotiť"
    const val result = "Výsledok"
    const val copy = "Kopírovať"
    const val required = "Povinný údaj"
    const val invalidNumber = "Zadajte platné číslo"
    fun outOfRange(min: Double?, max: Double?) = buildString {
        append("Mimo rozsah")
        if (min != null || max != null) {
            append(" (")
            append(min?.toString() ?: "–")
            append("–")
            append(max?.toString() ?: "–")
            append(")")
        }
    }
}