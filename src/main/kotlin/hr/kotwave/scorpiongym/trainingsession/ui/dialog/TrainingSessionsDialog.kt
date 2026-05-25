package hr.kotwave.scorpiongym.trainingsession.ui.dialog

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import hr.kotwave.scorpiongym.di.rememberMemberDetailsViewModel
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberDetailsViewModel
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.trainingsession.TrainingSession
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.DateTimePickerField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun TrainingSessionsDialog(member: Member, onClose: () -> Unit, membershipRecordId: Int = 0) {
    val memberDetailsViewModel: MemberDetailsViewModel = rememberMemberDetailsViewModel(member)
    if (membershipRecordId > 0) {
        memberDetailsViewModel.memberRecords.first { it.id == membershipRecordId }.let {
            memberDetailsViewModel.assignActiveMembershipRecord(it)
        }
    }
    val membershipViewModel: MembershipViewModel = getKoin().get()
    val membership = membershipViewModel.memberships.find { membership ->
        membership.id == (memberDetailsViewModel.activeMembershipRecord?.membershipId)
    }
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'u' HH:mm")
    val listState = rememberLazyListState()
    var updateTrigger by remember { mutableStateOf(false) }
    var expiredMembershipDialogOpened by remember { mutableStateOf(false) }

    var selectedSessionToDelete by remember { mutableStateOf<TrainingSession?>(null) }
    var confirmSessionDeleteDialog by remember { mutableStateOf(false) }

    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    val trainingSessions by remember { derivedStateOf { memberDetailsViewModel.trainingSessionsInActiveMembership } }

    when {

        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }

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
                                memberDetailsViewModel.deleteTrainingSession(it)
                                memberDetailsViewModel.initViewModel()
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
            memberDetailsViewModel.removeTrainingSessionsWithoutId()
            onClose()
        }
    ) {
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
                            membership?.takeIf { it.isNoLimit }?.run {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("No limit")
                                }
                            } ?: run {
                                append("Broj treninga u članarini: ")

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(membership?.numberOfTrainingsAvailable.toString())
                                }
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = buildAnnotatedString {
                            append("Istek članarine: ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(
                                    memberDetailsViewModel.activeMembershipRecord?.dateFinished?.format(
                                        DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                    )
                                )
                            }
                        }
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = buildAnnotatedString {
                            membership?.takeIf { it.isNoLimit }?.run {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Odrađeni treninzi u članarini: ")

                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("${memberDetailsViewModel.trainingSessionsInActiveMembership.size}")
                                    }
                                }
                            } ?: run {
                                append("Preostalo treninga u članarini: ")

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    val remainingTrainings =
                                        memberDetailsViewModel.numberOfTrainingsAvailable - memberDetailsViewModel.trainingSessionsInActiveMembership.size
                                    append("$remainingTrainings")
                                }
                            }
                        }
                    )
                }

                Divider(modifier = Modifier.height(2.dp))

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(start = 6.dp, end = 6.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        itemsIndexed(trainingSessions) { index, session ->
                            var sessionDateTime by remember { mutableStateOf(session.sessionDateTime) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                DateTimePickerField(
                                    modifier = Modifier.weight(0.8f),
                                    value = sessionDateTime,
                                    onValueChange = { newValue ->
                                        if (newValue != null && newValue != session.sessionDateTime) {
                                            sessionDateTime = newValue
                                            memberDetailsViewModel.updateTrainingSession(
                                                index,
                                                session.copy(sessionDateTime = newValue)
                                            )
                                        }
                                    },
                                    label = "Datum treninga",
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
                        if (memberDetailsViewModel.trainingSessionsInActiveMembership.size < memberDetailsViewModel.numberOfTrainingsAvailable) {
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
                                                membershipRecordId = memberDetailsViewModel.activeMembershipRecord!!.id
                                            )
                                            memberDetailsViewModel.addTrainingSession(newSession)
                                            updateTrigger = !updateTrigger
                                        }
                                    )
                                }
                            }
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(listState),
                    )
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
                            memberDetailsViewModel.removeTrainingSessionsWithoutId()
                            memberDetailsViewModel.initViewModel()
                            onClose()
                        }
                    )
                    HoverableButton(
                        text = "Potvrdi promjene",
                        onClick = {
                            try {
                                memberDetailsViewModel.confirmTrainingSessionUpdates()
                            } catch (_: Exception) {
                                infoMessage = "Greška pri dodavanju treninga"
                                showInfoDialog = true
                            }
                            if (memberDetailsViewModel.trainingSessionsInActiveMembership.size >= memberDetailsViewModel.numberOfTrainingsAvailable) {
                                memberDetailsViewModel.initViewModel()
                                expiredMembershipDialogOpened = true
                            } else {
                                memberDetailsViewModel.initViewModel()
                                onClose()
                            }
                        },
                        buttonBackgroundColor = Color.Green
                    )
                }
            }
        }
    }
    if (expiredMembershipDialogOpened) {
        InformativeDialog(
            message = "Članu ${memberDetailsViewModel.currentMember.name} ${memberDetailsViewModel.currentMember.surname} je istekla trenutna članarina",
            onDismiss = {
                expiredMembershipDialogOpened = false
                onClose()
            }
        )
    }
}
