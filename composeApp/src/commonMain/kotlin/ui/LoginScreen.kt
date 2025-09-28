package ui

import androidx.compose.runtime.Composable

@Composable
expect fun LoginScreen(onLoginSuccess: (String) -> Unit)
