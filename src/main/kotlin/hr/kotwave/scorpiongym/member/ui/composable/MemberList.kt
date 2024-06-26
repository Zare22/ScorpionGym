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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberFilterOption
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.memberotherservice.ui.window.AddMemberOtherServiceWindow
import hr.kotwave.scorpiongym.trainingsession.ui.window.AddTrainingSessionWindow
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.getKoin

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MemberList(members: List<Member>, onItemClick: (Member) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var memberFilterOption by remember { mutableStateOf<MemberFilterOption?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState(0)

    val sortedMembers = members.sortedWith(compareBy({ it.surname }, { it.name }))

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    MemberItem(member = member, onClick = { onItemClick(member) })
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
    var addTrainingSessionWindowOpened by remember { mutableStateOf(false) }
    var addMemberOtherServiceWindowOpened by remember { mutableStateOf(false) }

    if (addTrainingSessionWindowOpened) {
        Window(
            onCloseRequest = { addTrainingSessionWindowOpened = false },
            title = "Novi trening za člana",
            alwaysOnTop = true,
            state = WindowState(
                position = WindowPosition.Aligned(Alignment.Center),
                height = 200.dp, width = 600.dp
            ),
            icon = painterResource("ScorpionWindowIcon.png")
        ) {
            AddTrainingSessionWindow(member, onClose = { addTrainingSessionWindowOpened = false })
        }
    }

    if (addMemberOtherServiceWindowOpened) {
        Window(
            onCloseRequest = { addMemberOtherServiceWindowOpened = false },
            title = "Novi trening za člana",
            alwaysOnTop = true,
            state = WindowState(
                position = WindowPosition.Aligned(Alignment.Center),
                height = 400.dp, width = 600.dp
            ),
            icon = painterResource("ScorpionWindowIcon.png")
        ) {
            AddMemberOtherServiceWindow(member, onClose = { addMemberOtherServiceWindowOpened = false })
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
                listOf(
                    ContextMenuItem("Upiši trening članu") {
                        addTrainingSessionWindowOpened = true
                    },
                    ContextMenuItem("Upiši dodatnu uslugu članu") {
                        addMemberOtherServiceWindowOpened = true
                    }
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
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
        memberViewModel.memberRecords.find { record -> record.isActive } == null -> Color.Yellow
        else -> Color.Green
    }
}