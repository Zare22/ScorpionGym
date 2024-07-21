package hr.kotwave.scorpiongym.member.ui.composable

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.di.rememberMemberViewModel
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberFilterOption
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.member.MembersListViewModel
import hr.kotwave.scorpiongym.memberotherservice.ui.dialog.AddMemberOtherServiceDialog
import hr.kotwave.scorpiongym.memberotherservice.ui.dialog.OtherServicesDialog
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.membershiprecord.ui.dialog.UserMembershipRecordsDialog
import hr.kotwave.scorpiongym.trainingsession.ui.dialog.AddSingleTrainingSessionDialog
import hr.kotwave.scorpiongym.trainingsession.ui.dialog.TrainingSessionsDialog
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.theme.Gold
import hr.kotwave.scorpiongym.util.Locales
import org.koin.java.KoinJavaComponent.getKoin
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun MemberList(onItemClick: (Member) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var memberFilterOption by remember { mutableStateOf<MemberFilterOption?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState(0)

    val membersListViewModel: MembersListViewModel = getKoin().get()

    val sortedMembers = membersListViewModel.members.sortedWith(
        compareBy({ it.surname.lowercase(Locales.CroatianLocale) },
            { it.name.lowercase(Locales.CroatianLocale) })
    )

    Column {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Pretraži po imenu i prezimenu") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(end = 8.dp)
                    .clickable { expanded = true }
            ) {
                TextField(
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                    value = memberFilterOption?.let { "Filter: ${it.displayName}" } ?: "Filtriraj članove",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        memberFilterOption = MemberFilterOption.PAID
                        expanded = false
                    }) {
                        Text("Aktivni(podmireni dugovi)")
                    }
                    DropdownMenuItem(onClick = {
                        memberFilterOption = MemberFilterOption.UNPAID
                        expanded = false
                    }) {
                        Text("Neplaćeno")
                    }
                    DropdownMenuItem(onClick = {
                        memberFilterOption = MemberFilterOption.NO_ACTIVE_SUBSCRIPTION
                        expanded = false
                    }) {
                        Text("Nema aktivne članarine")
                    }
                    DropdownMenuItem(onClick = {
                        memberFilterOption = null
                        expanded = false
                    }) {
                        Text("Očisti filter")
                    }
                }
            }
        }

        val filteredMembers = sortedMembers.filter { member ->
            val fullName = "${member.name} ${member.surname}".lowercase()
            val fullNameReversed = "${member.surname} ${member.name}".lowercase()
            val matchesQuery =
                searchQuery.isEmpty() || fullName.contains(searchQuery.lowercase()) || fullNameReversed.contains(
                    searchQuery.lowercase()
                )
            val matchesFilter = when (memberFilterOption) {
                MemberFilterOption.PAID -> getRibbonColor(member) == Color.Green
                MemberFilterOption.UNPAID -> getRibbonColor(member) == Color.Red
                MemberFilterOption.NO_ACTIVE_SUBSCRIPTION -> getRibbonColor(member) == Gold
                null -> true
            }

            matchesQuery && matchesFilter
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.padding(end = 12.dp),
                state = lazyListState
            ) {
                items(filteredMembers) { member ->
                    MemberItem(
                        member = member,
                        onClick = { onItemClick(member) }
                    )
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(lazyListState),
            )
        }
    }
}

