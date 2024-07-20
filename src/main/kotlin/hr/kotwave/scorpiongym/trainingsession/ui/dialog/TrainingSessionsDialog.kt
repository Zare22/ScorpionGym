package hr.kotwave.scorpiongym.trainingsession.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import hr.kotwave.scorpiongym.di.rememberMemberViewModel
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.trainingsession.TrainingSession
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun TrainingSessionsDialog(member: Member, onClose: () -> Unit) {
    val memberViewModel: MemberViewModel = rememberMemberViewModel(member)
    val membershipViewModel: MembershipViewModel = getKoin().get()
    val membership = membershipViewModel.memberships.find { membership ->
        membership.id == (memberViewModel.activeMembershipRecord?.membershipId) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'u' HH:mm")
    val listState = rememberLazyListState()
    val trainingSessions by remember { derivedStateOf { memberViewModel.trainingSessionsInActiveMembership.sortedBy { it.sessionDateTime } } }
    var updateTrigger by remember { mutableStateOf(false) }
    var expiredMembershipDialogOpened by remember { mutableStateOf(false) }

    var selectedSessionToDelete by remember { mutableStateOf<TrainingSession?>(null) }
    var confirmSessionDeleteDialog by remember { mutableStateOf(false) }

    when {
        confirmSessionDeleteDialog -> {
            AlertDialog(
                onDismissRequest = { confirmSessionDeleteDialog = false },
                title = { Text("Brisanje trening", color = Color.Red) },
                text = {
                    Text(
                        text = buildAnnotatedString {
                            append("Ukoliko nastavite pobrisat ćete trening na datum: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(selectedSessionToDelete?.sessionDateTime?.format(dateFormatter))
                            }
                        }
                    )
                },
                confirmButton = {
                    HoverableButton(
                        text = "Potvrdi",
                        buttonBackgroundColor = Color.Red,
                        onClick = {
                            selectedSessionToDelete?.let {
                                memberViewModel.removeTrainingSession(it)
                                memberViewModel.initViewModel()
                            }
                            confirmSessionDeleteDialog = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { confirmSessionDeleteDialog = false }
                    )
                }
            )
        }
    }

    Dialog(
        onDismissRequest = {
            memberViewModel.removeTrainingSessionsWithoutId()
            onClose()
        }) {
        Card(modifier = Modifier.fillMaxHeight(0.8f)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = buildAnnotatedString {
                            append("Naziv članarine: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(membership?.name)
                            }
                        }
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = buildAnnotatedString {
                            append("Broj treninga u članarini: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(membership?.numberOfTrainingsAvailable.toString())
                            }
                        }
                    )
                }
                Divider(modifier = Modifier.height(2.dp))
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    itemsIndexed(trainingSessions) { index, session ->
                        var sessionDateTime by remember {
                            mutableStateOf(
                                TextFieldValue(
                                    session.sessionDateTime.format(
                                        dateFormatter
                                    )
                                )
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            FocusableOutlinedTextField(
                                modifier = Modifier.weight(0.8f),
                                value = sessionDateTime,
                                onValueChange = { newValue ->
                                    sessionDateTime = newValue
                                    val parsedDateTime =
                                        runCatching { LocalDateTime.parse(newValue.text, dateFormatter) }.getOrNull()
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
                                canSwitchWithTab = false
                            )
                            session.id.takeIf { it != 0 }?.let {
                                IconButton(
                                    onClick = {
                                        selectedSessionToDelete = session
                                        confirmSessionDeleteDialog = true
                                    },
                                    modifier = Modifier.weight(0.2f).padding(end = 4.dp)
                                        .align(Alignment.CenterVertically)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Obriši",
                                        tint = Color.Red
                                    )
                                }
                            }
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
                                HoverableButton(
                                    text = "+ Dodaj novi trening",
                                    onClick = {
                                        val newSession = TrainingSession(
                                            sessionDateTime = LocalDateTime.now(),
                                            membershipRecordId = memberViewModel.activeMembershipRecord!!.id
                                        )
                                        memberViewModel.addTrainingSession(newSession)
                                        updateTrigger = !updateTrigger
                                    }
                                )
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
                    HoverableButton(
                        text = "Povratak",
                        onClick = {
                            memberViewModel.removeTrainingSessionsWithoutId()
                            onClose()
                        }
                    )
                    HoverableButton(
                        text = "Potvrdi promjene",
                        onClick = {
                            memberViewModel.confirmTrainingSessionUpdates()
                            if (memberViewModel.trainingSessionsInActiveMembership.size >= memberViewModel.numberOfTrainingsAvailable) {
                                memberViewModel.initViewModel()
                                expiredMembershipDialogOpened = true
                            } else
                                onClose()
                        },
                        buttonBackgroundColor = Color.Green
                    )
                }
            }
        }
    }
    if (expiredMembershipDialogOpened) {
        InformativeDialog(
            message = "Članu ${memberViewModel.currentMember.name} ${memberViewModel.currentMember.surname} je istekla trenutna članarina",
            onDismiss = {
                expiredMembershipDialogOpened = false
                onClose()
            }
        )
    }
}
