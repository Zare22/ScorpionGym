package hr.kotwave.scorpiongym.unregisteredservice.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import hr.kotwave.scorpiongym.otherservice.OtherServiceViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableCheckbox
import hr.kotwave.scorpiongym.ui.theme.Typography
import hr.kotwave.scorpiongym.unregisteredservice.UnregisteredService
import hr.kotwave.scorpiongym.unregisteredservice.UnregisteredServiceViewModel
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun UnregisteredServiceDialog(onClose: () -> Unit) {

    val unregisteredServiceViewModel: UnregisteredServiceViewModel = getKoin().get()
    val otherServiceViewModel: OtherServiceViewModel = getKoin().get()
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'u' HH:mm")
    val listState = rememberLazyListState()
    val unregisteredOtherServices by remember { derivedStateOf { unregisteredServiceViewModel.unregisteredServices } }
    val initialIsPaidValues = remember { unregisteredOtherServices.map { it.isPaid }.toMutableStateList() }

    var countOfPaidOtherServices by remember { mutableStateOf(unregisteredOtherServices.count { it.isPaid }) }
    var countOfUnpaidOtherServices by remember { mutableStateOf(unregisteredOtherServices.size - countOfPaidOtherServices) }

    var selectedUnregisteredOtherService by remember { mutableStateOf<UnregisteredService?>(null) }
    var nameOfOtherService by remember { mutableStateOf("") }
    var confirmOtherServiceDeleteDialog by remember { mutableStateOf(false) }

    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    when {
        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }

        confirmOtherServiceDeleteDialog -> {
            AlertDialog(
                onDismissRequest = { confirmOtherServiceDeleteDialog = false },
                title = { Text("Brisanje usluge", color = Color.Red) },
                text = {
                    Text(
                        text = buildAnnotatedString {
                            append("Ukoliko nastavite pobrisat ćete uslugu: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(nameOfOtherService)
                            }

                            append(" na datum: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(selectedUnregisteredOtherService?.dateOfService?.format(dateFormatter))
                            }
                        }
                    )
                },
                confirmButton = {
                    HoverableButton(
                        text = "Potvrdi",
                        buttonBackgroundColor = Color.Red,
                        onClick = {
                            selectedUnregisteredOtherService?.let {
                                unregisteredServiceViewModel.deleteUnregisteredService(
                                    it
                                )
                            }
                            confirmOtherServiceDeleteDialog = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { confirmOtherServiceDeleteDialog = false }
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
                        text = "Tip usluge",
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Datum usluge",
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
                    itemsIndexed(unregisteredOtherServices) { index, unregisteredOtherService ->
                        val otherServiceType =
                            otherServiceViewModel.otherServices.find { otherService -> otherService.id == unregisteredOtherService.otherServiceId }
                        val otherServiceTypeName = otherServiceType?.name?.let { TextFieldValue(it) }
                        var unregisteredOtherServiceDate by remember {
                            mutableStateOf(
                                TextFieldValue(
                                    unregisteredOtherService.dateOfService.format(
                                        dateFormatter
                                    )
                                )
                            )
                        }

                        var isPaid by remember { mutableStateOf(unregisteredOtherService.isPaid) }
                        val price = otherServiceType?.price

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FocusableOutlinedTextField(
                                value = otherServiceTypeName ?: TextFieldValue(""),
                                onValueChange = {},
                                label = "",
                                currentFocusRequester = FocusRequester(),
                                nextFocusRequester = FocusRequester(),
                                canSwitchWithTab = false,
                                readOnly = true,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            FocusableOutlinedTextField(
                                value = unregisteredOtherServiceDate,
                                onValueChange = { newValue ->
                                    unregisteredOtherServiceDate = newValue
                                    val parsedDateTime =
                                        runCatching { LocalDateTime.parse(newValue.text, dateFormatter) }.getOrNull()
                                    if (parsedDateTime != null && parsedDateTime != unregisteredOtherService.dateOfService) {
                                        unregisteredServiceViewModel.updateUnregisteredService(
                                            index,
                                            unregisteredOtherService.copy(dateOfService = parsedDateTime)
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
                                    unregisteredServiceViewModel.updateUnregisteredService(
                                        index,
                                        unregisteredOtherService.copy(
                                            isPaid = isPaid
                                        )
                                    )
                                },
                                hoverText = "$price €",
                                showPopupOnHover = !isPaid
                            )
                            IconButton(
                                onClick = {
                                    selectedUnregisteredOtherService = unregisteredOtherService
                                    nameOfOtherService = otherServiceTypeName?.text ?: ""
                                    confirmOtherServiceDeleteDialog = true
                                },
                                modifier = Modifier.weight(0.5f).padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Obriši",
                                    tint = Color.Red
                                )
                            }
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
                            unregisteredOtherServices.forEachIndexed { index, record ->
                                unregisteredServiceViewModel.updateUnregisteredService(
                                    index,
                                    record.copy(
                                        isPaid = initialIsPaidValues[index]
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
                            text = "Plaćeno: $countOfPaidOtherServices",
                            style = Typography.button,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Neplaćeno: $countOfUnpaidOtherServices",
                            style = Typography.button,
                            textAlign = TextAlign.Center
                        )
                    }
                    HoverableButton(
                        text = "Potvrdi promjene",
                        onClick = {
                            try {
                                unregisteredServiceViewModel.confirmUnregisteredServicesUpdates()
                                onClose()
                            } catch (_: Exception) {
                                infoMessage = "Greška pri ažuriranju ostale usluge"
                                showInfoDialog = true
                            }
                        },
                        buttonBackgroundColor = Color.Green
                    )
                }
            }
        }
    }
}