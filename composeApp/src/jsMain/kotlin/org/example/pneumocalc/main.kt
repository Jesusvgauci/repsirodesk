package org.example.pneumocalc

import org.jetbrains.compose.web.renderComposable
import ui.PneumoTheme
import androidx.compose.runtime.Composable

fun main() {
    renderComposable(rootElementId = "root") {
        AppRoot()
    }
}

@Composable
private fun AppRoot() {
    PneumoTheme {
        App()
    }
}
