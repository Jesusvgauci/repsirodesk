package calculators

data class mMRCResult(
    val score: Int,
    val description: String
)

/**
 * mMRC Dyspnoe Scale (0–4).
 */
fun evaluatemMRC(score: Int): mMRCResult {
    val description = when (score) {
        0 -> "Dyspnoe len pri veľmi namáhavej námahe."
        1 -> "Dyspnoe pri rýchlej chôdzi alebo miernom stúpaní."
        2 -> "Chôdza pomalšia než rovesníci, prestávky pri chôdzi v rovine."
        3 -> "Po 100 metroch alebo pár minútach chôdze musí zastaviť."
        4 -> "Dyspnoe aj pri obliekaní alebo v pokoji."
        else -> "Neplatné skóre."
    }
    return mMRCResult(score, description)
}
