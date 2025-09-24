package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calculators.calculateSpO2FiO2
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OxygenationScreen() {
    var spo2 by remember { mutableStateOf("") }
    var fio2 by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

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
