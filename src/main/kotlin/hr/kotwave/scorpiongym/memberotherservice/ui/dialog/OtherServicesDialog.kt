package hr.kotwave.scorpiongym.memberotherservice.ui.dialog

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import hr.kotwave.scorpiongym.di.rememberMemberViewModel
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.memberotherservice.MemberOtherService
import hr.kotwave.scorpiongym.otherservice.OtherServiceViewModel
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableCheckbox
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun OtherServicesDialog(member: Member, onClose: () -> Unit) {
    val memberViewModel: MemberViewModel = rememberMemberViewModel(member)
    val otherServiceViewModel: OtherServiceViewModel = getKoin().get()
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'u' HH:mm")
    val listState = rememberLazyListState()
    val memberOtherServices by remember { derivedStateOf { memberViewModel.memberOtherServices.sortedBy { it.dateOfService } } }
    val initialIsPaidValues = remember { memberOtherServices.map { it.isPaid }.toMutableStateList() }

    var selectedMemberOtherServiceToDelete by remember { mutableStateOf<MemberOtherService?>(null) }
    var nameOfOtherService by remember { mutableStateOf("") }
    var confirmOtherServiceDeleteDialog by remember { mutableStateOf(false) }


    when {
        confirmOtherServiceDeleteDialog -> {
            AlertDialog(
                onDismissRequest = { confirmOtherServiceDeleteDialog = false },
                title = { Text("Brisanje usluge", color = Color.Red) },
                text = {
                    Text(
                        text = buildAnnotatedString {
                            append("Ukoliko nastavite pobrisat ćete uslugu: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(nameOfOtherService)
                            }

                            append(" na datum: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(selectedMemberOtherServiceToDelete?.dateOfService?.format(dateFormatter))
                            }
                        }
                    )
                },
                confirmButton = {
                    HoverableButton(
                        text = "Potvrdi",
                        buttonBackgroundColor = Color.Red,
                        onClick = {
                            selectedMemberOtherServiceToDelete?.let { memberViewModel.removeMemberOtherService(it) }
                            confirmOtherServiceDeleteDialog = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { confirmOtherServiceDeleteDialog = false }
                    )
                }
            )
        }
    }


    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onClose() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .fillMaxWidth(0.8f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tip usluge",
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Datum usluge",
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Plaćeno",
                        modifier = Modifier.weight(0.5f).padding(end = 8.dp),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Obriši",
                        modifier = Modifier.weight(0.5f).padding(end = 4.dp),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                }
                Divider(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(memberOtherServices) { index, memberOtherService ->
                        val otherServiceType =
                            otherServiceViewModel.otherServices.find { otherService -> otherService.id == memberOtherService.otherServiceId }
                        val otherServiceTypeName = otherServiceType?.name?.let { TextFieldValue(it) }
                        var memberOtherServiceDate by remember {
                            mutableStateOf(
                                TextFieldValue(
                                    memberOtherService.dateOfService.format(
                                        dateFormatter
                                    )
                                )
                            )
                        }

                        var isPaid by remember { mutableStateOf(memberOtherService.isPaid) }
                        val price = otherServiceType?.price

                        //Leave if we want to introduce discount on other services
//                        memberViewModel.currentMember.organizationId?.let { organizationId ->
//                            if (organizationId != 0) {
//                                val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()
//                                val typeOfOrganization =
//                                    typeOfOrganizationViewModel.organizationTypes.find { typeOfOrg ->
//                                        typeOfOrg.id == organizationId
//                                    }
//                                if (typeOfOrganization != null) {
//                                    val discountRate = typeOfOrganization.discountRate / 100.0
//                                    price?.let { originalPrice ->
//                                        price = originalPrice * (1 - discountRate)
//                                    }
//                                }
//                            }
//                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FocusableOutlinedTextField(
                                value = otherServiceTypeName ?: TextFieldValue(""),
                                onValueChange = {},
                                label = "",
                                currentFocusRequester = FocusRequester(),
                                nextFocusRequester = FocusRequester(),
                                canSwitchWithTab = false,
                                readOnly = true,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            FocusableOutlinedTextField(
                                value = memberOtherServiceDate,
                                onValueChange = { newValue ->
                                    memberOtherServiceDate = newValue
                                    val parsedDateTime =
                                        runCatching { LocalDateTime.parse(newValue.text, dateFormatter) }.getOrNull()
                                    if (parsedDateTime != null) {
                                        memberViewModel.updateMemberOtherService(
                                            index,
                                            memberOtherService.copy(dateOfService = parsedDateTime)
                                        )
                                    }
                                },
                                label = "",
                                currentFocusRequester = FocusRequester(),
                                nextFocusRequester = FocusRequester(),
                                canSwitchWithTab = false,
                                readOnly = false,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            HoverableCheckbox(
                                modifier = Modifier.weight(0.5f).padding(end = 8.dp),
                                checked = isPaid,
                                onCheckedChange = {
                                    isPaid = !isPaid
                                    memberViewModel.updateMemberOtherService(
                                        index,
                                        memberOtherService.copy(
                                            isPaid = isPaid
                                        )
                                    )
                                },
                                hoverText = "$price €",
                                showPopupOnHover = !isPaid
                            )
                            IconButton(
                                onClick = {
                                    selectedMemberOtherServiceToDelete = memberOtherService
                                    nameOfOtherService = otherServiceTypeName?.text ?: ""
                                    confirmOtherServiceDeleteDialog = true
                                },
                                modifier = Modifier.weight(0.5f).padding(end = 4.dp)
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
                            memberOtherServices.forEachIndexed { index, record ->
                                memberViewModel.updateMemberOtherService(
                                    index,
                                    record.copy(
                                        isPaid = initialIsPaidValues[index]
                                    )
                                )
                            }
                            onClose()
                        }
                    )
                    HoverableButton(
                        text = "Potvrdi promjene",
                        onClick = {
                            memberViewModel.confirmMemberOtherServicesUpdates()
                            onClose()
                        },
                        buttonBackgroundColor = Color.Green
                    )
                }
            }
        }
    }
}