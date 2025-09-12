package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calculators.calculateCAT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CATScreen() {
    val answers = remember { mutableStateListOf(0, 0, 0, 0, 0, 0, 0, 0) }
    var result by remember { mutableStateOf<String?>(null) }

    val questions = listOf(
        "Kašeľ",
        "Množstvo hlienu",
        "Tlak na hrudníku",
        "Dýchavičnosť pri stúpaní do kopca alebo schodov",
        "Obmedzenie aktivít doma",
        "Pocit istoty pri odchode z domu",
        "Kvalita spánku",
        "Energia"
    )

    Scaffold { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            // 🟦 InfoCard – stručné vysvetlenie
            InfoCard(
                "CAT (COPD Assessment Test) je 8-položkový dotazník (0–5 bodov na položku) " +
                        "hodnotiaci symptómy a vplyv CHOCHP na kvalitu života. Vyššie skóre = vyšší dopad ochorenia."
            )

            Spacer(Modifier.height(16.dp))

            // 🟦 Otázky so slidermi
            questions.forEachIndexed { index, q ->
                Text(q, style = MaterialTheme.typography.bodyLarge)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = answers[index].toFloat(),
                        onValueChange = { newValue -> answers[index] = newValue.toInt() },
                        valueRange = 0f..5f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            activeTickColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            inactiveTickColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${answers[index]}")
                }

                // číselná os 0–5 pod sliderom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (0..5).forEach { number ->
                        Text(text = number.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val res = calculateCAT(answers)
                    result = "Skóre: ${res.total}\n${res.impact}"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

            // 🟦 Výsledok v modrom boxe
            result?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
