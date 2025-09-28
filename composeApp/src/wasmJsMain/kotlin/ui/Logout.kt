package ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import kotlinx.browser.window

@Composable
actual fun Logout(onLogout: () -> Unit) {
    TextButton(onClick = {
        val auth = window.asDynamic().auth
        auth.signOut().then {
            onLogout()
        }
    }) {
        Text("Odhlásiť", color = MaterialTheme.colorScheme.onPrimary)
    }
}
