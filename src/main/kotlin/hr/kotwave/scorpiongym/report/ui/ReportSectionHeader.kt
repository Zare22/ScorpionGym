package hr.kotwave.scorpiongym.report.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton

/**
 * Section title with an "Ispiši" (export/print) button on the right. The button
 * appears only when [onPrint] is non-null (i.e. there is loaded data to print).
 */
@Composable
fun ReportSectionHeader(title: String, onPrint: (() -> Unit)?) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.h6)
        if (onPrint != null) {
            HoverableButton(text = "Ispiši", onClick = onPrint, padding = 4.dp)
        }
    }
}
