// src/commonMain/kotlin/com/yourapp/tnm/Tnm9Engine.kt
package calculators

/**
 * TNM-9 Lung Cancer Staging logic
 * - User selects input conditions (size, invasion, nodal stations, metastases)
 * - Engine calculates T/N/M and clinical stage
 * - Provides human-readable labels & descriptions for UI
 */
object Tnm9Engine {

    // ------------ ENUMS WITH DESCRIPTIONS ------------

    enum class TCategory(val label: String, val description: String) {
        Tis("Tis", "Carcinoma in situ"),
        T1a("T1a", "Tumor ≤ 1 cm"),
        T1b("T1b", "Tumor > 1 cm but ≤ 2 cm"),
        T1c("T1c", "Tumor > 2 cm but ≤ 3 cm"),
        T2a("T2a", "Tumor > 3 cm but ≤ 4 cm OR invasion of visceral pleura, main bronchus involvement, atelectasis/pneumonitis"),
        T2b("T2b", "Tumor > 4 cm but ≤ 5 cm"),
        T3("T3", "Tumor > 5 cm but ≤ 7 cm OR invasion of chest wall/parietal pleura, phrenic nerve, pericardium, or separate nodules in same lobe"),
        T4("T4", "Tumor > 7 cm OR invasion of mediastinum, heart, great vessels, trachea, carina, vertebral body/canal, esophagus, or separate nodules in different ipsilateral lobe"),
    }

    enum class NCategory(val label: String) {
        N0("N0"), N1("N1"), N2a("N2a"), N2b("N2b"), N3("N3"), NX("NX")
    }

    enum class MCategory(val label: String, val description: String) {
        M0("M0", "No distant metastasis"),
        M1a("M1a", "Intrathoracic metastasis: contralateral lung nodule, pleural/pericardial nodules, or malignant effusion"),
        M1b("M1b", "Single extrathoracic metastasis"),
        M1c1("M1c1", "Multiple extrathoracic metastases in a single organ"),
        M1c2("M1c2", "Multiple extrathoracic metastases in multiple organs"),
    }

    enum class StageGroup(val label: String) {
        STAGE_0("Stage 0"),
        IA1("Stage IA1"),
        IA2("Stage IA2"),
        IA3("Stage IA3"),
        IB("Stage IB"),
        IIA("Stage IIA"),
        IIB("Stage IIB"),
        IIIA("Stage IIIA"),
        IIIB("Stage IIIB"),
        IIIC("Stage IIIC"),
        IVA("Stage IVA"),
        IVB("Stage IVB"),
        UNSTAGEABLE("Unstageable")
    }

    enum class TumorSide { RIGHT, LEFT }

    // ------------ INPUT MODELS ------------

    /** Checkboxes for T upstaging factors */
    data class TUpstaging(
        val visceralPleuralInvasion: Boolean = false,
        val mainBronchusInvolvement: Boolean = false,
        val atelectasis: Boolean = false,
        val chestWall: Boolean = false,
        val phrenicNerve: Boolean = false,
        val pericardium: Boolean = false,
        val separateNoduleSameLobe: Boolean = false,
        val diaphragm: Boolean = false,
        val mediastinum: Boolean = false,
        val heart: Boolean = false,
        val greatVessels: Boolean = false,
        val trachea: Boolean = false,
        val laryngealNerve: Boolean = false,
        val esophagus: Boolean = false,
        val vertebra: Boolean = false,
        val carina: Boolean = false,
        val separateNoduleDifferentLobe: Boolean = false,
    )

    data class TInput(val size: Double?, val up: TUpstaging = TUpstaging())
    data class NInput(val tumorSide: TumorSide, val selectedStations: Set<String>)
    data class MInput(
        val intrathoracicMet: Boolean = false,
        val extrathoracicMetCount: Int = 0,
        val extrathoracicOrgansInvolved: Int = 0
    )

    // ------------ RESULT MODEL ------------

    data class Result(
        val t: TCategory,
        val n: NCategory,
        val m: MCategory,
        val stage: StageGroup
    ) {
        fun lineTNM() = "${t.label} ${n.label} ${m.label}"
        fun clinicalStage() = stage.label
    }

    // ------------ MAIN EVALUATION ------------

    fun evaluate(tInput: TInput, nInput: NInput, mInput: MInput): Result {
        val t = calculateT(tInput)
        val n = calculateN(nInput)
        val m = calculateM(mInput)
        val stage = calculateStage(t, n, m)
        return Result(t, n, m, stage)
    }

    // ------------ T LOGIC ------------

