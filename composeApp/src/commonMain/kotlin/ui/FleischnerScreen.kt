package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calculators.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleischnerScreen() {
    var longAxis by remember { mutableStateOf("") }
    var shortAxis by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var female by remember { mutableStateOf(false) }
    var famHx by remember { mutableStateOf(false) }
    var emphysema by remember { mutableStateOf(false) }
    var upperLobe by remember { mutableStateOf(false) }
    var noduleCount by remember { mutableStateOf("1") }
    var spiculation by remember { mutableStateOf(false) }

    var type by remember { mutableStateOf("solid") }
    var isMultiple by remember { mutableStateOf(false) }
    var suspicious by remember { mutableStateOf(false) }
    var solidComponent by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf<List<String>>(emptyList()) }

    val types = listOf("solid", "ground-glass", "part-solid")

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {
            InfoCard(
                "Fleischner odporúčania (2017) platia pre incidentálne noduly na CT (nie skríning, nie ≤35 r., nie pri imunosupresii a známej malignite)."
            )

            Spacer(Modifier.height(8.dp))
            InfoCard(
                "Nodulus sa meria sprývnym spôsobom tak že sa zoberie krátka os a dlhá na tom istom reze a vytvorrí sa z toho priemer, spikuly sa sa do dĺžky pri meraní nerátajú"
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = longAxis,
                onValueChange = { longAxis = it },
                label = { Text("Dlhá os (mm)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = shortAxis,
                onValueChange = { shortAxis = it },
                label = { Text("Krátka os (mm)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            Text("Typ nodulu", style = MaterialTheme.typography.titleMedium)
            types.forEach { t ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == t, onClick = { type = t })
                    Text(t)
                }
            }

            if (type == "part-solid") {
                OutlinedTextField(
                    value = solidComponent,
                    onValueChange = { solidComponent = it },
                    label = { Text("Solidná časť (mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Vek pacienta (r.)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = female, onCheckedChange = { female = it })
                Text("Žena")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = famHx, onCheckedChange = { famHx = it })
                Text("Rodinná anamnéza karcinómu pľúc")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = emphysema, onCheckedChange = { emphysema = it })
                Text("Emfyzém")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = upperLobe, onCheckedChange = { upperLobe = it })
                Text("Horný lalok")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = spiculation, onCheckedChange = { spiculation = it })
                Text("Spikulácie")
            }

            OutlinedTextField(
                value = noduleCount,
                onValueChange = { noduleCount = it },
                label = { Text("Počet uzlov") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isMultiple, onCheckedChange = { isMultiple = it })
                Text("Viacnásobné uzly")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = suspicious, onCheckedChange = { suspicious = it })
                Text("Podozrivé znaky (rast/spikulácie/↑densita)")
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val longMm = longAxis.toDoubleOrNull() ?: 0.0
                    val shortMm = shortAxis.toDoubleOrNull() ?: 0.0
                    val avgMm = if (longMm > 0 && shortMm > 0) (longMm + shortMm) / 2.0 else 0.0
                    val avgRounded = avgMm.roundToInt()

                    val brockInputs = BrockInputs(
                        age = age.toIntOrNull() ?: 62,
                        female = female,
                        famHx = famHx,
                        emphysema = emphysema,
                        diameterMm = avgMm,
                        type = when (type) {
                            "part-solid" -> NoduleType.PART_SOLID
                            "ground-glass" -> NoduleType.NON_SOLID
                            else -> NoduleType.SOLID
                        },
                        upperLobe = upperLobe,
                        noduleCount = noduleCount.toIntOrNull() ?: 1,
                        spiculation = spiculation
                    )

                    val brockP = BrockCalculator.probability(brockInputs)

                    val res = evaluateFleischner(
                        sizeMm = avgRounded,
                        type = type,
                        highRisk = false,
                        isMultiple = isMultiple,
                        suspiciousFeatures = suspicious,
                        solidComponentMm = solidComponent.toIntOrNull(),
                        brockRiskProbability = brockP,
                        brockHighRiskCutoffPercent = 5.0
                    )
                    result = res.recommendation
                    notes = res.notes
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Vyhodnotiť") }

            // 🔹 Výsledok cez ResultCard
            result?.let { rec ->
                Spacer(Modifier.height(16.dp))

                val output = buildString {
                    appendLine("Odporúčanie: $rec")
                    if (notes.isNotEmpty()) {
                        appendLine()
                        notes.forEach { n -> appendLine("• $n") }
                    }
                }

                ResultCard(
                    text = output,
                    onCopy = { copied ->
                        clipboardManager.setText(AnnotatedString(copied))
                        scope.launch { snackbarHostState.showSnackbar("Skopírované do schránky") }
                    }
                )
            }
        }
    }
}
