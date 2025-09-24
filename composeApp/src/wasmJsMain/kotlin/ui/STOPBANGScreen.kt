package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calculators.calculateSTOPBANG
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun STOPBANGScreen() {
    var snoring by remember { mutableStateOf(false) }
    var tired by remember { mutableStateOf(false) }
    var observed by remember { mutableStateOf(false) }
    var pressure by remember { mutableStateOf(false) }
    var bmi by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var neck by remember { mutableStateOf("") }
    var male by remember { mutableStateOf(true) }

    var result by remember { mutableStateOf<String?>(null) }

    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            // Info – bez zmeny logiky, len centrálna InfoCard
            InfoCard(
                "STOP-BANG dotazník je jednoduchý skríningový nástroj na odhad rizika obštrukčného spánkového apnoe (OSA). " +
                        "Názov je akronym pre: Snoring, Tiredness, Observed apnoea, high blood Pressure, BMI > 35, Age > 50, " +
                        "Neck circumference > 40 cm, Gender (male). Vyššie skóre znamená vyššie riziko stredne ťažkej až ťažkej OSA."
            )

            Spacer(Modifier.height(16.dp))

            // Položky dotazníka – pôvodné ovládače
            RowItem("Chrápanie", snoring) { snoring = it }
            RowItem("Denná únava", tired) { tired = it }
            RowItem("Pozorované apnoe", observed) { observed = it }
            RowItem("Hypertenzia", pressure) { pressure = it }

            OutlinedTextField(
                value = bmi,
                onValueChange = { bmi = it },
                label = { Text("BMI") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Vek") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = neck,
                onValueChange = { neck = it },
                label = { Text("Obvod krku (cm)") },
                modifier = Modifier.fillMaxWidth()
            )
            RowItem("Muž", male) { male = it }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val res = calculateSTOPBANG(
                        snoring, tired, observed, pressure,
                        bmi.toDoubleOrNull() ?: 25.0,
                        age.toIntOrNull() ?: 40,
                        neck.toDoubleOrNull() ?: 38.0,
                        male
                    )
                    result = "Skóre: ${res.score} → ${res.risk}"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

            // Výsledok – jediná zmena: ResultCard + kopírovanie
            result?.let { r ->
                Spacer(Modifier.height(16.dp))
                ResultCard(
                    text = r,
                    onCopy = { text ->
                        clipboard.setText(AnnotatedString(text))
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
