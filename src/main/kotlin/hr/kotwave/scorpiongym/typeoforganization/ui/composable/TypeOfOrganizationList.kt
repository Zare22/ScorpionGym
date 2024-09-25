package hr.kotwave.scorpiongym.typeoforganization.ui.composable

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
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.util.Locales
import hr.kotwave.scorpiongym.util.PreferencesHelper
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun TypeOfOrganizationList(onItemClick: (TypeOfOrganization) -> Unit) {
    val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()
    var searchQuery by remember { mutableStateOf("") }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pretraži tip organizacije") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        val filteredTypeOfOrganizations = if (searchQuery.isEmpty()) {
            typeOfOrganizationViewModel.organizationTypes.sortedWith(compareBy { it.name.lowercase(Locales.CroatianLocale) })
        } else {
            typeOfOrganizationViewModel.organizationTypes.sortedWith(compareBy { it.name.lowercase(Locales.CroatianLocale) })
                .filter { organization ->
                    organization.name.contains(searchQuery, ignoreCase = true)
                }
        }

        LazyColumn {
            items(filteredTypeOfOrganizations) { typeOfOrganization ->
                TypeOfOrganizationItem(
                    typeOfOrganization = typeOfOrganization,
                    onClick = { onItemClick(typeOfOrganization) })
            }
        }
    }
}

@Composable
fun TypeOfOrganizationItem(typeOfOrganization: TypeOfOrganization, onClick: () -> Unit) {
    val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()

    var showDeleteTypeOfOrganizationDialogAlert by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    when {
        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }

        showDeleteTypeOfOrganizationDialogAlert -> {
            AlertDialog(
                onDismissRequest = { showDeleteTypeOfOrganizationDialogAlert = false },
                title = { Text("Brisanje tipa organizacije", color = Color.Red) },
                text = {
                    Text(
                        text = buildAnnotatedString {
                            append("Ukoliko nastavite pobrisat ćete tip organizacije ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(typeOfOrganization.name)
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
                                typeOfOrganizationViewModel.deleteTypeOfOrganization(typeOfOrganization)
                            } catch (e: Exception) {
                                infoMessage = "Nije moguće pobrisati tip organizacije jer se koristi!"
                                showInfoDialog = true
                            }
                            showDeleteTypeOfOrganizationDialogAlert = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { showDeleteTypeOfOrganizationDialogAlert = false }
                    )
                }
            )
        }
    }
    ContextMenuArea(
        items = {
            val items = mutableListOf<ContextMenuItem>()
            if (PreferencesHelper().isAdmin)
                items.add(ContextMenuItem("Obriši tip organizacije") { showDeleteTypeOfOrganizationDialogAlert = true })
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
                Text(text = typeOfOrganization.name, style = MaterialTheme.typography.h6)
            }
        }
    }
}