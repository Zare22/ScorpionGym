package hr.kotwave.scorpiongym.unregisteredservice.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.otherservice.OtherServiceViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.Dropdown
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.unregisteredservice.UnregisteredService
import hr.kotwave.scorpiongym.unregisteredservice.UnregisteredServiceViewModel
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AddUnregisteredServiceDialog(onClose: () -> Unit) {
    val unregisteredServiceViewModel: UnregisteredServiceViewModel = getKoin().get()

    val otherServiceViewModel: OtherServiceViewModel = getKoin().get()
    val membershipViewModel: MembershipViewModel = getKoin().get()

    val filteredMemberships = membershipViewModel.memberships.filter { it.numberOfTrainingsAvailable == 1 }

    val combinedList: List<ServiceItem> = otherServiceViewModel.otherServices.map {
        ServiceItem.OtherServiceItem(id = it.id, name = it.name, price = it.price)
    } + filteredMemberships.map {
        ServiceItem.MembershipItem(id = it.id, name = it.name, price = it.price)
    }

    var selectedId by remember { mutableStateOf("") }
    var selectedPrice by remember { mutableStateOf(0.0) }
    var servicesExpanded by remember { mutableStateOf(false) }

    var otherServiceId: Int? by remember { mutableStateOf(null) }
    var membershipId: Int? by remember { mutableStateOf(null) }
    var isPaid by remember { mutableStateOf(false) }

    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    when {
        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }
    }

    Dialog(onDismissRequest = { onClose() }) {
        Card(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Dropdown(
                    expanded = servicesExpanded,
                    onExpandedChange = { servicesExpanded = it },
                    label = "Usluga ili trening",
                    items = combinedList,
                    selectedItem = combinedList.find {
                        when (it) {
                            is ServiceItem.OtherServiceItem -> it.id == otherServiceId
                            is ServiceItem.MembershipItem -> it.id == membershipId
                        }
                    },
                    onItemSelected = { selectedItem ->
                        selectedId = when (selectedItem) {
                            is ServiceItem.OtherServiceItem -> {
                                otherServiceId = selectedItem.id
                                membershipId = null
                                selectedPrice = selectedItem.price
                                selectedItem.id.toString()
                            }
                            is ServiceItem.MembershipItem -> {
                                membershipId = selectedItem.id
                                otherServiceId = null
                                selectedPrice = selectedItem.price
                                selectedItem.id.toString()
                            }
                        }
                    },
                    focusRequester = FocusRequester(),
                    nextFocusRequester = FocusRequester(),
                    canSwitchWithTab = false,
                    itemLabel = { item ->
                        when (item) {
                            is ServiceItem.OtherServiceItem -> "Usluga: ${item.name}"
                            is ServiceItem.MembershipItem -> "Trening: ${item.name}"
                        }
                    }
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = buildAnnotatedString {
                        append("Upisat ćete odabranu uslugu za neregistriranog člana na datum: ")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")))
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (selectedId.isNotEmpty()) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = buildAnnotatedString {
                            append("Cijena usluge je: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("$selectedPrice €")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            modifier = Modifier.wrapContentWidth(),
                            checked = isPaid,
                            onCheckedChange = { isPaid = !isPaid }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Plaćeno")
                    }

                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HoverableButton(
                        onClick = { onClose() }, text = "Povratak"
                    )
                    HoverableButton(
                        onClick = {
                            selectedId.toIntOrNull()?.let {
                                val unregisteredService = UnregisteredService(
                                    dateOfService = LocalDateTime.now(),
                                    membershipId = membershipId,
                                    otherServiceId = otherServiceId,
                                    isPaid = isPaid,
                                )
                                try {
                                    unregisteredServiceViewModel.addUnregisteredService(unregisteredService)
                                } catch (_: Exception) {
                                    infoMessage = "Greška pri dodavanju usluge"
                                    showInfoDialog = true
                                }
                            }
                            onClose()
                        },
                        text = "Potvrdi",
                        buttonBackgroundColor = Color.Green
                    )
                }
            }
        }
    }
}


sealed class ServiceItem {
    data class OtherServiceItem(val id: Int, val name: String, val price: Double) : ServiceItem()
    data class MembershipItem(val id: Int, val name: String, val price: Double) : ServiceItem()
}