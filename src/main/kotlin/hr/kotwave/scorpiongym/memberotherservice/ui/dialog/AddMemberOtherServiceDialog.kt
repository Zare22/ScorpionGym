package hr.kotwave.scorpiongym.memberotherservice.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
import hr.kotwave.scorpiongym.memberotherservice.MemberOtherService
import hr.kotwave.scorpiongym.otherservice.OtherServiceViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.Dropdown
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AddMemberOtherServiceDialog(member: Member, onClose: () -> Unit) {

    val memberDetailsViewModel: MemberDetailsViewModel = rememberMemberDetailsViewModel(member)
    val otherServiceViewModel: OtherServiceViewModel = getKoin().get()

    var selectedServiceId by remember { mutableStateOf("") }
    var servicesExpanded by remember { mutableStateOf(false) }

    var isPaid by remember { mutableStateOf(false) }

    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    when {
        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }
    }

    Dialog(onDismissRequest = { onClose() }) {

        Card(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Dropdown(
                    expanded = servicesExpanded,
                    onExpandedChange = { servicesExpanded = it },
                    label = "Ostale usluge",
                    items = otherServiceViewModel.otherServices,
                    selectedItem = otherServiceViewModel.otherServices.find { it.id.toString() == selectedServiceId },
                    onItemSelected = { selectedServiceId = it.id.toString() },
                    focusRequester = FocusRequester(),
                    nextFocusRequester = FocusRequester(),
                    canSwitchWithTab = false,
                    itemLabel = { it.name }
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = buildAnnotatedString {
                        append("Upisat ćete odabranu uslugu za člana ")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("${member.surname} ${member.name}")
                        }

                        append(" na datum: ")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")))
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (selectedServiceId.isNotEmpty()) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = buildAnnotatedString {
                            append("Cijena usluge je: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("${otherServiceViewModel.otherServices.find { otherService -> otherService.id == selectedServiceId.toIntOrNull() }?.price} €")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            modifier = Modifier.wrapContentWidth(),
                            checked = isPaid,
                            onCheckedChange = { isPaid = !isPaid }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Plaćeno")
                    }
                }
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
                            selectedServiceId.toIntOrNull()?.let {
                                val memberOtherService = MemberOtherService(
                                    dateOfService = LocalDateTime.now(),
                                    memberId = memberDetailsViewModel.currentMember.id,
                                    isPaid = isPaid,
                                    otherServiceId = selectedServiceId.toInt()
                                )
                                try {
                                    memberDetailsViewModel.addNewMemberOtherService(memberOtherService)
                                } catch (e: Exception) {
                                    infoMessage = "Greška pri dodavanju ostale usluge"
                                    showInfoDialog = true
                                }
                            }
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