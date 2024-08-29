package hr.kotwave.scorpiongym.membershiprecord.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import hr.kotwave.scorpiongym.di.rememberMemberViewModel
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecord
import hr.kotwave.scorpiongym.trainingsession.ui.dialog.TrainingSessionsDialog
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.Dropdown
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableCheckbox
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun UserMembershipRecordsDialog(member: Member, onClose: () -> Unit) {
    val memberViewModel: MemberViewModel = rememberMemberViewModel(member)
    val membershipViewModel: MembershipViewModel = getKoin().get()
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val listState = rememberLazyListState()
    val records by remember { derivedStateOf { memberViewModel.memberRecords.sortedBy { it.dateStarted } } }

    val initialIsPaidValues = remember { records.map { it.isPaid }.toMutableStateList() }
    val initialIsActiveValues = remember { records.map { it.isActive }.toMutableStateList() }

    var selectedRecordToDelete by remember { mutableStateOf<MembershipRecord?>(null) }
    var nameOfMembershipType by remember { mutableStateOf("") }
    var confirmRecordDeleteDialog by remember { mutableStateOf(false) }
    var showTrainingSessionsDialog by remember { mutableStateOf(false) }
    var selectedRecordId by remember { mutableStateOf(0) }

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
                            append("Ukoliko nastavite pobrisat ćete članarinu: ")

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
                                    memberViewModel.removeMembershipRecord(it)
                                } catch (e: Exception) {
                                    infoMessage = "Greška pri brisanju članarine"
                                    showInfoDialog = true
                                }
                                if (it.id != 0) memberViewModel.initViewModel()
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

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(records) { index, record ->
                        val membershipType =
                            membershipViewModel.memberships.find { membership -> membership.id == record.membershipId }

                        val membershipTypeName = membershipType?.name?.let { TextFieldValue(it) }
                        var recordDateStart by remember {
                            mutableStateOf(
                                TextFieldValue(
                                    record.dateStarted.format(
                                        dateFormatter
                                    )
                                )
                            )
                        }
                        var recordDateFinished by remember {
                            mutableStateOf(
                                TextFieldValue(
                                    record.dateFinished.format(
                                        dateFormatter
                                    )
                                )
                            )
                        }

                        var isPaid by remember { mutableStateOf(record.isPaid) }
                        var isActive by remember { mutableStateOf(record.isActive) }
                        var price = membershipType?.price

                        memberViewModel.currentMember.organizationId?.let { organizationId ->
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
                                        record.dateStarted.plusMonths(memberships.find { it.id == record.membershipId }?.duration
                                            ?: 1
                                        ).minusDays(1)

                                    recordDateFinished = TextFieldValue(
                                        record.dateFinished.format(
                                            dateFormatter
                                        )
                                    )

                                    expandedMembership[index] = false
                                },
                                focusRequester = FocusRequester(),
                                nextFocusRequester = FocusRequester(),
                                itemLabel = { it.name },
                                readOnly = record.id != 0
                            )
                            FocusableOutlinedTextField(
                                value = recordDateStart,
                                onValueChange = { newValue ->
                                    recordDateStart = newValue
                                    val parsedDateTime =
                                        runCatching { LocalDate.parse(newValue.text, dateFormatter) }.getOrNull()
                                    if (parsedDateTime != null) {
                                        memberViewModel.updateMembershipRecord(
                                            index,
                                            record.copy(dateStarted = parsedDateTime),
                                        )
                                    }
                                },
                                label = "",
                                currentFocusRequester = FocusRequester(),
                                nextFocusRequester = FocusRequester(),
                                canSwitchWithTab = false,
                                readOnly = !record.isActive && record.dateFinished.isBefore(LocalDate.now()),
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            FocusableOutlinedTextField(
                                value = recordDateFinished,
                                onValueChange = { newValue ->
                                    recordDateFinished = newValue
                                    val parsedDateTime =
                                        runCatching { LocalDate.parse(newValue.text, dateFormatter) }.getOrNull()
                                    if (parsedDateTime != null) {
                                        memberViewModel.updateMembershipRecord(
                                            index,
                                            record.copy(dateFinished = parsedDateTime),
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
                                    memberViewModel.updateMembershipRecord(
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
                                    memberViewModel.updateMembershipRecord(
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
                                    val newRecord = MembershipRecord(
                                        id = 0,
                                        memberId = member.id,
                                        membershipId = 0,
                                        dateStarted = records.maxByOrNull { it.dateFinished }?.dateFinished?.plusDays(1)
                                            ?: LocalDate.now(),
                                        dateFinished = records.maxByOrNull { it.dateStarted }?.dateStarted?.plusMonths(1)
                                            ?.minusDays(1) ?: LocalDate.now().plusMonths(1).minusDays(1),
                                        isActive = false,
                                        isPaid = false
                                    )
                                    memberViewModel.addFutureMembershipRecord(newRecord)
                                }
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
                            memberViewModel.removeUnconfirmedFutureMembershipRecords()
                            records.forEachIndexed { index, record ->
                                memberViewModel.updateMembershipRecord(
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
                    HoverableButton(
                        text = "Potvrdi promjene",
                        onClick = {
                            try {
                                memberViewModel.confirmMembershipRecordsUpdates()
                            } catch (e: Exception) {
                                infoMessage = "Greška pri ažuriranju članarina"
                                showInfoDialog = true
                            }
                            onClose()
                        },
                        buttonBackgroundColor = Color.Green
                    )
                }
            }
        }
    }
}