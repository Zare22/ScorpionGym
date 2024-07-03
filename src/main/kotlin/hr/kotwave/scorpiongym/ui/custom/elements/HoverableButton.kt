package hr.kotwave.scorpiongym.ui.custom.elements

import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.ui.theme.Shapes

@Composable
fun HoverableButton(
    text: String,
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    buttonBackgroundColor: Color = MaterialTheme.colors.primary,
    buttonContentColor: Color = MaterialTheme.colors.onPrimary,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    Button(
        onClick = {
            onClick()
        },
        modifier = modifier
            .padding(all = padding)
            .hoverable(interactionSource = interactionSource)
            .focusable(interactionSource = interactionSource),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = buttonBackgroundColor.copy(if (isHovered || isFocused) 0.60f else 1f),
            contentColor = buttonContentColor
        ),
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        shape = Shapes.medium
    ) {
        Text(text = text)
    }
}