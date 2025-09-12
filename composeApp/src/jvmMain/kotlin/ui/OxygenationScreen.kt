package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calculators.calculateSpO2FiO2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OxygenationScreen() {
    var spo2 by remember { mutableStateOf("") }
    var fio2 by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            // 🟦 InfoCard – stručné vysvetlenie
            InfoCard(
                "Pomer SpO₂/FiO₂ je jednoduchý ukazovateľ oxygenácie, ktorý môže slúžiť ako náhrada za PaO₂/FiO₂, " +
                        "najmä ak nie je k dispozícii arteriálny krvný plyn. Nižšie hodnoty znamenajú horšiu oxygenáciu."
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = spo2,
                onValueChange = { spo2 = it },
                label = { Text("SpO₂ (%)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fio2,
                onValueChange = { fio2 = it },
                label = { Text("FiO₂ (0–1.0)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val res = calculateSpO2FiO2(
                        spo2.toIntOrNull() ?: 95,
                        fio2.toDoubleOrNull() ?: 0.21
                    )
                    result = "SpO₂/FiO₂ = ${"%.1f".format(res.ratio)} • ${res.interpretation}"
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
