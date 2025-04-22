package hr.kotwave.scorpiongym.appuser.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import hr.kotwave.scorpiongym.appuser.AppUser
import hr.kotwave.scorpiongym.appuser.AppUserViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun AppUsersList(onClose: () -> Unit) {

    val appUserViewModel: AppUserViewModel = getKoin().get()
    val lazyListState = rememberLazyListState(0)
    var confirmAppUserDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var appUserToDelete by remember { mutableStateOf<AppUser?>(null) }
    var infoMessage by remember { mutableStateOf("") }

    when {
        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }

        confirmAppUserDeleteDialog -> {
            AlertDialog(
                onDismissRequest = { confirmAppUserDeleteDialog = false },
                title = { Text("Brisanje članarine", color = Color.Red) },
                text = {
                    Text(
                        text = "Ukoliko nastavite pobrisat ćete odabranog korisnika i SVE vezane naplate istog!"
                    )
                },
                confirmButton = {
                    HoverableButton(
                        text = "Potvrdi",
                        buttonBackgroundColor = Color.Red,
                        onClick = {
                            if (appUserToDelete != null) {
                                try {
                                    appUserViewModel.deleteAppUser(appUserToDelete!!)
                                    appUserToDelete = null
                                } catch (e: Exception) {
                                    infoMessage = e.message.toString()
                                    showInfoDialog = true
                                }
                            }
                            confirmAppUserDeleteDialog = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = {
                            appUserToDelete = null
                            confirmAppUserDeleteDialog = false
                        }
                    )
                }
            )
        }
    }

    Dialog(onDismissRequest = { onClose() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight(0.5f).padding(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.padding(12.dp),
                    state = lazyListState
                ) {
                    items(appUserViewModel.allUsers) { appUser ->
                        AppUserItem(
                            appUser = appUser,
                            onDelete = {
                                appUserToDelete = appUser
                                confirmAppUserDeleteDialog = true
                            }
                        )
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(lazyListState),
                )
            }
        }
    }
}

@Composable
fun AppUserItem(appUser: AppUser, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = appUser.username,
                style = MaterialTheme.typography.body1
            )

            Icon(
                imageVector = Icons.Default.Delete,
                tint = Color.Red,
                contentDescription = "Delete User",
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, false).clickable { onDelete() }
            )
        }
    }
}
