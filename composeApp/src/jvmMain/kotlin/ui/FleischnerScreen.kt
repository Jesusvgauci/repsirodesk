package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calculators.*
import kotlin.math.roundToInt

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

    Scaffold { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {
            InfoCard(
                "Fleischner odporúčania (2017) platia pre incidentálne noduly na CT (nie skríning, nie ≤35 r., nie imunosupresia)."
            )

            Spacer(Modifier.height(8.dp))
            InfoCard(
                "Meranie nodulu: dlhá + krátka os na tom istom reze, priemer = (d+ k)/2, celé mm, tenké rezy ≤1.5 mm."
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

            result?.let { rec ->
                Spacer(Modifier.height(16.dp))
                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Odporúčanie:", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(rec, style = MaterialTheme.typography.bodyLarge)
                        if (notes.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            notes.forEach { n -> Text("• $n") }
                        }
                    }
                }
            }
        }
    }
}
