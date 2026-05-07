package hr.kotwave.scorpiongym.ui.custom.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.theme.Shapes

/**
 * Standard chrome for an entity's edit/create form: titled [Surface] with a
 * back button and a green save button at the bottom. Tab from the save
 * button wraps focus back to [firstFocusRequester].
 *
 * The form fields go in [content]. Callers own field state, validation, and
 * the save lambda's `entity.copy(...)` logic.
 */
@Composable
fun CrudDetailsScaffold(
    title: String,
    isNew: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    firstFocusRequester: FocusRequester,
    saveFocusRequester: FocusRequester,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        elevation = 4.dp,
        shape = Shapes.large,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = title, style = MaterialTheme.typography.h2)
            }
            Spacer(modifier = Modifier.height(16.dp))

            content()

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HoverableButton(
                    onClick = onBack,
                    text = "Povratak",
                )
                HoverableButton(
                    modifier = Modifier
                        .focusRequester(saveFocusRequester)
                        .onPreviewKeyEvent {
                            if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                                firstFocusRequester.requestFocus()
                                true
                            } else false
                        },
                    onClick = onSave,
                    text = if (isNew) "Dodaj" else "Ažuriraj",
                    buttonBackgroundColor = Color.Green,
                )
            }
        }
    }
}

/**
 * Holds a [TextFieldValue] whose text resets to [value] whenever [key]
 * changes (e.g. the form's entity is replaced). Caret is placed at the
 * end of the new text.
 *
 * Replaces the common pattern of pairing `remember(key) { mutableStateOf(...) }`
 * with a `LaunchedEffect(key) { ... }` re-init.
 */
@Composable
fun rememberSyncedTextField(key: Any?, value: String): MutableState<TextFieldValue> {
    val state = remember(key) {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }
    LaunchedEffect(key) {
        state.value = TextFieldValue(value, selection = TextRange(value.length))
    }
    return state
}
