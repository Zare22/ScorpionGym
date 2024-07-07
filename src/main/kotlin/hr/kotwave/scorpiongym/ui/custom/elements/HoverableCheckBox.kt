package hr.kotwave.scorpiongym.ui.custom.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup


@Composable
fun HoverableCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    hoverText: String,
    modifier: Modifier = Modifier,
    showPopupOnHover: Boolean = false,
) {
    val showPopup = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> showPopup.value = true
                            PointerEventType.Exit -> showPopup.value = false
                            else -> Unit
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )

        if (showPopupOnHover && showPopup.value) {
            Popup(
                alignment = Alignment.Center,
                offset = IntOffset(5, -35)
            ) {
                HoverPopupContent(text = hoverText)
            }
        }
    }
}

@Composable
fun HoverPopupContent(text: String) {
    Box(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}