    private fun calculateT(input: TInput): TCategory {
        val size = input.size ?: return TCategory.Tis
        var t = when {
            size <= 1.0 -> TCategory.T1a
            size <= 2.0 -> TCategory.T1b
            size <= 3.0 -> TCategory.T1c
            size <= 4.0 -> TCategory.T2a
            size <= 5.0 -> TCategory.T2b
            size <= 7.0 -> TCategory.T3
            else -> TCategory.T4
        }
        // Upstaging factors override if more severe
        val u = input.up
        if (u.diaphragm || u.mediastinum || u.heart || u.greatVessels ||
            u.trachea || u.laryngealNerve || u.esophagus || u.vertebra ||
            u.carina || u.separateNoduleDifferentLobe
        ) t = TCategory.T4
        else if (u.chestWall || u.phrenicNerve || u.pericardium || u.separateNoduleSameLobe)
            t = maxOf(t, TCategory.T3)
        else if (u.visceralPleuralInvasion || u.mainBronchusInvolvement || u.atelectasis)
            t = if (t < TCategory.T2a) TCategory.T2a else t
        return t
    }

    // ------------ N LOGIC (simplified TNM-9 mapping) ------------

    private fun calculateN(input: NInput): NCategory {
        if (input.selectedStations.isEmpty()) return NCategory.N0
        val side = input.tumorSide
        val stations = input.selectedStations

        val n3 = stations.any { it.startsWith("1") || (side == TumorSide.RIGHT && it.endsWith("L")) || (side == TumorSide.LEFT && it.endsWith("R")) }
        if (n3) return NCategory.N3

        val n2Stations = stations.filter { it in listOf("2R","2L","3aR","3aL","3p","4R","4L","5","6","7","8","9") }
        if (n2Stations.size > 1) return NCategory.N2b
        if (n2Stations.size == 1) return NCategory.N2a

        val n1Stations = stations.filter { it in listOf("10R","10L","11R","11L","12R","12L") }
        if (n1Stations.isNotEmpty()) return NCategory.N1

        return NCategory.N0
    }

    // ------------ M LOGIC ------------

    private fun calculateM(input: MInput): MCategory {
        return when {
            input.intrathoracicMet -> MCategory.M1a
            input.extrathoracicMetCount == 1 -> MCategory.M1b
            input.extrathoracicMetCount > 1 && input.extrathoracicOrgansInvolved == 1 -> MCategory.M1c1
            input.extrathoracicOrgansInvolved > 1 -> MCategory.M1c2
            else -> MCategory.M0
        }
    }

    // ------------ STAGE GROUPING (TNM-9) ------------

    private fun calculateStage(t: TCategory, n: NCategory, m: MCategory): StageGroup {
        // M override
        if (m == MCategory.M1a || m == MCategory.M1b) return StageGroup.IVA
        if (m == MCategory.M1c1 || m == MCategory.M1c2) return StageGroup.IVB

        return when (t) {
            TCategory.Tis -> StageGroup.STAGE_0
            TCategory.T1a -> when (n) {
                NCategory.N0 -> StageGroup.IA1
                NCategory.N1 -> StageGroup.IIA
                NCategory.N2a -> StageGroup.IIB
                NCategory.N2b -> StageGroup.IIIA
                NCategory.N3 -> StageGroup.IIIB
                else -> StageGroup.UNSTAGEABLE
            }
            TCategory.T1b -> when (n) {
                NCategory.N0 -> StageGroup.IA2
                NCategory.N1 -> StageGroup.IIA
                NCategory.N2a -> StageGroup.IIB
                NCategory.N2b -> StageGroup.IIIA
                NCategory.N3 -> StageGroup.IIIB
                else -> StageGroup.UNSTAGEABLE
            }
            TCategory.T1c -> when (n) {
                NCategory.N0 -> StageGroup.IA3
                NCategory.N1 -> StageGroup.IIA
                NCategory.N2a -> StageGroup.IIB
                NCategory.N2b -> StageGroup.IIIA
                NCategory.N3 -> StageGroup.IIIB
                else -> StageGroup.UNSTAGEABLE
            }
            TCategory.T2a -> when (n) {
                NCategory.N0 -> StageGroup.IB
                NCategory.N1 -> StageGroup.IIB
                NCategory.N2a -> StageGroup.IIIA
                NCategory.N2b -> StageGroup.IIIB
                NCategory.N3 -> StageGroup.IIIB
                else -> StageGroup.UNSTAGEABLE
            }
            TCategory.T2b -> when (n) {
                NCategory.N0 -> StageGroup.IIA
                NCategory.N1 -> StageGroup.IIB
                NCategory.N2a -> StageGroup.IIIA
                NCategory.N2b -> StageGroup.IIIB
                NCategory.N3 -> StageGroup.IIIB
                else -> StageGroup.UNSTAGEABLE
            }
            TCategory.T3 -> when (n) {
                NCategory.N0 -> StageGroup.IIB
                NCategory.N1 -> StageGroup.IIIA
                NCategory.N2a -> StageGroup.IIIA
                NCategory.N2b -> StageGroup.IIIB
                NCategory.N3 -> StageGroup.IIIC
                else -> StageGroup.UNSTAGEABLE
            }
            TCategory.T4 -> when (n) {
                NCategory.N0, NCategory.N1 -> StageGroup.IIIA
                NCategory.N2a, NCategory.N2b -> StageGroup.IIIB
                NCategory.N3 -> StageGroup.IIIC
                else -> StageGroup.UNSTAGEABLE
            }
        }
    }
}
