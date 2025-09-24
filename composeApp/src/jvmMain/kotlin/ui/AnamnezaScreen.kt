package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// pevná šírka stĺpca pre názvy otázok
private val LabelWidth = 160.dp

@Composable
fun AnamnezaScreen(snackbarHostState: SnackbarHostState) {
    // ==== STAVY ====
    var dyspnoe by remember { mutableStateOf("dýcha sa mu dobre") }
    var duration by remember { mutableStateOf("") }
    var durationUnit by remember { mutableStateOf("dni") }
    var orthopnoe by remember { mutableStateOf("bez zhoršenia v ľahu a v noci") }

    var kasel by remember { mutableStateOf("nekašle") }
    var kaselType by remember { mutableStateOf("suchý") }
    var kaselDuration by remember { mutableStateOf("") }
    var kaselDurationUnit by remember { mutableStateOf("dni") }
    var kaselTime by remember { mutableStateOf("cez deň") }

    var hemoptysis by remember { mutableStateOf("nevykašliava krv") }
    var teploty by remember { mutableStateOf("nemal teploty") }
    var nocnePotenie by remember { mutableStateOf("nemá nočné potenie") }
    var ger by remember { mutableStateOf("záha nepáli") }

    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    // ==== LIVE GENEROVANÝ TEXT (bez tlačidla) ====
    val result by remember(
        dyspnoe, duration, durationUnit, orthopnoe,
        kasel, kaselType, kaselDuration, kaselDurationUnit, kaselTime,
        hemoptysis, teploty, nocnePotenie, ger
    ) {
        derivedStateOf {
            buildString {
                append("Subj: ")

                // Dušnosť
                if (dyspnoe.isNotBlank()) append("$dyspnoe, ")
                if (duration.isNotBlank()) append("tažkosti má približne $duration $durationUnit, ")
                if (orthopnoe.isNotBlank()) append("$orthopnoe, ")

                // Kašeľ – iba ak pacient kašle
                if (kasel == "kašle") {
                    append("$kasel, ")
                    if (kaselType.isNotBlank()) append("kašeľ je $kaselType, ")
                    if (kaselDuration.isNotBlank()) append("trvanie približne $kaselDuration $kaselDurationUnit, ")
                    if (kaselTime.isNotBlank()) append("$kaselTime, ")
                } else if (kasel.isNotBlank()) {
                    append("$kasel, ")
                }

                // Ďalšie príznaky
                if (hemoptysis.isNotBlank()) append("$hemoptysis, ")
                if (teploty.isNotBlank()) append("$teploty, ")
                if (nocnePotenie.isNotBlank()) append("$nocnePotenie, ")
                if (ger.isNotBlank()) append("$ger.")

                // odstránenie nadbytočnej bodkočiarky na konci
                if (endsWith("; ")) {
                    delete(length - 2, length)
                    append(".")
                }
            }
        }
    }

    ScrollableScreen {
        // === Dušnosť ===
        SectionCard(title = "🫁 Dušnosť") {
            DropdownQuestionRow(
                label = "Dušnosť",
                options = listOf("dýcha sa mu dobre", "zadýcha sa pri minimálnej námahe", "zadýcha sa pri väčšej námahe"),
                selected = dyspnoe,
                onSelected = { dyspnoe = it }
            )
            DurationInput(
                label = "Trvanie",
                value = duration,
                onValueChange = { duration = it },
                unit = durationUnit,
                onUnitChange = { durationUnit = it }
            )
            DropdownQuestionRow(
                label = "Zhoršenie",
                options = listOf("bez zhoršenia v ľahu a v noci", "dýchavica sa zhoršuje v noci", "dýchavica sa zhoršuje sa v ľahu"),
                selected = orthopnoe,
                onSelected = { orthopnoe = it }
            )
        }

        // === Kašeľ ===
        SectionCard(title = "🤧 Kašeľ") {
            TwinOptionSegmentRow(
                label = "Kašeľ",
                options = listOf("nekašle", "kašle"),
                selected = kasel,
                onSelected = { kasel = it }
            )
            DropdownQuestionRow(
                label = "Typ kašľa",
                options = listOf("suchý", "produktívny"),
                selected = kaselType,
                onSelected = { kaselType = it }
            )
            DurationInput(
                label = "Trvanie kašľa",
                value = kaselDuration,
                onValueChange = { kaselDuration = it },
                unit = kaselDurationUnit,
                onUnitChange = { kaselDurationUnit = it }
            )
            DropdownQuestionRow(
                label = "Čas výskytu",
                options = listOf("hlavne cez deň", "hlavne v noci", "celodenne"),
                selected = kaselTime,
                onSelected = { kaselTime = it }
            )
        }

        // === Ďalšie príznaky ===
        SectionCard(title = "📋 Ďalšie príznaky") {
            TwinOptionSegmentRow(
                label = "Hemoptýza",
                options = listOf("nevykašliava krv", "vykašliava krv"),
                selected = hemoptysis,
                onSelected = { hemoptysis = it }
            )
            TwinOptionSegmentRow(
                label = "Teploty",
                options = listOf("nemal teploty", "mal teploty"),
                selected = teploty,
                onSelected = { teploty = it }
            )
            TwinOptionSegmentRow(
                label = "Nočné potenie",
                options = listOf("nemá nočné potenie", "má nočné potenie"),
                selected = nocnePotenie,
                onSelected = { nocnePotenie = it }
            )
            TwinOptionSegmentRow(
                label = "GERD",
                options = listOf("záha nepáli", "udáva pálenie záhy "),
                selected = ger,
                onSelected = { ger = it }
            )
        }

        // === VŽDY VIDITEĽNÁ SPRÁVA ===
        ResultCard(
            text = result,
            onCopy = {
                clipboard.setText(AnnotatedString(result))
                scope.launch { snackbarHostState.showSnackbar("Skopírované do schránky") }
            }
        )
        Spacer(Modifier.height(8.dp))
    }
}

/* ---------- Layout kontajnery ---------- */

@Composable
fun ScrollableScreen(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        content = content
    )
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

/* ---------- Otázky / riadky ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownQuestionRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(LabelWidth)
        )
        Spacer(Modifier.width(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .width(240.dp),
                label = null
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DurationInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit
) {
    val units = listOf("dni", "mesiacov", "rokov")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(LabelWidth)
        )
        Spacer(Modifier.width(12.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { new ->
                if (new.isEmpty() || new.all { it.isDigit() }) onValueChange(new)
            },
            singleLine = true,
            modifier = Modifier.width(96.dp),
            label = null
        )

        Spacer(Modifier.width(8.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.weight(1f)
        ) {
            units.forEachIndexed { index, u ->
                SegmentedButton(
                    selected = unit == u,
                    onClick = { onUnitChange(u) },
                    shape = SegmentedButtonDefaults.itemShape(index, units.size),
                    colors = segmentedColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(u)
                }
            }
        }
    }
}

@Composable
fun TwinOptionSegmentRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(LabelWidth)
        )
        Spacer(Modifier.width(12.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.weight(1f)
        ) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = selected == option,
                    onClick = { onSelected(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    colors = segmentedColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(option)
                }
            }
        }
    }
}

/* ---------- Farby pre segmented tlačidlá ---------- */

@Composable
private fun segmentedColors(): SegmentedButtonColors =
    SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        inactiveContainerColor = MaterialTheme.colorScheme.surface,
        inactiveContentColor = MaterialTheme.colorScheme.onSurface,
        disabledActiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledActiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledInactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledInactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
