package hr.kotwave.scorpiongym.ui.theme

import androidx.compose.foundation.DarkDefaultContextMenuRepresentation
import androidx.compose.foundation.LightDefaultContextMenuRepresentation
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.White

private val DarkColorPalette = darkColors(
    primary = White,
    primaryVariant = LightGray,
    secondary = White,
    background = DarkGray,
    surface = DarkGray,
    onPrimary = Black,
    onSecondary = Black,
    onBackground = White,
    onSurface = White
)

private val LightColorPalette = lightColors(
    primary = Black,
    primaryVariant = DarkGray,
    secondary = Black,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = Black,
    onSurface = Black
)

@Composable
fun ScorpionGymTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}