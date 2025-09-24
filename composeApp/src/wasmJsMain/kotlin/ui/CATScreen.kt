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
import calculators.calculateCAT
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CATScreen() {
    val questions = listOf(
        "Kašeľ",
        "Vykašliavanie hlienu",
        "Tlak na hrudníku",
        "Dýchavičnosť pri chôdzi do kopca / po schodoch",
        "Obmedzenie aktivít doma",
        "Istota pri odchode z domu",
        "Kvalita spánku",
        "Energia"
    )

    val answers = remember { mutableStateListOf(*Array(8) { 0 }) }
    var resultText by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // InfoCard ostáva podľa pôvodného kódu
            InfoCard(
                text = "COPD Assessment Test (CAT) je jednoduchý dotazník určený na posúdenie závažnosti symptómov CHOCHP. " +
                        "Skóre 0–10 = nízke symptómy, 11–20 = stredné, 21–30 = vysoké, 31–40 = veľmi vysoké."
            )

            Spacer(modifier = Modifier.height(16.dp))

            questions.forEachIndexed { index, question ->
                Column {
                    Text(question, style = MaterialTheme.typography.bodyLarge)
                    Slider(
                        value = answers[index].toFloat(),
                        onValueChange = { answers[index] = it.toInt() },
                        valueRange = 0f..5f,
                        steps = 4
                    )
                    Text("Hodnota: ${answers[index]}")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val score = calculateCAT(answers) // 🔹 späť na pôvodný názov
                    resultText = "CAT skóre: $score"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

            // Výsledok cez ResultCard (jediná UI zmena)
            if (resultText.isNotEmpty()) {
                ResultCard(
                    text = resultText,
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
