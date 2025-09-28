package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calculators.evaluatemMRC
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mMRCScreen() {
    var selected by remember { mutableStateOf<Int?>(null) }
    var result by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val mMRCOptions = listOf(
        0 to "Dyspnoe len pri veľmi namáhavej námahe.",
        1 to "Dyspnoe pri rýchlej chôdzi alebo miernom stúpaní.",
        2 to "Chôdza pomalšia než rovesníci, prestávky pri chôdzi v rovine.",
        3 to "Po 100 metroch alebo pár minútach chôdze musí zastaviť.",
        4 to "Dyspnoe aj pri obliekaní alebo v pokoji."
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            InfoCard(
                "mMRC (modified Medical Research Council) je 5-stupňová dyspnoická škála (0–4) " +
                        "na hodnotenie limitácie dychavice pri každodenných aktivitách u pacientov s CHOCHP a inými pľúcnymi chorobami."
            )

            Spacer(Modifier.height(16.dp))

            // výber podľa opisu
            mMRCOptions.forEach { (score, description) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    RadioButton(
                        selected = selected == score,
                        onClick = { selected = score }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(description)
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (selected != null) {
                        result = "Výsledok: mMRC $selected"
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Vyberte opis ťažkostí.") }
                    }
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
