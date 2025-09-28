package org.example.pneumocalc

import androidx.compose.runtime.*
import org.jetbrains.compose.web.dom.*
import ui.*              // AppScaffold, AppTopBar, AppCard, AppButton, AppTextField...
import util.logout
import ui.*  // ak ešte nemáš, dočasne zakomentuj

// Sekcie aplikácie
enum class MainSection(val title: String) {
    AMBULANTNE("Ambulantné vyšetrenia"),
    KALKULATORY("Kalkulačky a dotazníky"),
    FUNKCNE("Funkčné vyšetrenia pľúc")
}

@Composable
fun App() = AppWeb()

@Composable
fun AppWeb() {
    var currentUser by remember { mutableStateOf<String?>(null) }
    var currentSection by remember { mutableStateOf(MainSection.KALKULATORY) }
    var currentScreenId by remember { mutableStateOf<String?>(null) }
    var functionalSubscreen by remember { mutableStateOf<String?>(null) }

    if (currentUser == null) {
        LoginWeb { email -> currentUser = email }   // DOM login verzia
        return
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                onBack = if (currentScreenId != null || functionalSubscreen != null) ({
                    if (functionalSubscreen != null) functionalSubscreen = null else currentScreenId = null
                }) else null,
                actions = {
                    AppButton(onClick = { logout { currentUser = null } }, text = "Odhlásiť")
                },
                menu = {
                    // prepínač sekcií
                    Select(attrs = {
                        onChange { ev ->
                            currentSection = when (ev.value) {
                                "AMBULANTNE" -> MainSection.AMBULANTNE
                                "FUNKCNE" -> MainSection.FUNKCNE
                                else -> MainSection.KALKULATORY
                            }
                            currentScreenId = null
                            functionalSubscreen = null
                        }
                        attr("style","background:transparent;color:white;")
                        value(
                            when (currentSection) {
                                MainSection.AMBULANTNE -> "AMBULANTNE"
                                MainSection.FUNKCNE -> "FUNKCNE"
                                else -> "KALKULATORY"
                            }
                        )
                    }) {
                        Option(value = "KALKULATORY") { Text(MainSection.KALKULATORY.title) }
                        Option(value = "AMBULANTNE") { Text(MainSection.AMBULANTNE.title) }
                        Option(value = "FUNKCNE") { Text(MainSection.FUNKCNE.title) }
                    }
                }
            )
        }
    ) {
        when (currentSection) {
            MainSection.KALKULATORY -> {
                if (currentScreenId == null) {
                    AppCard {
                        H3 { Text("Kalkulačky") }
                        Ul {
                            Li { Button(attrs = { onClick { currentScreenId = "ABG" } }) { Text("ABG") } }
                            Li { Button(attrs = { onClick { currentScreenId = "BODE" } }) { Text("BODE") } }
                            Li { Button(attrs = { onClick { currentScreenId = "SPN" } }) { Text("SPN") } }
                            // pridáš ďalšie podľa potreby
                        }
                    }
                } else {
                    when (currentScreenId) {
                        "ABG" -> ABGWeb()
                        else  -> AppCard { Text("Obrazovka $currentScreenId bude doplnená pre web.") }
                    }
                }
            }

            MainSection.AMBULANTNE -> {
                AppCard { Text("Ambulantné – web verzia sa pripravuje.") }
            }

            MainSection.FUNKCNE -> {
                if (functionalSubscreen == null) {
                    AppCard {
                        H3 { Text("Funkčné vyšetrenia pľúc") }
                        Ul {
                            Li { Button(attrs = { onClick { functionalSubscreen = "EVAL" } }) { Text("Interpretácia funkcií") } }
                            Li { Button(attrs = { onClick { functionalSubscreen = "DLCO" } }) { Text("Korekcia difúzie") } }
                            Li { Button(attrs = { onClick { functionalSubscreen = "PPO" } }) { Text("Pooperačná predikcia") } }
                        }
                    }
                } else {
                    AppCard { Text("Subscreen $functionalSubscreen bude doplnený pre web.") }
                }
            }
        }
    }
}
