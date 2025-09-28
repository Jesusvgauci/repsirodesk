package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private val scope = MainScope()

@Composable
actual fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RespiroDesk", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val auth = window.asDynamic().auth
                        if (auth == null) {
                            error = "Firebase Auth nie je inicializované"
                            return@launch
                        }

                        val provider = js("new firebase.auth.GoogleAuthProvider()")

                        auth.signInWithPopup(provider).then { result: dynamic ->
                            val user = result.user
                            val email = user?.email as String
                            onLoginSuccess(email)

                            // uloženie do Firestore
                            val db = window.asDynamic().db
                            if (db != null) {
                                db.collection("users").doc(user.uid).set(
                                    js(
                                        """({
                                            email: "${'$'}email",
                                            name: "${'$'}{user.displayName}",
                                            role: "basic",
                                            createdAt: new Date()
                                        })"""
                                    )
                                )
                            }
                        }.catch { e: dynamic ->
                            error = e.message as? String ?: "Neznáma chyba"
                        }
                    } catch (e: Throwable) {
                        error = e.message ?: "Neznáma chyba"
                    }
                }
            }
        ) {
            Text("Prihlásiť sa cez Google")
        }

        Spacer(Modifier.height(16.dp))

        error?.let {
            Text("Chyba: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}
