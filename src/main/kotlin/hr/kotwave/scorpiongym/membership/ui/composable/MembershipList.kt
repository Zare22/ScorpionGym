package hr.kotwave.scorpiongym.membership.ui.composable

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.membership.Membership
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.util.Locales
import hr.kotwave.scorpiongym.util.PreferencesHelper
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun MembershipList(onItemClick: (Membership) -> Unit) {
    val membershipViewModel: MembershipViewModel = getKoin().get()
    var searchQuery by remember { mutableStateOf("") }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pretraži tip članarine") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        val filteredMemberships = if (searchQuery.isEmpty()) {
            membershipViewModel.memberships.sortedWith(compareBy { it.name.lowercase(Locales.CroatianLocale) })
        } else {
            membershipViewModel.memberships.sortedWith(compareBy { it.name.lowercase(Locales.CroatianLocale) })
                .filter { membership ->
                    membership.name.contains(searchQuery, ignoreCase = true)
                }
        }

        LazyColumn {
            items(filteredMemberships) { membership ->
                MembershipItem(membership = membership, onClick = { onItemClick(membership) })
            }
        }
    }
}


@Composable
fun MembershipItem(membership: Membership, onClick: () -> Unit) {

    val membershipViewModel: MembershipViewModel = getKoin().get()

    var showDeleteMembershipDialogAlert by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    when {
        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }

        showDeleteMembershipDialogAlert -> {
            AlertDialog(
                onDismissRequest = { showDeleteMembershipDialogAlert = false },
                title = { Text("Brisanje članarine", color = Color.Red) },
                text = {
                    Text(
                        text = buildAnnotatedString {
                            append("Ukoliko nastavite pobrisat ćete članarinu ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(membership.name)
                            }
                        }
                    )
                },
                confirmButton = {
                    HoverableButton(
                        text = "Potvrdi",
                        buttonBackgroundColor = Color.Red,
                        onClick = {
                            try {
                                membershipViewModel.removeMembership(membership)
                            } catch (e: Exception) {
                                infoMessage = "Nije moguće pobrisati članarinu jer se koristi!"
                                showInfoDialog = true
                            }
                            showDeleteMembershipDialogAlert = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { showDeleteMembershipDialogAlert = false }
                    )
                }
            )
        }
    }
    ContextMenuArea(
        items = {
            val items = mutableListOf<ContextMenuItem>()
            if (PreferencesHelper().isAdmin)
                items.add(ContextMenuItem("Obriši članarinu") { showDeleteMembershipDialogAlert = true })
            items
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable(onClick = onClick)
                .pointerHoverIcon(PointerIcon.Hand, false),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = membership.name, style = MaterialTheme.typography.h6)
            }
        }
    }
}