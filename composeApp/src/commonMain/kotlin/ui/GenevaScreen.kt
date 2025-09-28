package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calculators.calculateGeneva
import kotlinx.coroutines.launch

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

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
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

            // 🟦 Výsledok cez ResultCard (jediná zmena)
            result?.let { r ->
                Spacer(Modifier.height(16.dp))
                ResultCard(
                    text = r,
                    onCopy = { copied ->
                        clipboardManager.setText(AnnotatedString(copied))
                        scope.launch { snackbarHostState.showSnackbar("Skopírované do schránky") }
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
