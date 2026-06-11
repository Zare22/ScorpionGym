package hr.kotwave.scorpiongym.report.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.report.ReportViewModel
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate

/** R2 — "Prihodi po kategoriji": net cash booked in the period, split by category. */
@Composable
fun RevenueBreakdownSection() {
    val reportViewModel: ReportViewModel = getKoin().get()

    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }

    val report = reportViewModel.revenueBreakdown

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Prihodi po kategoriji", style = MaterialTheme.typography.h6)
        Spacer(Modifier.height(8.dp))

        PeriodPickerRow(
            from = fromDate,
            to = toDate,
            onFromChange = { fromDate = it },
            onToChange = { toDate = it },
            onShow = { reportViewModel.loadRevenueBreakdown(fromDate, toDate) },
        )

        Spacer(Modifier.height(16.dp))

        if (report == null) {
            Text("Odaberite razdoblje i kliknite Prikaži.", style = MaterialTheme.typography.body2)
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Kategorija", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("Iznos", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("Udio", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
            Divider()

            report.rows.forEach { row ->
                val share = if (report.total != 0.0) row.netCollected / report.total * 100 else 0.0
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(row.category.label, modifier = Modifier.weight(2f))
                    Text("%.2f €".format(row.netCollected), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("%.1f %%".format(share), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
                Divider()
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Ukupno", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("%.2f €".format(report.total), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("", modifier = Modifier.weight(1f))
            }
        }
    }
}
