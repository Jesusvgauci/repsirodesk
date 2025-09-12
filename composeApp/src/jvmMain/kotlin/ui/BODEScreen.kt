package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import calculators.calculateBODE


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

    Scaffold { innerPadding ->
        ScrollableScreen(
            modifier = Modifier.padding(innerPadding),
            onBack = null
        ) {
            // 🟦 InfoCard – text bez prázdnych riadkov
            InfoCard(
                "BODE index je skórovací systém, ktorý využíva premenné z viacerých domén na odhad " +
                        "celkovej mortality aj mortality z respiračných príčin (respiračné zlyhanie, pneumónia, " +
                        "pľúcna embólia) u pacientov s CHOCHP. " +
                        "Určený je na použitie u pacientov so stabilnou CHOCHP, ktorí už sú na adekvátnej liečbe " +
                        "(nie pri akútnej exacerbácii CHOCHP). " +
                        "Vyžaduje FEV₁, 6-minútový test chôdze (6MWT) a dyspnoickú škálu mMRC. " +
                        "Nie je určený na vedenie ani ovplyvňovanie liečby. " +
                        "V porovnaní so samotným FEV₁ lepšie predpovedá riziko úmrtia, hospitalizácií a exacerbácií CHOCHP."
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = bmi, onValueChange = { bmi = it }, label = { Text("BMI") })
            OutlinedTextField(value = fev1, onValueChange = { fev1 = it }, label = { Text("FEV₁ (% pred)") })
            OutlinedTextField(value = mmrc, onValueChange = { mmrc = it }, label = { Text("mMRC skóre (0–4)") })
            OutlinedTextField(value = sixMWD, onValueChange = { sixMWD = it }, label = { Text("6MWD (m)") })

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val res = calculateBODE(
                        bmi.toDoubleOrNull() ?: 0.0,
                        fev1.toDoubleOrNull() ?: 0.0,
                        mmrc.toIntOrNull() ?: 0,
                        sixMWD.toIntOrNull() ?: 0
                    )

                    scoreText = "Skóre: ${res.score} → ${res.mortalityRisk}"
                    survivalText = when (res.score) {
                        in 0..2 -> "≈ 80 % prežitie za 4 roky"
                        in 3..4 -> "≈ 67 % prežitie za 4 roky"
                        in 5..6 -> "≈ 57 % prežitie za 4 roky"
                        in 7..10 -> "≈ 18 % prežitie za 4 roky"
                        else -> "Údaje mimo rozsah"
                    }
                    details = res.details
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

            if (scoreText != null || survivalText != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        scoreText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        survivalText?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (details.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            details.forEach { d ->
                                Text(
                                    text = d,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
