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
import calculators.ABGAnalysisResult
import calculators.analyzeABG
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ABGScreen() {
    var ph by remember { mutableStateOf("") }
    var paco2Kpa by remember { mutableStateOf("") } // vstup v kPa
    var hco3 by remember { mutableStateOf("") }
    var na by remember { mutableStateOf("") }
    var cl by remember { mutableStateOf("") }
    var albumin by remember { mutableStateOf("") }

    // Tri-stav: null = nešpecifikované, 0 = akútna, 1 = chronická
    var respType: Int? by remember { mutableStateOf<Int?>(null) }

    var result by remember { mutableStateOf<String?>(null) }

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
            // 🔹 InfoCard zo Components.kt
            InfoCard(
                text = "Interpretácia acidobázickej rovnováhy (ABR) je kalkulátor, ktorého cieľom je určiť, či má pacient poruchu pH vnútorného prostredia a aký je jej typ a závažnosť. Vyhodnocuje sa na základe výsledkov krvných plynov (pH, pCO₂, HCO₃⁻, BE) a často aj elektrolytov."
            )

            OutlinedTextField(
                value = ph,
                onValueChange = { ph = it },
                label = { Text("pH") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = paco2Kpa,
                onValueChange = { paco2Kpa = it },
                label = { Text("PaCO₂ (kPa)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = hco3,
                onValueChange = { hco3 = it },
                label = { Text("HCO₃⁻ (mmol/L)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = na,
                onValueChange = { na = it },
                label = { Text("Na⁺ (mmol/L) – voliteľné") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cl,
                onValueChange = { cl = it },
                label = { Text("Cl⁻ (mmol/L) – voliteľné") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = albumin,
                onValueChange = { albumin = it },
                label = { Text("Albumín (g/L) – voliteľné") },
                modifier = Modifier.fillMaxWidth()
            )

            // Výber akútna / chronická
            Text("Typ respiračnej poruchy (voliteľné)", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = respType == 0,
                    onClick = { respType = if (respType == 0) null else 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = { Text("Akútna") }
                )
                SegmentedButton(
                    selected = respType == 1,
                    onClick = { respType = if (respType == 1) null else 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = { Text("Chronická") }
                )
            }

            Button(
                onClick = {
                    val phVal = parseDoubleOrNull(ph)
                    val paco2KpaVal = parseDoubleOrNull(paco2Kpa)
                    val hco3Val = parseDoubleOrNull(hco3)
                    val naVal = parseDoubleOrNull(na)
                    val clVal = parseDoubleOrNull(cl)
                    val albValGL = parseDoubleOrNull(albumin)

                    if (phVal != null && paco2KpaVal != null && hco3Val != null) {
                        val res: ABGAnalysisResult = analyzeABG(
                            ph = phVal,
                            paCO2_kPa = paco2KpaVal,
                            hco3 = hco3Val,
                            na = naVal,
                            cl = clVal,
                            albumin_g_per_L = albValGL,
                            respType = respType
                        )
                        result = buildString {
                            appendLine("📌 Primárna porucha: ${res.primaryDisorder}")
                            res.expectedPaCO2_kPa?.let {
                                appendLine("🌡 Očak. PaCO₂: ${it.format(2)} kPa")
                            }
                            res.expectedHCO3?.let {
                                appendLine("🧪 Očak. HCO₃⁻: ${it.format(1)} mmol/L")
                            }
                            res.anionGap?.let {
                                appendLine("🧮 Aniónová medzera: ${it.format(1)} mmol/L")
                            }
                            res.anionGapCorrected?.let {
                                appendLine("🧮 AG (korig.): ${it.format(1)} mmol/L")
                            }
                            if (res.notes.isNotEmpty()) {
                                appendLine("\nPoznámky:")
                                res.notes.forEach { appendLine("• $it") }
                            }
                        }
                    } else {
                        result = "Zadajte platné hodnoty pH, PaCO₂ (kPa) a HCO₃⁻."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyhodnotiť")
            }

            // Výsledok cez ResultCard
            result?.let { r ->
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

// Helper funkcia – tolerantná na bodku aj čiarku
private fun parseDoubleOrNull(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()

// Multiplatform-friendly Double.format
private fun Double.format(digits: Int): String {
    val factor = 10.0.pow(digits)
    return (round(this * factor) / factor).toString()
}
