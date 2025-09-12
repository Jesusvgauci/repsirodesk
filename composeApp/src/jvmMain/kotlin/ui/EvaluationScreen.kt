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
import kotlinx.coroutines.launch
import org.example.pneumocalc.EvaluationViewModel
import ui.*

@Composable
fun EvaluationScreen(
    viewModel: EvaluationViewModel,
    snackbarHostState: SnackbarHostState
) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("SPIROMETRIA", "BD TEST")

    // --------- ZBER STAVOV Z VIEWMODELU ---------
    // z-skóre
    val fev1Z by viewModel.fev1Z.collectAsState()
    val fvcZ by viewModel.fvcZ.collectAsState()
    val ratioZ by viewModel.ratioZ.collectAsState()
    val tlcZ by viewModel.tlcZ.collectAsState()
    val rvTlcZ by viewModel.rvTlcZ.collectAsState()
    val dlcoZ by viewModel.dlcoZ.collectAsState()
    val kcoZ by viewModel.kcoZ.collectAsState()
    val vaZ by viewModel.vaZ.collectAsState()
    val sRaw by viewModel.sRaw.collectAsState()
    val zscoreResult by viewModel.zscoreResult.collectAsState()

    // % režim
    val fev1Pct by viewModel.fev1Pct.collectAsState()
    val fvcPct by viewModel.fvcPct.collectAsState()
    val ratioAbs by viewModel.ratioAbs.collectAsState()
    val tlcPct by viewModel.tlcPct.collectAsState()
    val rvTlcPct by viewModel.rvTlcPct.collectAsState()
    val dlcoPct by viewModel.dlcoPct.collectAsState()
    val kcoPct by viewModel.kcoPct.collectAsState()
    val vaPct by viewModel.vaPct.collectAsState()
    val percentResult by viewModel.percentResult.collectAsState()

    // BD test
    val fev1Pre by viewModel.fev1Pre.collectAsState()
    val fev1Post by viewModel.fev1Post.collectAsState()
    val fev1Pred by viewModel.fev1Pred.collectAsState()
    val fvcPre by viewModel.fvcPre.collectAsState()
    val fvcPost by viewModel.fvcPost.collectAsState()
    val fvcPred by viewModel.fvcPred.collectAsState()
    val bd2022 by viewModel.bd2022.collectAsState()
    val bd2005 by viewModel.bd2005.collectAsState()
    var useZscore by remember { mutableStateOf(true) }
    var useBd2022 by remember { mutableStateOf(true) }
    val ratioZPre by viewModel.ratioZPre.collectAsState()
    val ratioZPost by viewModel.ratioZPost.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            // === SPIROMETRIA ===
            0 -> Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text("Spirometria a bodypletyzmografia", style = MaterialTheme.typography.titleMedium)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        if (useZscore) "Režim: z-skóre (ATS/ERS 2022) – odporúčané"
                        else "Režim: % pred (ATS/ERS 2005)",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = useZscore, onCheckedChange = { useZscore = it })
                }

                if (useZscore) {
                    NumberField("FEV₁ (z-skóre)", fev1Z) { viewModel.fev1Z.value = it }
                    NumberField("FVC (z-skóre)", fvcZ) { viewModel.fvcZ.value = it }
                    NumberField("FEV₁/FVC (z-skóre)", ratioZ) { viewModel.ratioZ.value = it }
                    NumberField("TLC (z-skóre)", tlcZ) { viewModel.tlcZ.value = it }
                    NumberField("RV/TLC (z-skóre)", rvTlcZ) { viewModel.rvTlcZ.value = it }
                    NumberField("DLCO (z-skóre)", dlcoZ) { viewModel.dlcoZ.value = it }
                    NumberField("KCO (z-skóre)", kcoZ) { viewModel.kcoZ.value = it }
                    NumberField("VA (z-skóre)", vaZ) { viewModel.vaZ.value = it }
                    NumberField("sRaw (hodnota)", sRaw) { viewModel.sRaw.value = it }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.calculateZscore() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Vyhodnotiť") }

                    if (zscoreResult.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        CopyableResultCard(
                            text = zscoreResult,
                            onCopy = {
                                clipboard.setText(AnnotatedString(zscoreResult))
                                scope.launch { snackbarHostState.showSnackbar("Skopírované do schránky") }
                            }
                        )
                    }
                } else {
                    NumberField("FEV₁ (% pred)", fev1Pct) { viewModel.fev1Pct.value = it }
                    NumberField("FVC (% pred)", fvcPct) { viewModel.fvcPct.value = it }
                    NumberField("FEV₁/FVC (% pred)", ratioAbs) { viewModel.ratioAbs.value = it }
                    NumberField("TLC (% pred)", tlcPct) { viewModel.tlcPct.value = it }
                    NumberField("RV/TLC (% pred)", rvTlcPct) { viewModel.rvTlcPct.value = it }
                    NumberField("DLCO (% pred)", dlcoPct) { viewModel.dlcoPct.value = it }
                    NumberField("KCO (% pred)", kcoPct) { viewModel.kcoPct.value = it }
                    NumberField("VA (% pred)", vaPct) { viewModel.vaPct.value = it }
                    NumberField("sRaw (hodnota)", sRaw) { viewModel.sRaw.value = it }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.calculatePercent() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Vyhodnotiť (% pred)") }

                    if (percentResult.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        CopyableResultCard(
                            text = percentResult,
                            onCopy = {
                                clipboard.setText(AnnotatedString(percentResult))
                                scope.launch { snackbarHostState.showSnackbar("Skopírované do schránky") }
                            }
                        )
                    }
                }
            }

            // === BD TEST ===
            1 -> Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text("Bronchodilatačný test", style = MaterialTheme.typography.titleMedium)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        if (useBd2022) "Režim: ERS/ATS 2022 – odporúčané"
                        else "Režim: ATS/ERS 2005 (≥12 % a ≥200 ml)",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = useBd2022, onCheckedChange = { useBd2022 = it })
                }

                NumberField("FEV₁ pred (L)", fev1Pre) { viewModel.fev1Pre.value = it }
                NumberField("FEV₁ po (L)", fev1Post) { viewModel.fev1Post.value = it }
                NumberField("FEV₁ predik. (L)", fev1Pred) { viewModel.fev1Pred.value = it }

                Spacer(Modifier.height(8.dp))
                Text("FVC – voliteľné", style = MaterialTheme.typography.labelLarge)
                NumberField("FVC pred (L)", fvcPre) { viewModel.fvcPre.value = it }
                NumberField("FVC po (L)", fvcPost) { viewModel.fvcPost.value = it }
                NumberField("FVC predik. (L)", fvcPred) { viewModel.fvcPred.value = it }

                Spacer(Modifier.height(8.dp))
                Text("FEV₁/FVC – voliteľné (z-skóre)", style = MaterialTheme.typography.labelLarge)
                NumberField("Pomer FEV₁/FVC pred (z-skóre)", ratioZPre)  { viewModel.ratioZPre.value  = it }
                NumberField("Pomer FEV₁/FVC po (z-skóre)",   ratioZPost) { viewModel.ratioZPost.value = it }

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (useBd2022) viewModel.calculateBd2022() else viewModel.calculateBd2005()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Vyhodnotiť BD test") }

                val textToShow = if (useBd2022) bd2022 else bd2005
                if (textToShow.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    CopyableResultCard(
                        text = textToShow,
                        onCopy = {
                            clipboard.setText(AnnotatedString(textToShow))
                            scope.launch { snackbarHostState.showSnackbar("Skopírované do schránky") }
                        }
                    )
                }
            }
        }
    }
}

/** Jednotná kartička na zobrazenie výsledku + tlačidlo kopírovania. */
@Composable
private fun CopyableResultCard(
    text: String,
    onCopy: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.primaryContainer,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text, color = colors.onPrimaryContainer)
            IconButton(
                onClick = onCopy,
                modifier = Modifier.align(Alignment.End)
            ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Skopírovať") }
        }
    }
}
