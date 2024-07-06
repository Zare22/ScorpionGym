package hr.kotwave.scorpiongym.ui.custom.dialog

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton

@Composable
fun InformativeDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Obavijest") },
        text = { Text(text = message) },
        confirmButton = {
            HoverableButton(
                text = "Potvrdi",
                onClick = { onDismiss() }
            )
        }
    )
}
