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
import hr.kotwave.scorpiongym.report.print.ReportExporter
import hr.kotwave.scorpiongym.report.print.formatPeriod
import hr.kotwave.scorpiongym.report.print.toPrintable
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")

/** R5 — "Dugovanja": list of every currently-unpaid item so debts can be chased. */
@Composable
fun OutstandingSection() {
    val reportViewModel: ReportViewModel = getKoin().get()

    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }

    val report = reportViewModel.outstanding
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        ReportSectionHeader(
            "Nepodmirena dugovanja",
            onPrint = report?.let { r ->
                { ReportExporter.exportAndOpen(r.toPrintable("Nepodmirena dugovanja", formatPeriod(fromDate, toDate))) }
            },
        )
        Spacer(Modifier.height(8.dp))

        PeriodPickerRow(
            from = fromDate,
            to = toDate,
            onFromChange = { fromDate = it },
            onToChange = { toDate = it },
            onShow = { reportViewModel.loadOutstanding(fromDate, toDate) },
        )

        Spacer(Modifier.height(16.dp))

        if (report == null) {
            Text(
                "Ostavite datume prazne za sva dugovanja, ili odaberite razdoblje. Kliknite Prikaži.",
                style = MaterialTheme.typography.body2,
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Član", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("Stavka", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("Datum", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                Text("Iznos", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
            Divider()

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    if (report.rows.isEmpty()) {
                        item {
                            Text(
                                "Nema nepodmirenih dugovanja.",
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.body2,
                            )
                        }
                    }
                    items(report.rows) { row ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text(row.memberName ?: "Neregistrirano", modifier = Modifier.weight(2f))
                            Text(row.description, modifier = Modifier.weight(2f))
                            Text(row.date.format(DATE_FORMAT), modifier = Modifier.weight(1.5f))
                            Text("%.2f €".format(row.amount), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
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
                Text("Ukupno (${report.count})", modifier = Modifier.weight(5.5f), fontWeight = FontWeight.Bold)
                Text("%.2f €".format(report.total), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
        }
    }
}
