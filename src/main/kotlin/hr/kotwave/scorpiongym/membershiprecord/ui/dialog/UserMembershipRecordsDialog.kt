package hr.kotwave.scorpiongym.membershiprecord.ui.dialog

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import hr.kotwave.scorpiongym.di.rememberMemberDetailsViewModel
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberDetailsViewModel
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecord
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecordDao
import hr.kotwave.scorpiongym.membershiprecord.chooseRenewalStartDate
import hr.kotwave.scorpiongym.trainingsession.ui.dialog.TrainingSessionsDialog
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.DatePickerField
import hr.kotwave.scorpiongym.ui.custom.elements.Dropdown
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableCheckbox
import hr.kotwave.scorpiongym.ui.theme.Typography
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun UserMembershipRecordsDialog(member: Member, onClose: () -> Unit) {
    val memberDetailsViewModel: MemberDetailsViewModel = rememberMemberDetailsViewModel(member)
    val membershipViewModel: MembershipViewModel = getKoin().get()
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val listState = rememberLazyListState()
    val records by remember { derivedStateOf { memberDetailsViewModel.memberRecords } }

    val initialIsPaidValues = remember { records.map { it.isPaid }.toMutableStateList() }
    val initialIsActiveValues = remember { records.map { it.isActive }.toMutableStateList() }

    var selectedRecordToDelete by remember { mutableStateOf<MembershipRecord?>(null) }
    var nameOfMembershipType by remember { mutableStateOf("") }
    var confirmRecordDeleteDialog by remember { mutableStateOf(false) }
    var showTrainingSessionsDialog by remember { mutableStateOf(false) }
    var expiredMembershipDialogOpened by remember { mutableStateOf(false) }
    var selectedRecordId by remember { mutableStateOf(0) }
    var countOfPaidRecords by remember { mutableStateOf(records.count { it.isPaid }) }
    var countOfUnpaidRecords by remember { mutableStateOf(records.size - countOfPaidRecords) }

    val expandedMembership = remember { mutableStateMapOf<Int, Boolean>() }

    val memberships = getKoin().get<MembershipViewModel>().memberships

    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    when {

        showTrainingSessionsDialog -> {
            TrainingSessionsDialog(member, onClose = { showTrainingSessionsDialog = false }, selectedRecordId)
        }

        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }

        confirmRecordDeleteDialog -> {
            AlertDialog(
                onDismissRequest = { confirmRecordDeleteDialog = false },
                title = { Text("Brisanje članarine", color = Color.Red) },
                text = {
                    Text(
                        text = buildAnnotatedString {
                            append("Ukoliko nastavite pobrisat ćete sve vezane treninge i članarinu: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(nameOfMembershipType)
                            }

                            append(" sa početkom na datum: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(selectedRecordToDelete?.dateStarted?.format(dateFormatter))
                            }

                            append(" i završetkom na datum: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(selectedRecordToDelete?.dateFinished?.format(dateFormatter))
                            }
                        }
                    )
                },
                confirmButton = {
                    HoverableButton(
                        text = "Potvrdi",
                        buttonBackgroundColor = Color.Red,
                        onClick = {
                            selectedRecordToDelete?.let {
                                try {
                                    memberDetailsViewModel.deleteMembershipRecord(it)
                                } catch (_: Exception) {
                                    infoMessage = "Greška pri brisanju članarine"
                                    showInfoDialog = true
                                }
                                if (it.id != 0) memberDetailsViewModel.initViewModel()
                            }
                            confirmRecordDeleteDialog = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { confirmRecordDeleteDialog = false }
                    )
                }
            )
        }
    }


    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            memberDetailsViewModel.removeUnconfirmedFutureMembershipRecords()
            onClose()
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .fillMaxWidth(0.8f)
        ) {
            Column(modifier = Modifier.fillMaxHeight().padding(16.dp)) {
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
                    Text(
                        text = "Treninzi",
                        modifier = Modifier.weight(0.5f).padding(end = 4.dp),
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

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(start = 6.dp, end = 6.dp)
                    ) {
                        itemsIndexed(records) { index, record ->
                            val membershipType =
                                membershipViewModel.memberships.find { membership -> membership.id == record.membershipId }

                            val membershipTypeName = membershipType?.name?.let { TextFieldValue(it) }
                            var recordDateStart by remember { mutableStateOf(record.dateStarted) }
                            var recordDateFinished by remember { mutableStateOf(record.dateFinished) }

                            var isPaid by remember { mutableStateOf(record.isPaid) }
                            var isActive by remember { mutableStateOf(record.isActive) }
                            var price = membershipType?.price

                            memberDetailsViewModel.currentMember.organizationId?.let { organizationId ->
                                if (organizationId != 0) {
                                    val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()
                                    val typeOfOrganization =
                                        typeOfOrganizationViewModel.organizationTypes.find { typeOfOrg ->
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

                            val expanded = expandedMembership.getOrDefault(index, false)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Dropdown(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    expanded = expanded,
                                    onExpandedChange = { expandedMembership[index] = it },
                                    label = "Tip članarine",
                                    items = memberships,
                                    selectedItem = memberships.find { it.id.toString() == record.membershipId.toString() },
                                    onItemSelected = {
                                        record.membershipId = it.id
                                        record.dateFinished =
                                            record.dateStarted.plusMonths(
                                                memberships.find { membership -> membership.id == record.membershipId }?.duration
                                                    ?: 1
                                            ).minusDays(1)
                                        recordDateFinished = record.dateFinished

                                        expandedMembership[index] = false
                                    },
                                    focusRequester = FocusRequester(),
                                    nextFocusRequester = FocusRequester(),
                                    itemLabel = { it.name }
                                )
                                DatePickerField(
                                    value = recordDateStart,
                                    onValueChange = { newValue ->
                                        if (newValue != null && newValue != record.dateStarted) {
                                            recordDateStart = newValue
                                            memberDetailsViewModel.updateMembershipRecord(
                                                index,
                                                record.copy(dateStarted = newValue),
                                            )
                                        }
                                    },
                                    label = "",
                                    enabled = !(!record.isActive && record.dateFinished.isBefore(LocalDate.now())),
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                DatePickerField(
                                    value = recordDateFinished,
                                    onValueChange = { newValue ->
                                        if (newValue != null) {
                                            recordDateFinished = newValue
                                            memberDetailsViewModel.updateMembershipRecord(
                                                index,
                                                record.copy(dateFinished = newValue),
                                            )
                                        }
                                    },
                                    label = "",
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                HoverableCheckbox(
                                    modifier = Modifier.weight(0.5f).padding(end = 8.dp),
                                    checked = isPaid,
                                    onCheckedChange = {
                                        isPaid = !isPaid
                                        memberDetailsViewModel.updateMembershipRecord(
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
                                    onCheckedChange = {
                                        isActive = !isActive
                                        memberDetailsViewModel.updateMembershipRecord(
                                            index,
                                            record.copy(
                                                isActive = isActive
                                            )
                                        )
                                    },
                                    enabled = !records.any { it.isActive } && record.dateFinished.isAfter(LocalDate.now())
                                )
                                IconButton(
                                    onClick = {
                                        selectedRecordId = record.id
                                        showTrainingSessionsDialog = true
                                    },
                                    modifier = Modifier.weight(0.5f).padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Pokaži treninge",
                                        tint = Color.Green
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        selectedRecordToDelete = record
                                        nameOfMembershipType = membershipTypeName?.text ?: ""
                                        confirmRecordDeleteDialog = true
                                    },
                                    modifier = Modifier.weight(0.5f).padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                HoverableButton(
                                    text = "+ Dodaj novu članarinu",
                                    onClick = {
                                        val dateStarted = chooseRenewalStartDate(
                                            today = LocalDate.now(),
                                            existingRecords = memberDetailsViewModel.memberRecords
                                        )
                                        val dateFinished = dateStarted.plusMonths(1).minusDays(1)
                                        val newRecord = MembershipRecord(
                                            id = 0,
                                            memberId = member.id,
                                            membershipId = 0,
                                            dateStarted = dateStarted,
                                            dateFinished = dateFinished,
                                            isActive = false,
                                            isPaid = false
                                        )
                                        memberDetailsViewModel.addFutureMembershipRecord(newRecord)
                                    }
                                )
                            }
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(listState),
                    )
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
                            memberDetailsViewModel.removeUnconfirmedFutureMembershipRecords()
                            records.forEachIndexed { index, record ->
                                memberDetailsViewModel.updateMembershipRecord(
                                    index,
                                    record.copy(
                                        isPaid = initialIsPaidValues[index],
                                        isActive = initialIsActiveValues[index],
                                    )
                                )
                            }
                            onClose()
                        }
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = "Plaćeno: $countOfPaidRecords",
                            style = Typography.button,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Neplaćeno: $countOfUnpaidRecords",
                            style = Typography.button,
                            textAlign = TextAlign.Center
                        )
                    }
                    HoverableButton(
                        text = "Potvrdi promjene",
                        onClick = {
                            try {
                                memberDetailsViewModel.confirmMembershipRecordsUpdates()
                            } catch (_: Exception) {
                                infoMessage = "Greška pri ažuriranju članarina"
                                showInfoDialog = true
                            }
                            if (memberDetailsViewModel.activeMembershipRecord != null && memberDetailsViewModel.trainingSessionsInActiveMembership.size >= memberDetailsViewModel.numberOfTrainingsAvailable) {
                                val membershipRecordDao: MembershipRecordDao = getKoin().get()
                                memberDetailsViewModel.activeMembershipRecord?.copy(
                                    isActive = false,
                                    dateFinished = LocalDate.now()
                                )?.let { membershipRecordDao.updateMembershipRecord(it) }
                                memberDetailsViewModel.initViewModel()
                                expiredMembershipDialogOpened = true
                            } else onClose()
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