package hr.kotwave.scorpiongym.trainingsession.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
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
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AddSingleTrainingSessionDialog(member: Member, onClose: () -> Unit) {
    val memberDetailsViewModel: MemberDetailsViewModel = rememberMemberDetailsViewModel(member)
    var expiredMembershipDialogOpened by remember { mutableStateOf(false) }

    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    when {
        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }
    }

    Dialog(onDismissRequest = { onClose() }) {

        Card(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = buildAnnotatedString {
                            append("Upisat ćete trening za člana ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("${memberDetailsViewModel.currentMember.surname} ${memberDetailsViewModel.currentMember.name}")
                            }

                            append(" na datum: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HoverableButton(
                            onClick = { onClose() }, text = "Povratak"
                        )
                        HoverableButton(
                            onClick = {
                                try {
                                    memberDetailsViewModel.addNewTrainingSessionToMember()
                                } catch (_: Exception) {
                                    infoMessage = "Greška pri dodavanju treninga"
                                    showInfoDialog = true
                                }
                                if (memberDetailsViewModel.trainingSessionsInActiveMembership.size >= memberDetailsViewModel.numberOfTrainingsAvailable) {
                                    memberDetailsViewModel.initViewModel()
                                    expiredMembershipDialogOpened = true
                                } else
                                    onClose()
                            },
                            text = "Potvrdi",
                            buttonBackgroundColor = Color.Green
                        )
                    }
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