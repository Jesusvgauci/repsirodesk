package org.example.pneumocalc

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import ui.PneumoTheme

// webový entrypoint
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.getElementById("root")!!) {
        PneumoTheme {
            Box(modifier = Modifier.fillMaxSize()) {
                App()
            }
        }
    }
}
