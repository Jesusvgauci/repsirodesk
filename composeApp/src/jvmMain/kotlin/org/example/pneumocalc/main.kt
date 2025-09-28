package org.example.pneumocalc

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.res.useResource
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import ui.*  // 🔹 toto pridaj, lebo Theme je v package ui


// desktopový entrypoint
fun main() = application {
    // načítanie ikony okna (nepovinné)
    val windowIconPainter = remember {
        runCatching { useResource("logo.png", ::loadImageBitmap) }.getOrNull()
            ?.let { BitmapPainter(it) }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "RespiroDesk",
        icon = windowIconPainter
    ) {
        PneumoTheme {
            // využitie spoločnej funkcie App() z commonMain
            App()
        }
    }
}
