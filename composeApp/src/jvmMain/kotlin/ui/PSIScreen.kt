package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calculators.calculatePSI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PSIScreen() {
    var age by remember { mutableStateOf("") }
    var male by remember { mutableStateOf(true) }
    var nursingHome by remember { mutableStateOf(false) }
    var neoplastic by remember { mutableStateOf(false) }
    var liver by remember { mutableStateOf(false) }
    var heartFailure by remember { mutableStateOf(false) }
    var cerebrovascular by remember { mutableStateOf(false) }
    var renal by remember { mutableStateOf(false) }
    var rr by remember { mutableStateOf("") }
    var sbp by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    var confusion by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            // 🟦 InfoCard – stručné vysvetlenie
            InfoCard(
                "PSI (Pneumonia Severity Index) odhaduje riziko mortality u dospelých s komunitnou pneumóniou. " +
                        "Zohľadňuje vek, komorbidity, vitálne funkcie a laboratórne/klinické ukazovatele. " +
                        "Výsledok pomáha rozhodnúť o mieste liečby (ambulantne vs. hospitalizácia)."
            )

            Spacer(Modifier.height(16.dp))

            // 🟦 Vstupy
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Vek") },
                modifier = Modifier.fillMaxWidth()
            )
            RowItem("Muž", male) { male = it }
            RowItem("Domov dôchodcov", nursingHome) { nursingHome = it }
            RowItem("Neoplázia", neoplastic) { neoplastic = it }
            RowItem("Choroba pečene", liver) { liver = it }
            RowItem("Srdcové zlyhanie", heartFailure) { heartFailure = it }
            RowItem("Cievna mozgová príhoda", cerebrovascular) { cerebrovascular = it }
            RowItem("Renálne ochorenie", renal) { renal = it }

            OutlinedTextField(
                value = rr,
                onValueChange = { rr = it },
                label = { Text("RR (/min)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sbp,
                onValueChange = { sbp = it },
                label = { Text("Systolický TK (mmHg)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = temp,
                onValueChange = { temp = it },
                label = { Text("Teplota (°C)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pulse,
                onValueChange = { pulse = it },
                label = { Text("Pulz (/min)") },
                modifier = Modifier.fillMaxWidth()
            )
            RowItem("Konfúzia", confusion) { confusion = it }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val res = calculatePSI(
                        age.toIntOrNull() ?: 50,
                        male, nursingHome, neoplastic, liver,
                        heartFailure, cerebrovascular, renal,
                        rr.toIntOrNull() ?: 20,
                        sbp.toIntOrNull() ?: 120,
                        temp.toDoubleOrNull() ?: 37.0,
                        pulse.toIntOrNull() ?: 80,
                        confusion
                    )
                    result = "Skóre: ${res.score} • Trieda ${res.riskClass} • Mortalita ${res.mortality} • Odporúčanie: ${res.recommendation}"
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
