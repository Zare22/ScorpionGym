package hr.kotwave.scorpiongym.appuser.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import hr.kotwave.scorpiongym.appuser.AppUserViewModel
import hr.kotwave.scorpiongym.ui.custom.elements.Dropdown
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun UserActionsDialog(logs: List<Triple<String, String, String>>, onClose: () -> Unit) {
    var filterDate by remember { mutableStateOf("") }
    var selectedUsername by remember { mutableStateOf<String?>(null) }
    var usernameDropdownExpanded by remember { mutableStateOf(false) }

    val appUserViewModel: AppUserViewModel = getKoin().get()
    val usernames = appUserViewModel.allUsers.map { it.username }

    val lazyListState = rememberLazyListState(0)

    val filteredLogs by remember(filterDate, selectedUsername, logs) {
        derivedStateOf {
            val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val parsedDate = runCatching { LocalDate.parse(filterDate, dateFormatter) }.getOrNull()

            logs.filter { log ->
                val logDateTime = runCatching {
                    LocalDateTime.parse(log.second, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                }.getOrNull()

                val matchesDate = parsedDate?.let { logDateTime?.toLocalDate() == it } ?: true
                val matchesUsername = selectedUsername?.let { log.third == it } ?: true

                matchesDate && matchesUsername
            }
        }
    }

    Dialog(onDismissRequest = { onClose() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.6f).padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Aktivnosti radnika", style = MaterialTheme.typography.h6)
                Divider(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {

                    OutlinedTextField(
                        value = filterDate,
                        onValueChange = { newValue ->
                            filterDate = newValue
                        },
                        label = { Text("Unesite datum (DD.MM.YYYY)") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp, top = 8.dp),
                        singleLine = true
                    )

                    Dropdown(
                        expanded = usernameDropdownExpanded,
                        onExpandedChange = { usernameDropdownExpanded = it },
                        label = "Odaberite korisnika",
                        items = usernames,
                        selectedItem = selectedUsername,
                        onItemSelected = { selectedUsername = it },
                        focusRequester = FocusRequester(),
                        nextFocusRequester = FocusRequester(),
                        itemLabel = { it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        readOnly = false
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    Row {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            items(filteredLogs) { log ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(
                                        text = buildAnnotatedString {
                                            append(log.first)
                                            append(" (")
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append(log.third)
                                            }
                                            append(")")
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(log.second, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(lazyListState),
                    )
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
