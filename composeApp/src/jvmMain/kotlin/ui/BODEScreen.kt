package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calculators.calculateBODE
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BODEScreen() {
    var bmi by remember { mutableStateOf("") }
    var fev1 by remember { mutableStateOf("") }
    var mmrc by remember { mutableStateOf("") }
    var sixMWD by remember { mutableStateOf("") }

    var scoreText by remember { mutableStateOf<String?>(null) }
    var survivalText by remember { mutableStateOf<String?>(null) }
    var details by remember { mutableStateOf<List<String>>(emptyList()) }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ScrollableScreen(
            modifier = Modifier.padding(innerPadding),
            onBack = null
        ) {
            // 🔹 InfoCard – popis
            InfoCard(
                text = "BODE index je skórovací systém, ktorý využíva premenné z viacerých domén na odhad " +
                        "celkovej mortality aj mortality z respiračných príčin (respiračné zlyhanie, pneumónia, " +
                        "pľúcna embólia) u pacientov s CHOCHP. " +
                        "Určený je na použitie u pacientov so stabilnou CHOCHP, ktorí už sú na adekvátnej liečbe " +
                        "(nie pri akútnej exacerbácii CHOCHP). " +
                        "Vyžaduje FEV₁, 6-minútový test chôdze (6MWT) a dyspnoickú škálu mMRC. " +
                        "Nie je určený na vedenie ani ovplyvňovanie liečby. " +
                        "V porovnaní so samotným FEV₁ lepšie predpovedá riziko úmrtia, hospitalizácií a exacerbácií CHOCHP."
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = bmi,
                onValueChange = { bmi = it },
                label = { Text("BMI") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fev1,
                onValueChange = { fev1 = it },
                label = { Text("FEV₁ % predikcie") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = mmrc,
                onValueChange = { mmrc = it },
                label = { Text("mMRC (0–4)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sixMWD,
                onValueChange = { sixMWD = it },
                label = { Text("6MWD (m)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val bmiVal = bmi.toDoubleOrNull()
                    val fev1Val = fev1.toDoubleOrNull()
                    val mmrcVal = mmrc.toIntOrNull()
                    val sixMWDVal = sixMWD.toIntOrNull() // 🔹 opravené späť na Int

                    if (bmiVal != null && fev1Val != null && mmrcVal != null && sixMWDVal != null) {
                        val result = calculateBODE(bmiVal, fev1Val, mmrcVal, sixMWDVal)
                        scoreText = "BODE skóre: ${result.score}"
                        details = result.details
                    } else {
                        scoreText = "Zadajte všetky vstupné hodnoty."
                        details = emptyList()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

            // 🔹 Výsledok cez ResultCard
            scoreText?.let { score ->
                val combined = buildString {
                    appendLine(score)
                    if (details.isNotEmpty()) {
                        appendLine()
                        details.forEach { appendLine("• $it") }
                    }
                }

                ResultCard(
                    text = combined,
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
