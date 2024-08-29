package hr.kotwave.scorpiongym.appuser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun UserActionsDialog(logs: List<Pair<String, String>>, onClose: () -> Unit) {
    Dialog(onDismissRequest = { onClose() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.6f).padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Aktivnosti radnika", style = MaterialTheme.typography.h6)

                LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp)) {
                    items(logs) { log ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(log.first, modifier = Modifier.weight(1f))
                            Text(log.second, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onClose() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
