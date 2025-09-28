package org.example.pneumocalc

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.reflect.KClass
import ui.*
import util.*

// Sekcie aplikácie
enum class MainSection(val title: String) {
    AMBULANTNE("Ambulantné vyšetrenia"),
    KALKULATORY("Kalkulačky a dotazníky"),
    FUNKCNE("Funkčné vyšetrenia pľúc")
}

// Prepojenie typov obrazoviek na konkrétne composable
private val screenRegistry: Map<KClass<out Screen>, @Composable (Screen, (Screen) -> Unit) -> Unit> =
    mapOf(
        Screen.Home::class to { _, navigate -> HomeScreen(onNavigate = navigate) },
        Screen.Light::class to { _, _ -> LightScreen() },
        Screen.ABG::class to { _, _ -> ABGScreen() },
        Screen.BODE::class to { _, _ -> BODEScreen() },
        Screen.Fleischner::class to { _, _ -> FleischnerScreen() },
        Screen.Geneva::class to { _, _ -> GenevaScreen() },
        Screen.mMRC::class to { _, _ -> mMRCScreen() },
        Screen.Steroid::class to { _, _ -> SteroidScreen() },
        Screen.PSI::class to { _, _ -> PSIScreen() },
        Screen.PESI::class to { _, _ -> PESIScreen() },
        Screen.RESECT90::class to { _, _ -> RESECT90Screen() },
        Screen.SPN::class to { _, _ -> SPNScreen() },
        Screen.Oxygenation::class to { _, _ -> OxygenationScreen() },
        Screen.STOPBANG::class to { _, _ -> STOPBANGScreen() },
        Screen.CAT::class to { _, _ -> CATScreen() },
        Screen.TNM9::class to { _, _ -> Tnm9Screen() }
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var currentUser by remember { mutableStateOf<String?>(null) }

    PneumoTheme {
        if (currentUser == null) {
            // 🔹 Login obrazovka (iba pre web)
            LoginScreen { email -> currentUser = email }
        } else {
            // 🔹 Hlavná aplikácia
            MainAppContent(currentUser!!)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(currentUser: String) {
    var currentSection by remember { mutableStateOf(MainSection.KALKULATORY) }
    var currentScreen by remember { mutableStateOf<Screen?>(null) }
    var functionalSubscreen by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val evaluationViewModel = remember { EvaluationViewModel() }

    Surface(color = MaterialTheme.colorScheme.background) {
        Scaffold(
            topBar = {
                var menuExpanded by remember { mutableStateOf(false) }

                CenterAlignedTopAppBar(
                    navigationIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // ikona späť
                            if (currentScreen != null || functionalSubscreen != null) {
                                IconButton(
                                    onClick = {
                                        when {
                                            functionalSubscreen != null -> functionalSubscreen = null
                                            else -> currentScreen = null
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = "Späť",
                                        tint = Color.White
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                            }

                            // menu sekcií
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Menu",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    MainSection.values().forEach { sec ->
                                        DropdownMenuItem(
                                            text = { Text(sec.title) },
                                            onClick = {
                                                currentSection = sec
                                                currentScreen = null
                                                functionalSubscreen = null
                                                menuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Respiro",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Desk",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFB2EBF2)
                            )
                        }
                    },
                    actions = {
                        // 🔹 Odhlásenie – cross-platform riešenie
                        TextButton(
                            onClick = {
                                logout { /* callback po logoute */ }
                            }
                        ) {
                            Text("Odhlásiť", color = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF00ACC1),
                        titleContentColor = Color.White
                    )
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentSection) {
                    MainSection.KALKULATORY -> {
                        if (currentScreen == null) {
                            CalculatorList { selected -> currentScreen = selected }
                        } else {
                            val renderer = screenRegistry[currentScreen!!::class]
                            renderer?.invoke(currentScreen!!) {
                                currentScreen = null
                            }
                        }
                    }
                    MainSection.AMBULANTNE -> {
                        AnamnezaScreen(snackbarHostState = snackbarHostState)
                    }
                    MainSection.FUNKCNE -> {
                        if (functionalSubscreen == null) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item { FunctionalCard("Interpretácia funkčných vyšetrení") { functionalSubscreen = "EVAL" } }
                                item { FunctionalCard("Korekcia difúzie podľa hemoglobínu") { functionalSubscreen = "DLCO" } }
                                item { FunctionalCard("Predikcia pooperačnej funkcie") { functionalSubscreen = "PPO" } }
                            }
                        } else {
                            when (functionalSubscreen) {
                                "EVAL" -> EvaluationScreen(viewModel = evaluationViewModel, snackbarHostState = snackbarHostState)
                                "DLCO" -> DlcoHbScreen(snackbarHostState = snackbarHostState)
                                "PPO"  -> PpoScreen(snackbarHostState = snackbarHostState)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FunctionalCard(label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}
