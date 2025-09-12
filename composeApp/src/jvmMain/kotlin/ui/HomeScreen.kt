package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.pneumocalc.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("PneumoCalc - Home") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Button(onClick = { onNavigate(Screen.Light) }) {
                Text("Lightove kritériá")
            }

            Button(onClick = { onNavigate(Screen.ABG) }) {
                Text("ABG Analyzer")
            }
            Button(onClick = { onNavigate(Screen.BODE) }) {
                Text("BODE Index")
            }
            Button(onClick = { onNavigate(Screen.CAT) }) {
                Text("CAT Test")
            }
            Button(onClick = { onNavigate(Screen.CURB65) }) {
                Text("CURB-65")
            }
            Button(onClick = { onNavigate(Screen.Fleischner) }) {
                Text("Fleischner Guidelines")
            }
            Button(onClick = { onNavigate(Screen.Geneva) }) {
                Text("Geneva Score")
            }
            Button(onClick = { onNavigate(Screen.mMRC) }) {
                Text("mMRC Dyspnoe Scale")
            }

            Button(onClick = { onNavigate(Screen.Steroid) }) {
                Text("Steroid Conversion")
            }
            Button(onClick = { onNavigate(Screen.PSI) }) { Text("PSI/PORT") }
            Button(onClick = { onNavigate(Screen.PESI) }) { Text("PESI") }
            Button(onClick = { onNavigate(Screen.RESECT90) }) { Text("RESECT-90") }
            Button(onClick = { onNavigate(Screen.SPN) }) { Text("SPN Risk (Mayo)") }
            Button(onClick = { onNavigate(Screen.Oxygenation) }) { Text("SpO₂/FiO₂ Ratio") }
            Button(onClick = { onNavigate(Screen.STOPBANG) }) { Text("STOP-BANG") }






        }
    }
}
