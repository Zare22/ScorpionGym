package hr.kotwave.scorpiongym.paymentauditlog.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import hr.kotwave.scorpiongym.paymentauditlog.PaymentAuditLogViewModel
import hr.kotwave.scorpiongym.ui.custom.elements.DatePickerField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate


@Composable
fun CashRegisterDialog(onClose: () -> Unit) {

    val paymentAuditLogViewModel: PaymentAuditLogViewModel = getKoin().get()
    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }
    var userSums by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    val lazyListState = rememberLazyListState(0)
    var periodSum by remember { mutableStateOf(0.0) }

    val triggerShowPaymentAuditLogs = {
        userSums = paymentAuditLogViewModel.getUserTotalByDateRange(fromDate, toDate)
        periodSum = userSums.values.sum()
    }

    Dialog(onDismissRequest = { onClose() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.5f).padding(16.dp).heightIn(max = 700.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyDown) {
                            triggerShowPaymentAuditLogs()
                            true
                        } else {
                            false
                        }
                    }
            ) {
                Text("Kasa", style = MaterialTheme.typography.h6)
                Divider(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    DatePickerField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        label = "Od",
                        modifier = Modifier.weight(1f),
                    )
                    DatePickerField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        label = "Do",
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                    )
                    HoverableButton(
                        text = "Danas",
                        onClick = {
                            val today = LocalDate.now()
                            fromDate = today
                            toDate = today
                            triggerShowPaymentAuditLogs()
                        }, modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    HoverableButton(
                        text = "Prikaži",
                        onClick = { triggerShowPaymentAuditLogs() },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f)) {
                    Row {
                        LazyColumn(
                            state = lazyListState, modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            items(userSums.entries.toList()) { entry ->
                                Card(
                                    elevation = 4.dp, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = entry.key,
                                            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = "%.2f€".format(entry.value),
                                            color = if (entry.value > 0) Color.Green else Color.Red,
                                            style = MaterialTheme.typography.body1,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )

                                    }
                                }
                            }
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(lazyListState),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = periodSum.toString(),
                        color = if (periodSum > 0) Color.Green else Color.Red,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    HoverableButton(
                        text = "Zatvori",
                        onClick = { onClose() },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}