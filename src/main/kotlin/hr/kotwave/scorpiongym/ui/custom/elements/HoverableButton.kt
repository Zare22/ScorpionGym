package hr.kotwave.scorpiongym.ui.custom.elements

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.hoverable(onEnter: () -> Unit, onExit: () -> Unit): Modifier {
    return this
        .onPointerEvent(PointerEventType.Enter) { onEnter() }
        .onPointerEvent(PointerEventType.Exit) { onExit() }
}

@Composable
fun HoverableButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String
) {
    var isHovered by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(if (isHovered || isFocused) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.primary)

    Button(
        onClick = {
            onClick()
            isFocused = false
        },
        modifier = modifier
            .padding(16.dp)
            .hoverable(
                onEnter = { isHovered = true },
                onExit = { isHovered = false }
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = MaterialTheme.colors.onPrimary
        )
    ) {
        Text(text = text)
    }
}