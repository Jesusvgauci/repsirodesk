package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calculators.Tnm9Engine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tnm9Screen() {
    // --- Vstupné premenné ---
    var tumorSize by remember { mutableStateOf("") }

    var visceralPleura by remember { mutableStateOf(false) }
    var mainBronchus by remember { mutableStateOf(false) }
    var atelectasis by remember { mutableStateOf(false) }
    var chestWall by remember { mutableStateOf(false) }
    var mediastinum by remember { mutableStateOf(false) }
    var separateNoduleSameLobe by remember { mutableStateOf(false) }
    var separateNoduleDifferentLobe by remember { mutableStateOf(false) }

    var tumorSide by remember { mutableStateOf(Tnm9Engine.TumorSide.RIGHT) }
    var selectedStations by remember { mutableStateOf(setOf<String>()) }

    var intrathoracicMet by remember { mutableStateOf(false) }
    var extrathoracicCount by remember { mutableStateOf("") }
    var extrathoracicOrgans by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<Tnm9Engine.Result?>(null) }

    val allStations = listOf(
        "1R","1L","2R","2L","3aR","3aL","3p","4R","4L","5","6","7","8","9",
        "10R","10L","11R","11L","12R","12L"
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize() // ✅ Fix Infinity height
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Zadaj klinické údaje", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = tumorSize,
                onValueChange = { tumorSize = it },
                label = { Text("Veľkosť nádoru (cm)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Upstaging faktory
            Text("Upstaging faktory", style = MaterialTheme.typography.titleSmall)
            UpstageCheckbox("Viscerálna pleurálna invázia", visceralPleura) { visceralPleura = it }
            UpstageCheckbox("Postihnutie hlavného bronchu", mainBronchus) { mainBronchus = it }
            UpstageCheckbox("Atelektáza / pneumonitída", atelectasis) { atelectasis = it }
            UpstageCheckbox("Invázia hrudnej steny / perikardu", chestWall) { chestWall = it }
            UpstageCheckbox("Invázia mediastína / veľkých ciev", mediastinum) { mediastinum = it }
            UpstageCheckbox("Samostatný uzol v rovnakom laloku", separateNoduleSameLobe) { separateNoduleSameLobe = it }
            UpstageCheckbox("Samostatný uzol v inom ipsilaterálnom laloku", separateNoduleDifferentLobe) { separateNoduleDifferentLobe = it }

            Spacer(Modifier.height(12.dp))

            // Strana nádoru
            Text("Strana nádoru", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = tumorSide == Tnm9Engine.TumorSide.RIGHT,
                    onClick = { tumorSide = Tnm9Engine.TumorSide.RIGHT },
                    label = { Text("Pravá") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                FilterChip(
                    selected = tumorSide == Tnm9Engine.TumorSide.LEFT,
                    onClick = { tumorSide = Tnm9Engine.TumorSide.LEFT },
                    label = { Text("Ľavá") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            // Výber uzlinových staníc – teraz bez pevnej výšky, aby scrollovala celá stránka
            // Výber uzlinových staníc – fix pre infinity height
            Text("Vyber postihnuté uzlinové stanice", style = MaterialTheme.typography.titleSmall)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp), // ✅ dôležité pre Compose layout!
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(allStations) { station ->
                    FilterChip(
                        selected = selectedStations.contains(station),
                        onClick = {
                            selectedStations =
                                if (selectedStations.contains(station))
                                    selectedStations - station
                                else
                                    selectedStations + station
                        },
                        label = { Text(station) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }


            Spacer(Modifier.height(12.dp))

            // Metastázy
            Text("Metastázy", style = MaterialTheme.typography.titleSmall)
            UpstageCheckbox("Intratorakálne metastázy (M1a)", intrathoracicMet) { intrathoracicMet = it }
            OutlinedTextField(
                value = extrathoracicCount,
                onValueChange = { extrathoracicCount = it },
                label = { Text("Počet extratorakálnych metastáz") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = extrathoracicOrgans,
                onValueChange = { extrathoracicOrgans = it },
                label = { Text("Počet postihnutých orgánov") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val tInput = Tnm9Engine.TInput(
                        size = tumorSize.toDoubleOrNull(),
                        up = Tnm9Engine.TUpstaging(
                            visceralPleuralInvasion = visceralPleura,
                            mainBronchusInvolvement = mainBronchus,
                            atelectasis = atelectasis,
                            chestWall = chestWall,
                            mediastinum = mediastinum,
                            separateNoduleSameLobe = separateNoduleSameLobe,
                            separateNoduleDifferentLobe = separateNoduleDifferentLobe
                        )
                    )
                    val nInput = Tnm9Engine.NInput(
                        tumorSide = tumorSide,
                        selectedStations = selectedStations
                    )
                    val mInput = Tnm9Engine.MInput(
                        intrathoracicMet = intrathoracicMet,
                        extrathoracicMetCount = extrathoracicCount.toIntOrNull() ?: 0,
                        extrathoracicOrgansInvolved = extrathoracicOrgans.toIntOrNull() ?: 0
                    )

                    result = Tnm9Engine.evaluate(tInput, nInput, mInput)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vypočítať TNM a štádium")
            }

            result?.let { r ->
                Spacer(Modifier.height(16.dp))
                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Výsledok", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("TNM: ${r.lineTNM()}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("Klinické štádium: ${r.clinicalStage()}", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun UpstageCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
