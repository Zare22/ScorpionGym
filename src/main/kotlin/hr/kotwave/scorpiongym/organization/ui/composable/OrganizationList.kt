package hr.kotwave.scorpiongym.organization.ui.composable

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
import hr.kotwave.scorpiongym.organization.Organization
import hr.kotwave.scorpiongym.organization.OrganizationViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.util.Locales
import hr.kotwave.scorpiongym.util.PreferencesHelper
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun OrganizationList(onItemClick: (Organization) -> Unit) {
    val organizationViewModel: OrganizationViewModel = getKoin().get()
    var searchQuery by remember { mutableStateOf("") }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pretraži organizaciju") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        val filteredOrganizations = if (searchQuery.isEmpty()) {
            organizationViewModel.organizations.sortedWith(compareBy { it.name.lowercase(Locales.CroatianLocale) })
        } else {
            organizationViewModel.organizations.sortedWith(compareBy { it.name.lowercase(Locales.CroatianLocale) })
                .filter { organization ->
                    organization.name.contains(searchQuery, ignoreCase = true)
                }
        }

        LazyColumn {
            items(filteredOrganizations) { organization ->
                OrganizationItem(organization = organization, onClick = { onItemClick(organization) })
            }
        }
    }
}


@Composable
fun OrganizationItem(organization: Organization, onClick: () -> Unit) {

    val organizationViewModel: OrganizationViewModel = getKoin().get()

    var showDeleteOrganizationDialogAlert by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    when {
        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }

        showDeleteOrganizationDialogAlert -> {
            AlertDialog(
                onDismissRequest = { showDeleteOrganizationDialogAlert = false },
                title = { Text("Brisanje organizacije", color = Color.Red) },
                text = {
                    Text(
                        text = buildAnnotatedString {
                            append("Ukoliko nastavite pobrisat ćete organizaciju ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(organization.name)
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
                                organizationViewModel.deleteOrganization(organization)
                            } catch (e: Exception) {
                                infoMessage = "Nije moguće pobrisati organizaciju jer se koristi!"
                                showInfoDialog = true
                            }
                            showDeleteOrganizationDialogAlert = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { showDeleteOrganizationDialogAlert = false }
                    )
                }
            )
        }
    }
    ContextMenuArea(
        items = {
            val items = mutableListOf<ContextMenuItem>()
            if (PreferencesHelper().isAdmin)
                items.add(ContextMenuItem("Obriši organizaciju") { showDeleteOrganizationDialogAlert = true })
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
                Text(text = organization.name, style = MaterialTheme.typography.h6)
            }
        }
    }
}