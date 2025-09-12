package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calculators.calculateSPN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SPNScreen() {
    var age by remember { mutableStateOf("") }
    var smoker by remember { mutableStateOf(false) }
    var cancerHistory by remember { mutableStateOf(false) }
    var diameter by remember { mutableStateOf("") }
    var upperLobe by remember { mutableStateOf(false) }
    var spiculation by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            // 🟦 InfoCard – vysvetlenie kalkulačky
            InfoCard(
                "Táto kalkulačka odhaduje pravdepodobnosť malignity solitárneho pľúcneho uzla (SPN) " +
                        "na základe veku, fajčenia, anamnézy malignity, veľkosti uzla, jeho lokalizácie a spikulácie."
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Vek") },
                modifier = Modifier.fillMaxWidth()
            )

            RowItem("Fajčiar", smoker) { smoker = it }
            RowItem("Anamnéza malignity", cancerHistory) { cancerHistory = it }

            OutlinedTextField(
                value = diameter,
                onValueChange = { diameter = it },
                label = { Text("Priemer nodulu (mm)") },
                modifier = Modifier.fillMaxWidth()
            )

            RowItem("Horný lalok", upperLobe) { upperLobe = it }
            RowItem("Spikulácie", spiculation) { spiculation = it }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val res = calculateSPN(
                        age.toIntOrNull() ?: 60,
                        smoker,
                        cancerHistory,
                        diameter.toDoubleOrNull() ?: 10.0,
                        upperLobe,
                        spiculation
                    )
                    result = "Riziko: ${"%.1f".format(res.probability)} % → ${res.category}"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

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
