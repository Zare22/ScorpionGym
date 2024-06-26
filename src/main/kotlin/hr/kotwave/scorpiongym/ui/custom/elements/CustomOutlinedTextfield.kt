package hr.kotwave.scorpiongym.ui.custom.elements

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun FocusableOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    currentFocusRequester: FocusRequester,
    nextFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .focusRequester(currentFocusRequester)
            .onPreviewKeyEvent {
                if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                    nextFocusRequester.requestFocus()
                    true
                } else false
            }
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onValueChange(value.copy(selection = TextRange(value.text.length)))
                }
            },
        readOnly = readOnly,
        maxLines = maxLines
    )
}