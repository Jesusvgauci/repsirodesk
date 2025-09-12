package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calculators.calculateGeneva

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenevaScreen() {
    var age65 by remember { mutableStateOf(false) }
    var prevVTE by remember { mutableStateOf(false) }
    var surgeryFracture by remember { mutableStateOf(false) }
    var cancer by remember { mutableStateOf(false) }
    var legPain by remember { mutableStateOf(false) }
    var hemoptysis by remember { mutableStateOf(false) }
    var hr by remember { mutableStateOf("") }
    var legSwelling by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            // 🟦 InfoCard – stručné vysvetlenie
            InfoCard(
                "Revidované Ženevské skóre odhaduje klinickú pravdepodobnosť pľúcnej embólie " +
                        "pomocou bodov za vek ≥65 rokov, predchádzajúcu VTE, nedávnu operáciu/fraktúru, " +
                        "aktívnu malignitu, jednostrannú bolesť DK, hemoptýzu, tachykardiu a citlivosť/opuch DK."
            )

            Spacer(Modifier.height(16.dp))

            // 🟦 Vstupy
            RowItem("Vek ≥ 65 rokov", age65) { age65 = it }
            RowItem("Predchádzajúca VTE", prevVTE) { prevVTE = it }
            RowItem("Operácia / fraktúra < 1 mesiac", surgeryFracture) { surgeryFracture = it }
            RowItem("Aktívna malignita", cancer) { cancer = it }
            RowItem("Jednostranná bolesť DK", legPain) { legPain = it }
            RowItem("Hemoptýza", hemoptysis) { hemoptysis = it }

            OutlinedTextField(
                value = hr,
                onValueChange = { hr = it },
                label = { Text("Srdcová frekvencia (bpm)") },
                modifier = Modifier.fillMaxWidth()
            )

            RowItem("Bolesť pri palpácii DK + opuch", legSwelling) { legSwelling = it }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val res = calculateGeneva(
                        age65, prevVTE, surgeryFracture, cancer,
                        legPain, hemoptysis, hr.toIntOrNull() ?: 70, legSwelling
                    )
                    result = "Skóre: ${res.score} • ${res.risk}"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

            // 🟦 Výsledok v modrom boxe
            result?.let {
                Spacer(Modifier.height(16.dp))
                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
