package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calculators.evaluateLight

@Composable
fun LightScreen() {
    var serumProtein by remember { mutableStateOf("") }
    var pleuralProtein by remember { mutableStateOf("") }
    var serumLDH by remember { mutableStateOf("") }
    var pleuralLDH by remember { mutableStateOf("") }
    var serumLDH_ULN by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        InfoCard(
            "Lightove kritériá sa používajú na určenie typu pleurálneho výpotku u pacienta a tým dopomôcť k určeniu etiológie. " +
                    "Pamätajte, že sú vysoko senzitívne (98 %) ale menej špecifické (83 %)."
        )

        Spacer(Modifier.height(12.dp))

        // ✅ Prehľad kritérií
        Surface(
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Lightove kritériá (ak aspoň 1 splnené):",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text("• Pleurálny proteín / sérový proteín > 0.5")
                Text("• Pleurálny LDH / sérový LDH > 0.6")
                Text("• Pleurálny LDH > 2/3 hornej hranice normy LDH")
            }
        }

        Spacer(Modifier.height(16.dp))

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
            label = { Text("Pleurálne LDH (U/L)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = serumLDH_ULN,
            onValueChange = { serumLDH_ULN = it },
            label = { Text("Horná hranica normy LDH (U/L)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val res = evaluateLight(
                    serumProtein.toDoubleOrNull() ?: 0.0,
                    pleuralProtein.toDoubleOrNull() ?: 0.0,
                    serumLDH.toDoubleOrNull() ?: 0.0,
                    pleuralLDH.toDoubleOrNull() ?: 0.0,
                    serumLDH_ULN.toDoubleOrNull() ?: 0.0
                )
                result = res.explanation.joinToString("\n")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Vyhodnotiť")
        }

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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
