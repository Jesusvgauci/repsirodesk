// Theme.kt
package ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🎨 Vlastné farby
private val TealPrimary = Color(0xFF00ACC1)       // sýta tyrkysová
private val TealLight = Color(0xFFB2EBF2)        // svetlá tyrkysová (pozadie tlačidiel)
private val TealBackground = Color(0xFFE0F7FA)   // veľmi jemná tyrkysová (pozadie appky)

private val CustomLightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealLight,
    onPrimaryContainer = Color.Black,
    background = TealBackground,
    onBackground = Color.Black,
    surface = TealBackground,
    onSurface = Color.Black
)

private val CustomDarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White
)

@Composable
fun PneumoTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) CustomDarkColors else CustomLightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
