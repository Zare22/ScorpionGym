package hr.kotwave.scorpiongym.ui.custom.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.util.PreferencesHelper

@Composable
fun CustomMenu(
    onThemeChange: () -> Unit,
    onBackup: () -> Unit,
    onAddUnregisteredService: () -> Unit,
    onLogout: () -> Unit,
    onAllLogsSelected: () -> Unit,
    onCreateNewAppUser: () -> Unit,
    onOpenUnregisteredServiceDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var unregisteredServiceMenuExpanded by remember { mutableStateOf(false) }

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
                if (PreferencesHelper().isAdmin) {
                    DropdownMenuItem(onClick = {
                        onCreateNewAppUser()
                        settingsMenuExpanded = false
                    }) {
                        Text("Kreiraj novog korisnika", style = MaterialTheme.typography.body2)
                    }
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
        Box {
            Text(
                text = "Neregistrirane usluge",
                modifier = Modifier
                    .clickable { unregisteredServiceMenuExpanded = !unregisteredServiceMenuExpanded }
                    .padding(8.dp),
                style = MaterialTheme.typography.body1
            )
            DropdownMenu(
                expanded = unregisteredServiceMenuExpanded,
                onDismissRequest = { unregisteredServiceMenuExpanded = false }
            ) {
                DropdownMenuItem(onClick = {
                    onAddUnregisteredService()
                    unregisteredServiceMenuExpanded = false
                }) {
                    Text("Upiši uslugu za neregistriranog člana", style = MaterialTheme.typography.body2)
                }
                DropdownMenuItem(onClick = {
                    onOpenUnregisteredServiceDialog()
                    unregisteredServiceMenuExpanded = false
                }) {
                    Text("Povijest neregistriranih usluga", style = MaterialTheme.typography.body2)
                }
            }
        }
        Spacer(Modifier.width(8.dp))

        if (PreferencesHelper().isAdmin) {
            Text(
                text = "Povijest aktivnosti",
                modifier = Modifier
                    .clickable { onAllLogsSelected() }
                    .padding(8.dp),
                style = MaterialTheme.typography.body1
            )
//            Text(
//                text = "Kasa",
//                modifier = Modifier
//                    .clickable {
//                        getKoin().get<PaymentAuditLogViewModel>().initPaymentAuditLogs()
//                        onCashRegisterSelected()
//                    }
//                    .padding(8.dp),
//                style = MaterialTheme.typography.body1
//            )
        }

    }
    Divider(thickness = 0.5.dp)
}
