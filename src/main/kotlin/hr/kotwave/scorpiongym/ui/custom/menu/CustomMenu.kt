package hr.kotwave.scorpiongym.ui.custom.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.appuser.AppUser
import hr.kotwave.scorpiongym.util.PreferencesHelper

@Composable
fun CustomMenu(
    onThemeChange: () -> Unit,
    onBackup: () -> Unit,
    onAddUnregisteredService: () -> Unit,
    onLogout: () -> Unit,
    users: List<AppUser>,
    onUserSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var usersMenuExpanded by remember { mutableStateOf(false) }

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
                DropdownMenuItem(onClick = {
                    onLogout()
                    settingsMenuExpanded = false
                }) {
                    Text("Odlogiraj se", style = MaterialTheme.typography.body2)
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
        Spacer(Modifier.width(8.dp))

        if (PreferencesHelper().isAdmin) {
            Box {
                Text(
                    text = "Radnici",
                    modifier = Modifier
                        .clickable { usersMenuExpanded = !usersMenuExpanded }
                        .padding(8.dp),
                    style = MaterialTheme.typography.body1
                )
                DropdownMenu(
                    expanded = usersMenuExpanded,
                    onDismissRequest = { usersMenuExpanded = false }
                ) {
                    users.forEach { user ->
                        DropdownMenuItem(onClick = {
                            onUserSelected(user.id)
                            usersMenuExpanded = false
                        }) {
                            Text(user.username, style = MaterialTheme.typography.body2)
                        }
                    }
                }
            }
        }

    }
    Divider(thickness = 0.5.dp)
}
