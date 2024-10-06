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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import hr.kotwave.scorpiongym.paymentauditlog.PaymentAuditLogViewModel
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun CashRegisterDialog(onClose: () -> Unit) {

    val paymentAuditLogViewModel: PaymentAuditLogViewModel = getKoin().get()
    var filterDate by remember { mutableStateOf("") }
    var userSums by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    val lazyListState = rememberLazyListState(0)

    Dialog(onDismissRequest = { onClose() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 700.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Kasa", style = MaterialTheme.typography.h6)

                // Row with Date Input and "Prikaži" Button
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    OutlinedTextField(
                        value = filterDate,
                        onValueChange = { newValue ->
                            filterDate = newValue
                        },
                        label = { Text("Unesite datum (DD.MM.YYYY)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            // Parse and fetch data
                            val parsedDate = runCatching {
                                LocalDate.parse(filterDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                            }.getOrNull()

                            if (parsedDate != null) {
                                userSums = paymentAuditLogViewModel.getUserTotalByDate(parsedDate)
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    ) {
                        Text("Prikaži")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Summaries
                Box(modifier = Modifier.weight(1f)) {
                    Row {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            items(userSums.entries.toList()) { entry ->
                                Card(
                                    elevation = 4.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
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
                    onClick = { onClose() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
