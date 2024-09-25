package hr.kotwave.scorpiongym.otherservice.ui.composable

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
import hr.kotwave.scorpiongym.otherservice.OtherService
import hr.kotwave.scorpiongym.otherservice.OtherServiceViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.util.Locales
import hr.kotwave.scorpiongym.util.PreferencesHelper
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun OtherServiceList(onItemClick: (OtherService) -> Unit) {
    val otherServiceViewModel: OtherServiceViewModel = getKoin().get()
    var searchQuery by remember { mutableStateOf("") }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pretraži ostale usluge") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        val filteredOtherServices = if (searchQuery.isEmpty()) {
            otherServiceViewModel.otherServices.sortedWith(compareBy { it.name.lowercase(Locales.CroatianLocale) })
        } else {
            otherServiceViewModel.otherServices.sortedWith(compareBy { it.name.lowercase(Locales.CroatianLocale) })
                .filter { otherService ->
                    otherService.name.contains(searchQuery, ignoreCase = true)
                }
        }

        LazyColumn {
            items(filteredOtherServices) { otherService ->
                OtherServiceItem(otherService = otherService, onClick = { onItemClick(otherService) })
            }
        }
    }
}

@Composable
fun OtherServiceItem(otherService: OtherService, onClick: () -> Unit) {
    val otherServiceViewModel: OtherServiceViewModel = getKoin().get()

    var showDeleteOtherServiceDialogAlert by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    when {
        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }

        showDeleteOtherServiceDialogAlert -> {
            AlertDialog(
                onDismissRequest = { showDeleteOtherServiceDialogAlert = false },
                title = { Text("Brisanje ostale usluge", color = Color.Red) },
                text = {
                    Text(
                        text = buildAnnotatedString {
                            append("Ukoliko nastavite pobrisat ćete ostalu uslugu ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(otherService.name)
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
                                otherServiceViewModel.deleteOtherService(otherService)
                            } catch (e: Exception) {
                                infoMessage = "Nije moguće pobrisati ostalu uslugu jer se koristi!"
                                showInfoDialog = true
                            }
                            showDeleteOtherServiceDialogAlert = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { showDeleteOtherServiceDialogAlert = false }
                    )
                }
            )
        }
    }
    ContextMenuArea(
        items = {
            val items = mutableListOf<ContextMenuItem>()
            if (PreferencesHelper().isAdmin)
                items.add(ContextMenuItem("Obriši ostalu uslugu") { showDeleteOtherServiceDialogAlert = true })
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
                Text(otherService.name, style = MaterialTheme.typography.h6)
            }
        }
    }
}