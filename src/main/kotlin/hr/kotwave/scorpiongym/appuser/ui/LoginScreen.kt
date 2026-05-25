package hr.kotwave.scorpiongym.appuser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import hr.kotwave.scorpiongym.appuser.AppUserViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import org.koin.java.KoinJavaComponent.getKoin

class LoginScreen(private val onLoginSuccess: () -> Unit) : Screen {

    @Composable
    override fun Content() {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf(false) }

        val appUserViewModel: AppUserViewModel = getKoin().get()

        val usernameFocusRequester = remember { FocusRequester() }
        val passwordFocusRequester = remember { FocusRequester() }
        val loginButtonFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            usernameFocusRequester.requestFocus()
        }

        fun performLogin() {
            try {
                appUserViewModel.loginAppUser(username, password)
                onLoginSuccess()
            } catch (_: Exception) {
                loginError = true
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyDown) {
                            performLogin()
                            true
                        } else {
                            false
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Korisničko ime") },
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .fillMaxWidth(0.5f)
                        .focusRequester(usernameFocusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                passwordFocusRequester.requestFocus()
                                true
                            } else false
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Lozinka") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val iconText = if (showPassword) "🙈" else "👁️"
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Text(iconText)
                        }
                    },
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .fillMaxWidth(0.5f)
                        .focusRequester(passwordFocusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                loginButtonFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (loginError) {
                    InformativeDialog("Pogrešno korisničko ime ili lozinka") { loginError = false }
                }

                Button(
                    onClick = { performLogin() },
                    modifier = Modifier
                        .width(100.dp)
                        .focusRequester(loginButtonFocusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                usernameFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                ) {
                    Text("Login")
                }
            }

            Text(
                text = "Developed by Leo Žarković",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }
}
