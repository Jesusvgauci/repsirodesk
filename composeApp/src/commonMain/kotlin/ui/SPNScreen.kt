package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calculators.calculateSPN
import kotlinx.coroutines.launch
import utils.toFixed

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

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

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
                    result = "Riziko: ${res.probability.toFixed(1)} % → ${res.category}"
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
