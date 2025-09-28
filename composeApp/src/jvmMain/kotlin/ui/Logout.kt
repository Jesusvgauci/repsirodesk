package ui

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
actual fun Logout(onLogout: () -> Unit) {
    // Na desktope logout nič nerobí
    TextButton(onClick = { onLogout() }) {
        Text("Odhlásiť", color = MaterialTheme.colorScheme.onPrimary)
    }
}