@Composable
fun MemberItem(member: Member, onClick: () -> Unit) {
    var showAddSingleTrainingSessionDialog by remember { mutableStateOf(false) }
    var showAddMemberOtherServiceDialog by remember { mutableStateOf(false) }
    var showDeleteMemberDialogAlertOpened by remember { mutableStateOf(false) }
    var showManageTrainingSessionsDialog by remember { mutableStateOf(false) }
    var showMangeMembershipRecordsDialog by remember { mutableStateOf(false) }
    var showMangeMemberOtherServicesDialog by remember { mutableStateOf(false) }

    val membersListViewModel: MembersListViewModel = getKoin().get()
    val memberViewModel: MemberViewModel = rememberMemberViewModel(member)

    when {
        showAddSingleTrainingSessionDialog -> {
            AddSingleTrainingSessionDialog(member, onClose = { showAddSingleTrainingSessionDialog = false })
        }

        showAddMemberOtherServiceDialog -> {
            AddMemberOtherServiceDialog(member, onClose = { showAddMemberOtherServiceDialog = false })
        }

        showMangeMembershipRecordsDialog -> {
            UserMembershipRecordsDialog(member, onClose = { showMangeMembershipRecordsDialog = false })
        }

        showDeleteMemberDialogAlertOpened -> {
            AlertDialog(
                onDismissRequest = { showDeleteMemberDialogAlertOpened = false },
                title = { Text("Brisanje člana", color = Color.Red) },
                text = {
                    Text(
                        text = buildAnnotatedString {
                            append("Ukoliko nastavite pobrisat ćete člana ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("${memberViewModel.currentMember.name} ${memberViewModel.currentMember.surname}")
                            }

                            append(" i sve njegove vezane podatke!")
                        }
                    )
                },
                confirmButton = {
                    HoverableButton(
                        text = "Potvrdi",
                        buttonBackgroundColor = Color.Red,
                        onClick = {
                            membersListViewModel.deleteMember(member)
                            showDeleteMemberDialogAlertOpened = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { showDeleteMemberDialogAlertOpened = false }
                    )
                }
            )
        }

        showManageTrainingSessionsDialog -> {
            TrainingSessionsDialog(member, onClose = { showManageTrainingSessionsDialog = false })
        }

        showMangeMemberOtherServicesDialog -> {
            OtherServicesDialog(member, onClose = { showMangeMemberOtherServicesDialog = false })
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand, false),
        elevation = 4.dp
    ) {
        ContextMenuArea(
            items = {
                val items = mutableListOf<ContextMenuItem>()

                if (memberViewModel.activeMembershipRecord != null) {
                    items.add(ContextMenuItem(label = "Upiši trening članu") { showAddSingleTrainingSessionDialog = true })
                    items.add(ContextMenuItem("Pregled treninga aktivne članarine") {
                        showManageTrainingSessionsDialog = true
                    })
                }
                if (memberViewModel.memberRecords.isNotEmpty())
                    items.add(ContextMenuItem("Pregled svih članarina") { showMangeMembershipRecordsDialog = true })
                items.add(ContextMenuItem("Upiši dodatnu uslugu članu") { showAddMemberOtherServiceDialog = true })
                if (memberViewModel.memberOtherServices.isNotEmpty())
                    items.add(ContextMenuItem("Pregled svih ostalih usluga") { showMangeMemberOtherServicesDialog = true })
                items.add(ContextMenuItem("Obriši člana") { showDeleteMemberDialogAlertOpened = true })

                items
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = "${memberViewModel.currentMember.surname} ${memberViewModel.currentMember.name}",
                        style = MaterialTheme.typography.h6
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    memberViewModel.currentMember.dateOfBirth?.let { dateOfBirth ->
                        Text(
                            text = buildAnnotatedString {
                                append("Datum rođenja: ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(dateOfBirth.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                                }
                            },
                            style = MaterialTheme.typography.body2
                        )
                    }
                    memberViewModel.currentMember.phoneNumber?.takeIf { it.isNotEmpty() }?.let { phoneNumber ->
                        Text(
                            text = buildAnnotatedString {
                                append("Broj telefona: ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(phoneNumber)
                                }
                            },
                            style = MaterialTheme.typography.body2
                        )
                    }
                    if (memberViewModel.activeMembershipRecord != null) {
                        val membershipViewModel: MembershipViewModel = getKoin().get()
                        val typeOfMembership =
                            membershipViewModel.memberships.find { memb -> memb.id == memberViewModel.activeMembershipRecord!!.membershipId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                buildAnnotatedString {
                                    append("Tip aktivne članarine: ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        typeOfMembership?.let { append(it.name) }
                                    }
                                },
                                style = MaterialTheme.typography.body2
                            )
                            Text(
                                text = buildAnnotatedString {
                                    typeOfMembership?.takeIf { it.isNoLimit }?.run {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("No limit")
                                        }
                                    } ?: run {
                                        append("Preostalo treninga u aktivnoj članarini: ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            val remainingTrainings = memberViewModel.numberOfTrainingsAvailable - memberViewModel.trainingSessionsInActiveMembership.size
                                            append("$remainingTrainings")
                                        }
                                    }

                                },
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(20.dp)
                        .height(20.dp)
                        .background(getRibbonColor(member))
                )
            }
        }
    }
}

@Composable
fun getRibbonColor(member: Member): Color {
    val memberViewModel: MemberViewModel = rememberMemberViewModel(member)

    return when {
        memberViewModel.memberRecords.count { record -> !record.isPaid } >= 1 -> Color.Red
        memberViewModel.memberRecords.find { record -> record.isActive } == null -> Gold
        else -> Color.Green
    }
}