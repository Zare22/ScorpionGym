package hr.kotwave.scorpiongym.ui.custom.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> Dropdown(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    focusRequester: FocusRequester,
    nextFocusRequester: FocusRequester,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (!readOnly) {
                    onExpandedChange(it)
                }
            }
        ) {
            OutlinedTextField(
                value = selectedItem?.let(itemLabel) ?: "",
                onValueChange = {},
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent {
                        if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                            nextFocusRequester.requestFocus()
                            true
                        } else false
                    }
                    .pointerHoverIcon(PointerIcon.Hand, true),
                readOnly = true,
                trailingIcon = {
                    if (!readOnly) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            onItemSelected(item)
                            onExpandedChange(false)
                        }) {
                        Text(itemLabel(item))
                    }
                }
            }
        }
    }
}