package org.example.pneumocalc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CalculatorList(onSelect: (Screen) -> Unit) {
    val kalkulatory = listOf(
        Screen.Light to "Lightove kritériá",
        Screen.ABG to "Interpretácia acidobazickej rovnováhy",
        Screen.BODE to "BODE index",
        Screen.Fleischner to "Fleischnerove kritériá pre sledovanie nodulov",
        Screen.Geneva to "Revidované ženevské skóre na odhad pravdepodobnosti pľúcnej embólie",
        Screen.Steroid to "Konverzia kortikosteroidov",
        Screen.PSI to "PSI skóre",
        Screen.PESI to "PESI skóre",
        Screen.RESECT90 to "RESECT-90",
        Screen.SPN to "SPN skóre",
        Screen.Oxygenation to "Oxygenačný index",
        Screen.TNM9 to "TNM klasifikácia"
    )

    val dotazniky = listOf(
        Screen.CAT to "CAT skóre",
        Screen.mMRC to "mMRC skóre",
        Screen.STOPBANG to "STOP-BANG"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nadpis kalkulátory
        item {
            Text(
                text = "KALKULÁTORY",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }
        items(kalkulatory) { (screen, title) ->
            CalcCard(screen, title, onSelect)
        }

        // Nadpis dotazníky
        item {
            Text(
                text = "DOTAZNÍKY",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )
        }
        items(dotazniky) { (screen, title) ->
            CalcCard(screen, title, onSelect)
        }
    }
}

@Composable
private fun CalcCard(screen: Screen, title: String, onSelect: (Screen) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(screen) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}
