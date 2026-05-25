package hr.kotwave.scorpiongym.ui.custom.elements

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Provides an M3 [MaterialTheme] whose ColorScheme mirrors the surrounding M2
 * palette. Used by the M3 picker dialogs ([DatePickerField], [DateTimePickerField])
 * to stay visually consistent with the rest of the (Material 2) app.
 */
@Composable
internal fun MatchedM3Theme(content: @Composable () -> Unit) {
    val m2 = androidx.compose.material.MaterialTheme.colors
    val scheme = if (m2.isLight) {
        lightColorScheme(
            primary = m2.primary,
            onPrimary = m2.onPrimary,
            secondary = m2.secondary,
            onSecondary = m2.onSecondary,
            background = m2.background,
            onBackground = m2.onBackground,
            surface = m2.surface,
            onSurface = m2.onSurface,
            surfaceVariant = m2.surface,
            onSurfaceVariant = m2.onSurface,
        )
    } else {
        darkColorScheme(
            primary = m2.primary,
            onPrimary = m2.onPrimary,
            secondary = m2.secondary,
            onSecondary = m2.onSecondary,
            background = m2.background,
            onBackground = m2.onBackground,
            surface = m2.surface,
            onSurface = m2.onSurface,
            surfaceVariant = m2.surface,
            onSurfaceVariant = m2.onSurface,
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
