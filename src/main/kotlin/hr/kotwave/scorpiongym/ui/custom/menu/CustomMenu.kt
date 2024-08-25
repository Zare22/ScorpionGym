package hr.kotwave.scorpiongym.ui.custom.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomMenu(
    onThemeChange: () -> Unit,
    onBackup: () -> Unit,
    onAddUnregisteredService: () -> Unit,
    modifier: Modifier = Modifier
) {
    var settingsMenuExpanded by remember { mutableStateOf(false) }

    Row(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Box {
            Text(
                text = "Postavke",
                modifier = Modifier
                    .clickable { settingsMenuExpanded = !settingsMenuExpanded }
                    .padding(8.dp),
                style = MaterialTheme.typography.body1
            )
            DropdownMenu(
                expanded = settingsMenuExpanded,
                onDismissRequest = { settingsMenuExpanded = false }
            ) {
                DropdownMenuItem(onClick = {
                    onThemeChange()
                    settingsMenuExpanded = false
                }) {
                    Text("Promijeni temu", style = MaterialTheme.typography.body2)
                }
                DropdownMenuItem(onClick = {
                    onBackup()
                    settingsMenuExpanded = false
                }) {
                    Text("Napravi backup", style = MaterialTheme.typography.body2)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Upiši uslugu za neregistriranog člana",
            modifier = Modifier
                .clickable { onAddUnregisteredService() }
                .padding(8.dp),
            style = MaterialTheme.typography.body1
        )
    }
    Divider(thickness = 0.5.dp)
}
