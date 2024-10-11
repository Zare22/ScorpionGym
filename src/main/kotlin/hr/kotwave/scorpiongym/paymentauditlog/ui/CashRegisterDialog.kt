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
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun CashRegisterDialog(onClose: () -> Unit) {

    val paymentAuditLogViewModel: PaymentAuditLogViewModel = getKoin().get()
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var userSums by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    val lazyListState = rememberLazyListState(0)

    val triggerShowPaymentAuditLogs = {
        val parsedFromDate =
            runCatching { LocalDate.parse(fromDate, DateTimeFormatter.ofPattern("dd.MM.yyyy")) }.getOrNull()
        val parsedToDate =
            runCatching { LocalDate.parse(toDate, DateTimeFormatter.ofPattern("dd.MM.yyyy")) }.getOrNull()

        userSums = paymentAuditLogViewModel.getUserTotalByDateRange(parsedFromDate, parsedToDate)
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

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { newValue -> fromDate = newValue },
                        label = { Text("Od (DD.MM.YYYY)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { newValue -> toDate = newValue },
                        label = { Text("Do (DD.MM.YYYY)") },
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                            fromDate = today
                            toDate = today
                            triggerShowPaymentAuditLogs()
                        }, modifier = Modifier.align(Alignment.CenterVertically).padding(start = 16.dp)
                    ) {
                        Text("Danas")
                    }
                    Button(
                        onClick = { triggerShowPaymentAuditLogs() },
                        modifier = Modifier.align(Alignment.CenterVertically).padding(start = 16.dp)
                    ) {
                        Text("Prikaži")
                    }
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

                Button(
                    onClick = { onClose() }, modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}