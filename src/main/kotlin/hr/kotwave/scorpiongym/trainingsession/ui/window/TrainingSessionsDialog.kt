package hr.kotwave.scorpiongym.trainingsession.ui.window

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.trainingsession.TrainingSession
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun TrainingSessionsDialog(member: Member, onClose: () -> Unit) {
    val memberViewModel: MemberViewModel = getKoin().get { parametersOf(member) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'u' HH:mm")
    val listState = rememberLazyListState()
    val trainingSessions by remember { derivedStateOf { memberViewModel.trainingSessionsInActiveMembership.sortedBy { it.sessionDateTime } } }
    var updateTrigger by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onClose() }) {
        Card(modifier = Modifier.fillMaxHeight(0.8f)) {
            Column {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    itemsIndexed(trainingSessions) { index, session ->
                        var sessionDateTime by remember { mutableStateOf(TextFieldValue(session.sessionDateTime.format(dateFormatter))) }
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)) {
                            FocusableOutlinedTextField(
                                value = sessionDateTime,
                                onValueChange = { newValue ->
                                    sessionDateTime = newValue
                                    val parsedDateTime = runCatching { LocalDateTime.parse(newValue.text, dateFormatter) }.getOrNull()
                                    if (parsedDateTime != null) {
                                        memberViewModel.updateTrainingSession(
                                            index,
                                            session.copy(sessionDateTime = parsedDateTime)
                                        )
                                    }
                                },
                                label = "Datum treninga",
                                currentFocusRequester = FocusRequester(),
                                nextFocusRequester = FocusRequester(),
                            )
                        }
                    }
                    if (memberViewModel.trainingSessionsInActiveMembership.size < memberViewModel.numberOfTrainingsAvailable) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(onClick = {
                                    val newSession = TrainingSession(
                                        sessionDateTime = LocalDateTime.now(),
                                        membershipRecordId = memberViewModel.activeMembershipRecord!!.id
                                    )
                                    memberViewModel.addTrainingSession(newSession)
                                    updateTrigger = !updateTrigger
                                }) {
                                    Text("+ Dodaj novi trening")
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(updateTrigger) {
                    if (trainingSessions.isNotEmpty()) {
                        listState.animateScrollToItem(trainingSessions.size - 1)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { memberViewModel.confirmTrainingSessionUpdates() }) {
                        Text("Potvrdi promjene")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onClose() }) {
                        Text("Povratak")
                    }
                }
            }
        }
    }
}
