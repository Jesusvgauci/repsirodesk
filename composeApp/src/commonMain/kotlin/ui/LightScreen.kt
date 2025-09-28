package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calculators.evaluateLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightScreen() {
    var serumProtein by remember { mutableStateOf("") }
    var pleuralProtein by remember { mutableStateOf("") }
    var serumLDH by remember { mutableStateOf("") }
    var pleuralLDH by remember { mutableStateOf("") }
    var serumLDH_ULN by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🔹 InfoCard – vysvetlenie
            InfoCard(
                text = "Lightove kritériá sa používajú na určenie typu pleurálneho výpotku u pacienta a tým dopomôcť k určeniu etiológie. " +
                        "Pamätajte, že sú vysoko senzitívne (98 %) ale menej špecifické (83 %)."
            )

            // ✅ Prehľad kritérií – tiež môžeme dať do InfoCard
            InfoCard(
                text = """
                    Lightove kritériá (ak aspoň 1 splnené):
                    • Pleurálny proteín / sérový proteín > 0.5
                    • Pleurálny LDH / sérový LDH > 0.6
                    • Pleurálny LDH > 2/3 hornej hranice normy LDH
                """.trimIndent()
            )

            OutlinedTextField(
                value = serumProtein,
                onValueChange = { serumProtein = it },
                label = { Text("Sérum proteíny (g/dL)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pleuralProtein,
                onValueChange = { pleuralProtein = it },
                label = { Text("Pleurálne proteíny (g/dL)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = serumLDH,
                onValueChange = { serumLDH = it },
                label = { Text("Sérum LDH (U/L)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pleuralLDH,
                onValueChange = { pleuralLDH = it },
                label = { Text("Pleurálny LDH (U/L)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = serumLDH_ULN,
                onValueChange = { serumLDH_ULN = it },
                label = { Text("Horná hranica normy LDH (U/L)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val sp = serumProtein.toDoubleOrNull()
                    val pp = pleuralProtein.toDoubleOrNull()
                    val sLDH = serumLDH.toDoubleOrNull()
                    val pLDH = pleuralLDH.toDoubleOrNull()
                    val sLDH_ULNval = serumLDH_ULN.toDoubleOrNull()

                    if (sp != null && pp != null && sLDH != null && pLDH != null && sLDH_ULNval != null) {
                        val res = evaluateLight(sp, pp, sLDH, pLDH, sLDH_ULNval)
                        result = res.explanation.joinToString("\n")
                    } else {
                        result = "Zadajte všetky hodnoty (sérum/pleurálne proteíny, LDH a horná hranica normy LDH)."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

            // 🔹 Výsledok cez ResultCard
            result?.let { r ->
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
