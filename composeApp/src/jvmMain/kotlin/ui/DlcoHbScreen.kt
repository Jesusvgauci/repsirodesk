package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ui.NumberField
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DlcoHbScreen(snackbarHostState: SnackbarHostState) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var isMale by remember { mutableStateOf(true) }
    var hbText by remember { mutableStateOf("") }
    var dlcoAbsText by remember { mutableStateOf("") }
    var dlcoPctText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("DLCO – korekcia podľa Hb", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = isMale,
                onClick = { isMale = true },
                label = { Text("Muž") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
            FilterChip(
                selected = !isMale,
                onClick = { isMale = false },
                label = { Text("Žena") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        NumberField("Hemoglobín (g/L, 10–300)", hbText) { hbText = it }
        NumberField("DLCO (absolútna hodnota)", dlcoAbsText) { dlcoAbsText = it }
        NumberField("DLCO % pred (voliteľné)", dlcoPctText) { dlcoPctText = it }

        Button(
            onClick = {
                val hb = hbText.toDoubleOrNull()
                val dlco = dlcoAbsText.toDoubleOrNull()
                val dlcoPct = dlcoPctText.toDoubleOrNull()

                if (hb == null || hb !in 10.0..300.0) {
                    scope.launch { snackbarHostState.showSnackbar("Zadaj Hb v rozsahu 10–300 g/L.") }
                    return@Button
                }
                if (dlco == null) {
                    scope.launch { snackbarHostState.showSnackbar("Zadaj absolútnu DLCO.") }
                    return@Button
                }

                val hbRef = if (isMale) 14.6 else 13.4
                val hbIn_gPerdL = hb / 10.0
                val corr = dlco * (hbRef / hbIn_gPerdL)
                val corrPct = dlcoPct?.let { it * (hbRef / hbIn_gPerdL) }

                fun f1(x: Double) = ((x * 10).roundToInt() / 10.0)
                    .toString().let { if (it.contains('.')) it else "$it.0" }

                result = buildString {
                    appendLine("Upravená DLCO (absolútne): ${f1(corr)}")
                    if (corrPct != null) appendLine("Upravená DLCO (% pred): ${f1(corrPct)} %")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) { Text("Prepočítať") }

        if (result.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 3.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(result, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(result))
                            scope.launch { snackbarHostState.showSnackbar("Skopírované do schránky") }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) { Icon(Icons.Default.ContentCopy, contentDescription = "Skopírovať") }
                }
            }
        }


        Text(
            "Pozn.: DLCO_corr = DLCO_meas × (Hb_ref / (Hb_g/L ÷ 10)); Hb_ref: 14.6 g/dL (muž), 13.4 g/dL (žena). " +
                    "Táto obrazovka sa neukladá do histórie.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
