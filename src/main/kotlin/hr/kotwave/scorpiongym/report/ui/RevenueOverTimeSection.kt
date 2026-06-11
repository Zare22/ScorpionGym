package hr.kotwave.scorpiongym.report.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.report.ReportViewModel
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate

/** R3 — "Prihodi kroz vrijeme": net cash booked per calendar month. */
@Composable
fun RevenueOverTimeSection() {
    val reportViewModel: ReportViewModel = getKoin().get()

    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }

    val report = reportViewModel.revenueOverTime
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Prihodi kroz vrijeme (mjesečno)", style = MaterialTheme.typography.h6)
        Spacer(Modifier.height(8.dp))

        PeriodPickerRow(
            from = fromDate,
            to = toDate,
            onFromChange = { fromDate = it },
            onToChange = { toDate = it },
            onShow = { reportViewModel.loadRevenueOverTime(fromDate, toDate) },
        )

        Spacer(Modifier.height(16.dp))

        if (report == null) {
            Text("Odaberite razdoblje i kliknite Prikaži.", style = MaterialTheme.typography.body2)
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Mjesec", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Naplaćeno", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
            Divider()

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    if (report.rows.isEmpty()) {
                        item {
                            Text(
                                "Nema prihoda u odabranom razdoblju.",
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.body2,
                            )
                        }
                    }
                    items(report.rows) { row ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text(formatMonth(row.month), modifier = Modifier.weight(1f))
                            Text("%.2f €".format(row.netCollected), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                        Divider()
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(listState),
                )
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Ukupno", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("%.2f €".format(report.total), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
        }
    }
}

/** "2025-01" -> "01.2025." (Croatian month display); falls back to the raw value. */
private fun formatMonth(ym: String): String {
    val parts = ym.split("-")
    return if (parts.size == 2) "${parts[1]}.${parts[0]}." else ym
}
