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
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberFilterOption
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.member.MembersListViewModel
import hr.kotwave.scorpiongym.memberotherservice.ui.window.AddMemberOtherServiceDialog
import hr.kotwave.scorpiongym.trainingsession.ui.window.AddTrainingSessionDialog
import hr.kotwave.scorpiongym.trainingsession.ui.window.TrainingSessionsDialog
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.getKoin

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun MemberList(onItemClick: (Member) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var memberFilterOption by remember { mutableStateOf<MemberFilterOption?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState(0)

    val membersListViewModel: MembersListViewModel = getKoin().get()
    val sortedMembers = membersListViewModel.members.sortedWith(compareBy({ it.surname }, { it.name }))

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
                        Text("Podmireni svi dugovi")
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
                MemberFilterOption.PAID -> getRibbonColor(member) == Color.Green // No specific filtering for this option
                MemberFilterOption.UNPAID -> getRibbonColor(member) == Color.Red
                MemberFilterOption.NO_ACTIVE_SUBSCRIPTION -> getRibbonColor(member) == Color.Yellow
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
    var addTrainingSessionDialogOpened by remember { mutableStateOf(false) }
    var addMemberOtherServiceDialogOpened by remember { mutableStateOf(false) }
    var showDeleteDialogAlert by remember { mutableStateOf(false) }

    var trainingSessionsDialogOpened by remember { mutableStateOf(false) }

    val membersListViewModel: MembersListViewModel = getKoin().get()

    when {
        addTrainingSessionDialogOpened -> {
            AddTrainingSessionDialog(member, onClose = { addTrainingSessionDialogOpened = false })
        }

        addMemberOtherServiceDialogOpened -> {
            AddMemberOtherServiceDialog(member, onClose = { addMemberOtherServiceDialogOpened = false })
        }

        showDeleteDialogAlert -> {
            AlertDialog(
                onDismissRequest = { showDeleteDialogAlert = false },
                title = { Text("Brisanje člana", color = Color.Red) },
                text = { Text("Ukoliko nastavite pobrisat ćete člana ${member.name} ${member.surname} i sve njegove vezane podatke") },
                confirmButton = {
                    HoverableButton(
                        text = "Potvrdi",
                        buttonBackgroundColor = Color.Red,
                        onClick = { membersListViewModel.deleteMember(member) }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { showDeleteDialogAlert = false }
                    )
                }
            )
        }

        trainingSessionsDialogOpened -> {
            TrainingSessionsDialog(member, onClose = { trainingSessionsDialogOpened = false })
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

                if (member.membershipRecordId != null && member.membershipRecordId != 0) {
                    items.add(ContextMenuItem(label = "Upiši trening članu") { addTrainingSessionDialogOpened = true })
                    items.add(ContextMenuItem("Pregled treninga aktivne članarine") { trainingSessionsDialogOpened = true })
                }
                items.add(ContextMenuItem("Upiši dodatnu uslugu članu") { addMemberOtherServiceDialogOpened = true })
                items.add(ContextMenuItem("Obriši člana") { showDeleteDialogAlert = true })

                items
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(text = "${member.surname} ${member.name}", style = MaterialTheme.typography.h6)
                    Text(text = member.phoneNumber ?: "", style = MaterialTheme.typography.body2)
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


fun getRibbonColor(member: Member): Color {
    val memberViewModel: MemberViewModel = getKoin().get { parametersOf(member) }

    return when {
        memberViewModel.memberRecords.count { record -> !record.isPaid } >= 1 -> Color.Red
        memberViewModel.memberRecords.find { record -> record.isActive } == null -> Color(0xFFFFC107)
        else -> Color.Green
    }
}

