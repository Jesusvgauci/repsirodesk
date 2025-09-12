package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calculators.evaluatemMRC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mMRCScreen() {
    var selected by remember { mutableStateOf(0) }
    var result by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            // 🟦 InfoCard – stručné vysvetlenie škály
            InfoCard(
                "mMRC (modified Medical Research Council) je 5-stupňová dyspnoická škála (0–4) " +
                        "na hodnotenie limitácie dychavice pri každodenných aktivitách u pacientov s CHOCHP a inými pľúcnymi chorobami."
            )

            Spacer(Modifier.height(16.dp))

            // 🟦 Voľba skóre 0–4
            mMRCOption(index = 0, title = "Skóre 0", selected = selected == 0) { selected = 0 }
            mMRCOption(index = 1, title = "Skóre 1", selected = selected == 1) { selected = 1 }
            mMRCOption(index = 2, title = "Skóre 2", selected = selected == 2) { selected = 2 }
            mMRCOption(index = 3, title = "Skóre 3", selected = selected == 3) { selected = 3 }
            mMRCOption(index = 4, title = "Skóre 4", selected = selected == 4) { selected = 4 }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val res = evaluatemMRC(selected)
                    result = "mMRC $selected → ${res.description}"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

            // 🟦 Výsledok v modrom boxe
            result?.let {
                Spacer(Modifier.height(16.dp))
                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun mMRCOption(
    index: Int,
    title: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(8.dp))
        Text(title)
    }
}
