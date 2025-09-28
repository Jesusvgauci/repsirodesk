package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calculators.calculateRESECT90
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RESECT90Screen() {
    var age by remember { mutableStateOf("") }
    var male by remember { mutableStateOf(true) }
    var fev1 by remember { mutableStateOf("") }
    var dlco by remember { mutableStateOf("") }
    var pneumonectomy by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            InfoCard(
                "RESECT-90 je skórovací model na odhad 90-dňovej mortality po resekcii pľúc. " +
                        "Zohľadňuje vek, pohlavie, FEV₁ % predikcie, DLCO % predikcie a typ výkonu (pneumonektómia). " +
                        "Výsledok slúži na predoperačné rizikové zhodnotenie – klinický kontext je nevyhnutný."
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Vek") },
                modifier = Modifier.fillMaxWidth()
            )
            RowItem("Muž", male) { male = it }
            OutlinedTextField(
                value = fev1,
                onValueChange = { fev1 = it },
                label = { Text("FEV₁ (% predikcie)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dlco,
                onValueChange = { dlco = it },
                label = { Text("DLCO (% predikcie)") },
                modifier = Modifier.fillMaxWidth()
            )
            RowItem("Pneumonektómia", pneumonectomy) { pneumonectomy = it }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val res = calculateRESECT90(
                        age.toIntOrNull() ?: 65,
                        male,
                        fev1.toIntOrNull() ?: 80,
                        dlco.toIntOrNull() ?: 80,
                        pneumonectomy
                    )
                    result = "Skóre: ${res.score} → ${res.risk}"
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
