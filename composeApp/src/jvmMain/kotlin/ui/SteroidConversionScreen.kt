package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calculators.convertSteroid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SteroidScreen() {
    val drugs = listOf(
        "Betametazon (IV)",
        "Kortizón (PO)",
        "Dexametazon (IV/PO)",
        "Hydrokortizón (IV/PO)",
        "Metylprednizolón (IV/PO)",
        "Prednizolón (PO)",
        "Prednison (PO)",
        "Triamcinolón (IV)"
    )

    var inputDrug by remember { mutableStateOf(drugs.first()) }
    var outputDrug by remember { mutableStateOf(drugs[1]) }
    var inputDose by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    var inputExpanded by remember { mutableStateOf(false) }
    var outputExpanded by remember { mutableStateOf(false) }

    val colors = ExposedDropdownMenuDefaults.textFieldColors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface
    )

    Scaffold { innerPadding ->
        ScrollableScreen(modifier = Modifier.padding(innerPadding)) {

            // 🟦 InfoCard – stručné vysvetlenie
            InfoCard(
                text = "Kalkulačka na prepočet ekvivalentných dávok systémových kortikosteroidov. " +
                        "Výsledky sú orientačné – vždy zohľadni klinický stav pacienta."
            )

            // 🟦 Vstupný liek
            ExposedDropdownMenuBox(
                expanded = inputExpanded,
                onExpandedChange = { inputExpanded = !inputExpanded }
            ) {
                OutlinedTextField(
                    value = inputDrug,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vstupný liek") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = inputExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = colors
                )
                ExposedDropdownMenu(
                    expanded = inputExpanded,
                    onDismissRequest = { inputExpanded = false },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    drugs.forEach { drug ->
                        DropdownMenuItem(
                            text = { Text(drug, color = MaterialTheme.colorScheme.onPrimaryContainer) },
                            onClick = {
                                inputDrug = drug
                                inputExpanded = false
                            }
                        )
                    }
                }
            }

            // 🟦 Dávka
            OutlinedTextField(
                value = inputDose,
                onValueChange = { inputDose = it },
                label = { Text("Dávka (mg)") },
                modifier = Modifier.fillMaxWidth()
            )

            // 🟦 Cieľový liek
            ExposedDropdownMenuBox(
                expanded = outputExpanded,
                onExpandedChange = { outputExpanded = !outputExpanded }
            ) {
                OutlinedTextField(
                    value = outputDrug,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cieľový liek") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = outputExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = colors
                )
                ExposedDropdownMenu(
                    expanded = outputExpanded,
                    onDismissRequest = { outputExpanded = false },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    drugs.forEach { drug ->
                        DropdownMenuItem(
                            text = { Text(drug, color = MaterialTheme.colorScheme.onPrimaryContainer) },
                            onClick = {
                                outputDrug = drug
                                outputExpanded = false
                            }
                        )
                    }
                }
            }

            // 🟦 Akcia
            Button(
                onClick = {
                    val dose = inputDose.toDoubleOrNull()
                    result = if (dose != null && dose > 0) {
                        val converted = convertSteroid(inputDrug, dose, outputDrug)
                        "%.1f mg %s ≈ %.1f mg %s".format(dose, inputDrug, converted, outputDrug)
                    } else {
                        "Zadajte platnú dávku."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Prepočítať")
            }

            // 🟦 Výsledok
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
