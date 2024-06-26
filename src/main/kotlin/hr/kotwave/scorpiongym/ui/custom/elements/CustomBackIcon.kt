package hr.kotwave.scorpiongym.ui.custom.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun CustomBackIcon(
    modifier: Modifier = Modifier,
    navigator: Navigator,
) {
    Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowBack,
        contentDescription = "Back",
        tint = MaterialTheme.colors.onSurface,
        modifier = modifier
            .padding(16.dp)
            .clickable { navigator.pop() }
    )
}