package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt
import ui.NumberField

// ---------- Konštanty ----------
private const val TOTAL_SEGMENTS = 19
private const val RIGHT_SEGMENTS = 10
private const val LEFT_SEGMENTS = 9

private enum class ResectionType { Lobectomy, Bilobectomy, Segmentectomy, Pneumonectomy, CustomCount }
private enum class Side { Right, Left }
private enum class RightBilobe { UpperMiddle, MiddleLower }
private enum class Lobe { RUL, RML, RLL, LUL, LLL }

// počty segmentov podľa 19-segmentovej konvencie
private fun segmentsInLobe(lobe: Lobe): Int = when (lobe) {
    Lobe.RUL -> 3
    Lobe.RML -> 2
    Lobe.RLL -> 5
    Lobe.LUL -> 5
    Lobe.LLL -> 4
}
private fun segmentsOnSide(side: Side) = if (side == Side.Right) RIGHT_SEGMENTS else LEFT_SEGMENTS

// pekné 1 desatinné miesto bez locale
private fun fmt1(x: Double): String {
    val v = (x * 10).roundToInt() / 10.0
    val s = v.toString()
    return if (s.contains('.')) s else "$s.0"
}

@Composable
fun PpoScreen(snackbarHostState: SnackbarHostState) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    // vstupy
    var fev1PctText by remember { mutableStateOf("") }
    var dlcoPctText by remember { mutableStateOf("") }

    var type by remember { mutableStateOf(ResectionType.Lobectomy) }
    var side by remember { mutableStateOf(Side.Right) }
    var lobe by remember { mutableStateOf(Lobe.RUL) }
    var bilobe by remember { mutableStateOf(RightBilobe.MiddleLower) }

    var segCountText by remember { mutableStateOf("") }
    var considerObstruction by remember { mutableStateOf(false) }
    var obstructedText by remember { mutableStateOf("") }

    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("PPO – pooperačná funkcia (segmentová metóda)", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Text("Východzie hodnoty (voliteľné – v % z predikcie)", style = MaterialTheme.typography.labelLarge)
        NumberField("FEV₁ % pred (napr. 75)", fev1PctText) { fev1PctText = it }
        NumberField("DLCO % pred (napr. 62)", dlcoPctText) { dlcoPctText = it }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        Text("Typ resekcie", style = MaterialTheme.typography.labelLarge)
        Column(Modifier.padding(top = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = type == ResectionType.Lobectomy, onClick = { type = ResectionType.Lobectomy })
                Text("Lobektómia")
                Spacer(Modifier.width(16.dp))
                RadioButton(selected = type == ResectionType.Bilobectomy, onClick = { type = ResectionType.Bilobectomy })
                Text("Bilobektómia")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = type == ResectionType.Segmentectomy, onClick = { type = ResectionType.Segmentectomy })
                Text("Segmentektómia")
                Spacer(Modifier.width(16.dp))
                RadioButton(selected = type == ResectionType.Pneumonectomy, onClick = { type = ResectionType.Pneumonectomy })
                Text("Pneumonektómia")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = type == ResectionType.CustomCount, onClick = { type = ResectionType.CustomCount })
                Text("Vlastný počet segmentov")
            }
        }

        Spacer(Modifier.height(8.dp))

        when (type) {
            ResectionType.Lobectomy -> {
                Text("Strana a lalok", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = side == Side.Right,
                        onClick = {
                            side = Side.Right
                            if (lobe == Lobe.LUL || lobe == Lobe.LLL) lobe = Lobe.RUL
                        },
                        label = { Text("Pravá") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    FilterChip(
                        selected = side == Side.Left,
                        onClick = {
                            side = Side.Left
                            if (lobe == Lobe.RUL || lobe == Lobe.RML || lobe == Lobe.RLL) lobe = Lobe.LUL
                        },
                        label = { Text("Ľavá") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val lobes = if (side == Side.Right) listOf(Lobe.RUL, Lobe.RML, Lobe.RLL) else listOf(Lobe.LUL, Lobe.LLL)
                    lobes.forEach { current ->
                        FilterChip(
                            selected = lobe == current,
                            onClick = { lobe = current },
                            label = {
                                val label = when (current) {
                                    Lobe.RUL -> "RUL (3)"
                                    Lobe.RML -> "RML (2)"
                                    Lobe.RLL -> "RLL (5)"
                                    Lobe.LUL -> "LUL (5)"
                                    Lobe.LLL -> "LLL (4)"
                                }
                                Text(label)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            ResectionType.Bilobectomy -> {
                Text("Bilobektómia je možná iba na pravej strane", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = bilobe == RightBilobe.UpperMiddle,
                        onClick = { bilobe = RightBilobe.UpperMiddle },
                        label = { Text("RUL + RML (5)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    FilterChip(
                        selected = bilobe == RightBilobe.MiddleLower,
                        onClick = { bilobe = RightBilobe.MiddleLower },
                        label = { Text("RML + RLL (7)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            ResectionType.Segmentectomy, ResectionType.CustomCount -> {
                NumberField("Počet segmentov na odstránenie (1–19)", segCountText) { segCountText = it }
            }

            ResectionType.Pneumonectomy -> {
                Text("Strana", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = side == Side.Right, onClick = { side = Side.Right }, label = { Text("Pravá (10)") })
                    FilterChip(selected = side == Side.Left, onClick = { side = Side.Left }, label = { Text("Ľavá (9)") })
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = considerObstruction, onCheckedChange = { considerObstruction = it })
            Text("Zohľadniť nefunkčné (obtur./atelekt.) segmenty")
        }
        if (considerObstruction) {
            NumberField("Počet nefunkčných segmentov (0–18)", obstructedText) { obstructedText = it }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val fev1Pct = fev1PctText.toDoubleOrNull()
                val dlcoPct = dlcoPctText.toDoubleOrNull()
                val segCount = segCountText.toIntOrNull()
                val obstructed = obstructedText.toIntOrNull() ?: 0

                val removed = when (type) {
                    ResectionType.Lobectomy -> segmentsInLobe(lobe)
                    ResectionType.Bilobectomy -> if (bilobe == RightBilobe.UpperMiddle) 5 else 7
                    ResectionType.Segmentectomy, ResectionType.CustomCount -> segCount ?: -1
                    ResectionType.Pneumonectomy -> segmentsOnSide(side)
                }

                if (removed <= 0 || removed > TOTAL_SEGMENTS) {
                    scope.launch { snackbarHostState.showSnackbar("Zadaj platný počet odobraných segmentov (1–19).") }
                    return@Button
                }
                if (considerObstruction && (obstructed < 0 || obstructed >= TOTAL_SEGMENTS)) {
                    scope.launch { snackbarHostState.showSnackbar("Počet nefunkčných segmentov musí byť v rozsahu 0–18.") }
                    return@Button
                }

                val functionalTotal = max(1, TOTAL_SEGMENTS - max(0, obstructed))
                val functionalRemaining = max(0, functionalTotal - removed)
                val preservedFraction = functionalRemaining.toDouble() / functionalTotal.toDouble()

                val ppoFev = fev1Pct?.let { it * preservedFraction }
                val ppoDlco = dlcoPct?.let { it * preservedFraction }

                result = buildString {
                    appendLine("Odstránené segmenty: $removed")
                    if (considerObstruction) {
                        appendLine("Nefunkčné segmenty pred operáciou: $obstructed")
                        appendLine("Funkčný základ: $functionalTotal (z 19)")
                    } else {
                        appendLine("Základ pre výpočet: 19 segmentov")
                    }
                    appendLine("Zachovaná frakcia: ${fmt1(preservedFraction * 100)} %")
                    if (ppoFev != null) appendLine("ppoFEV₁ ≈ ${fmt1(ppoFev)} %")
                    if (ppoDlco != null) appendLine("ppoDLCO ≈ ${fmt1(ppoDlco)} %")
                    if (ppoFev == null && ppoDlco == null) {
                        append("Zadaj aspoň FEV₁ % alebo DLCO % pre výpočet ppo %.")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Prepočítať") }

        // 🔹 Výsledok cez ResultCard namiesto Surface
        if (result.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            ResultCard(
                text = result,
                onCopy = { copied ->
                    clipboard.setText(AnnotatedString(copied))
                    scope.launch { snackbarHostState.showSnackbar("Skopírované do schránky") }
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Pozn.: Výpočet podľa segmentovej metódy (19 segmentov). Ak sú pred operáciou prítomné nefunkčné (obtur./atelekt.) segmenty, " +
                    "základ pre výpočet sa zníži o ich počet (teda resekcia neuberá z funkcie, ktorá už chýba).",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
