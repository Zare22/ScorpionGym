package hr.kotwave.scorpiongym.membershiprecord.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import hr.kotwave.scorpiongym.di.rememberMemberViewModel
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableCheckbox
import org.koin.java.KoinJavaComponent.getKoin
import java.time.format.DateTimeFormatter

@Composable
fun UserMembershipRecordsDialog(member: Member, onClose: () -> Unit) {
    val memberViewModel: MemberViewModel = rememberMemberViewModel(member)
    val membershipViewModel: MembershipViewModel = getKoin().get()
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'u' HH:mm")
    val listState = rememberLazyListState()
    val records by remember { derivedStateOf { memberViewModel.memberRecords.sortedBy { it.dateStarted } } }

    val initialIsPaidValues = remember { records.map { it.isPaid }.toMutableStateList() }


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
                        text = "Tip članarine",
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Početak članarine",
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Kraj članarine",
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
                        text = "Aktivno",
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
                    itemsIndexed(records) { index, record ->
                        val membershipType =
                            membershipViewModel.memberships.find { membership -> membership.id == record.membershipId }

                        val membershipTypeName = membershipType?.name?.let { TextFieldValue(it) }
                        val recordDateStart = TextFieldValue(record.dateStarted.format(dateFormatter))
                        val recordDateFinished = TextFieldValue(record.dateFinished.format(dateFormatter))

                        var isPaid by remember { mutableStateOf(record.isPaid) }
                        var price = membershipType?.price

                        memberViewModel.currentMember.organizationId?.let { organizationId ->
                            if (organizationId != 0) {
                                val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()
                                val typeOfOrganization = typeOfOrganizationViewModel.organizationTypes.find { typeOfOrg ->
                                    typeOfOrg.id == organizationId
                                }
                                if (typeOfOrganization != null) {
                                    val discountRate = typeOfOrganization.discountRate / 100.0
                                    price?.let { originalPrice ->
                                        price = originalPrice * (1 - discountRate)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FocusableOutlinedTextField(
                                value = membershipTypeName ?: TextFieldValue(""),
                                onValueChange = {},
                                label = "",
                                currentFocusRequester = FocusRequester(),
                                nextFocusRequester = FocusRequester(),
                                readOnly = true,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            FocusableOutlinedTextField(
                                value = recordDateStart,
                                onValueChange = {},
                                label = "",
                                currentFocusRequester = FocusRequester(),
                                nextFocusRequester = FocusRequester(),
                                readOnly = true,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            FocusableOutlinedTextField(
                                value = recordDateFinished,
                                onValueChange = {},
                                label = "",
                                currentFocusRequester = FocusRequester(),
                                nextFocusRequester = FocusRequester(),
                                readOnly = true,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            HoverableCheckbox(
                                modifier = Modifier.weight(0.5f).padding(end = 8.dp),
                                checked = isPaid,
                                onCheckedChange = {
                                    isPaid = !isPaid
                                    memberViewModel.updateMembershipRecordsIsPaid(
                                        index,
                                        record.copy(
                                            isPaid = isPaid
                                        )
                                    )
                                },
                                hoverText = "$price €",
                                showPopupOnHover = !isPaid
                            )
                            Checkbox(
                                modifier = Modifier.weight(0.5f).padding(end = 4.dp),
                                checked = record.isActive,
                                onCheckedChange = { },
                                enabled = false
                            )
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
                            records.forEachIndexed { index, record ->
                                memberViewModel.updateMembershipRecordsIsPaid(
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
                            memberViewModel.confirmMembershipRecordsIsPaid()
                            onClose()
                        },
                        buttonBackgroundColor = Color.Green
                    )
                }
            }
        }
    }
}