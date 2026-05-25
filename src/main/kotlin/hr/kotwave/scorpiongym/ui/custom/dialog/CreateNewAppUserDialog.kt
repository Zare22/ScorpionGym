package hr.kotwave.scorpiongym.ui.custom.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import hr.kotwave.scorpiongym.appuser.AppUserViewModel
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun CreateNewAppUserDialog(onDismiss: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val registerButtonFocusRequester = remember { FocusRequester() }

    val appUserViewModel: AppUserViewModel = getKoin().get()

    LaunchedEffect(Unit) {
        usernameFocusRequester.requestFocus()
    }

    fun performRegistration() {
        try {
            appUserViewModel.registerAppUser(username, password, isAdmin)
            infoMessage = "Korisnik je uspješno registriran"
            showInfoDialog = true
        } catch (_: Exception) {
            showInfoDialog = true
            infoMessage = "Greška pri registraciji novog korisnika"
        }
    }

    when {
        showInfoDialog -> {
            InformativeDialog(infoMessage) { onDismiss() }
        }
    }

    Dialog(properties = DialogProperties(usePlatformDefaultWidth = false), onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier.fillMaxHeight(0.8f).fillMaxWidth(0.8f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp).onPreviewKeyEvent { event ->
                    if (event.key == Key.Enter && event.type == KeyEventType.KeyDown) {
                        performRegistration()
                        true
                    } else {
                        false
                    }
                }, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TextField(value = username,
                    onValueChange = { username = it },
                    label = { Text("Korisničko ime") },
                    modifier = Modifier.width(IntrinsicSize.Max).fillMaxWidth(0.5f)
                        .focusRequester(usernameFocusRequester).onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                passwordFocusRequester.requestFocus()
                                true
                            } else false
                        })

                Spacer(modifier = Modifier.height(16.dp))

                TextField(value = password,
                    onValueChange = { password = it },
                    label = { Text("Lozinka") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val iconText = if (showPassword) "🙈" else "👁️"
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Text(iconText)
                        }
                    },
                    modifier = Modifier.width(IntrinsicSize.Max).fillMaxWidth(0.5f)
                        .focusRequester(passwordFocusRequester).onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                registerButtonFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        })

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(0.5f).padding(bottom = 16.dp)
                ) {
                    Checkbox(
                        checked = isAdmin,
                        onCheckedChange = { isAdmin = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Administrator")
                }

                Button(onClick = { performRegistration() },
                    modifier = Modifier.width(100.dp).focusRequester(registerButtonFocusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                usernameFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }) {
                    Text("Registriraj")
                }
            }
        }
    }
}