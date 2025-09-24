package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calculators.calculatePESI
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PESIScreen() {
    var age by remember { mutableStateOf("") }
    var male by remember { mutableStateOf(true) }
    var cancer by remember { mutableStateOf(false) }
    var heartFailure by remember { mutableStateOf(false) }
    var lungDisease by remember { mutableStateOf(false) }
    var pulse by remember { mutableStateOf("") }
    var sbp by remember { mutableStateOf("") }
    var rr by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var confusion by remember { mutableStateOf(false) }
    var spo2 by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            InfoCard(
                "PESI (Pulmonary Embolism Severity Index) odhaduje 30-dňovú mortalitu u pacientov s pľúcnou embóliou. " +
                        "Zohľadňuje vek, pohlavie, komorbidity a vitálne funkcie. " +
                        "Výsledok pomáha stratifikovať riziko a určovať miesto a intenzitu liečby."
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = age, onValueChange = { age = it },
                label = { Text("Vek") },
                modifier = Modifier.fillMaxWidth()
            )
            RowItem("Muž", male) { male = it }
            RowItem("Karcinóm", cancer) { cancer = it }
            RowItem("Srdcové zlyhanie", heartFailure) { heartFailure = it }
            RowItem("Pľúcne ochorenie", lungDisease) { lungDisease = it }

            OutlinedTextField(
                value = pulse, onValueChange = { pulse = it },
                label = { Text("Pulz (/min)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sbp, onValueChange = { sbp = it },
                label = { Text("Systolický TK (mmHg)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = rr, onValueChange = { rr = it },
                label = { Text("RR (/min)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = temp, onValueChange = { temp = it },
                label = { Text("Teplota (°C)") },
                modifier = Modifier.fillMaxWidth()
            )
            RowItem("Konfúzia", confusion) { confusion = it }
            OutlinedTextField(
                value = spo2, onValueChange = { spo2 = it },
                label = { Text("SpO₂ (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val res = calculatePESI(
                        age.toIntOrNull() ?: 50,
                        male,
                        cancer,
                        heartFailure,
                        lungDisease,
                        pulse.toIntOrNull() ?: 80,
                        sbp.toIntOrNull() ?: 120,
                        rr.toIntOrNull() ?: 20,
                        temp.toDoubleOrNull() ?: 37.0,
                        confusion,
                        spo2.toIntOrNull() ?: 95
                    )
                    result = "Skóre: ${res.score} • Trieda ${res.classGroup} • Mortalita ${res.mortality}"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

            // 🔹 Výsledok cez ResultCard
            result?.let { r ->
                Spacer(Modifier.height(16.dp))
                ResultCard(
                    text = r,
                    onCopy = { copied ->
                        clipboardManager.setText(AnnotatedString(copied))
                        scope.launch {
                            snackbarHostState.showSnackbar("Skopírované do schránky")
                        }
                    }
                )
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
