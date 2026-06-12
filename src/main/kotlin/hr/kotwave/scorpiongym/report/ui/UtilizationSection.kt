package hr.kotwave.scorpiongym.report.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
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

private val WEEKDAY_LABELS = listOf("Pon", "Uto", "Sri", "Čet", "Pet", "Sub", "Ned")

/** R6 — "Iskorištenost": training-session counts by weekday, hour, and month. */
@Composable
fun UtilizationSection() {
    val reportViewModel: ReportViewModel = getKoin().get()

    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }

    val report = reportViewModel.utilization

    Column(modifier = Modifier.fillMaxSize()) {
        ReportSectionHeader(
            "Iskorištenost (treninzi)",
            onPrint = report?.let { r ->
                { ReportExporter.exportAndOpen(r.toPrintable("Iskorištenost", formatPeriod(fromDate, toDate))) }
            },
        )
        Spacer(Modifier.height(8.dp))

        PeriodPickerRow(
            from = fromDate,
            to = toDate,
            onFromChange = { fromDate = it },
            onToChange = { toDate = it },
            onShow = { reportViewModel.loadUtilization(fromDate, toDate) },
        )

        Spacer(Modifier.height(16.dp))

        if (report == null) {
            Text("Odaberite razdoblje i kliknite Prikaži.", style = MaterialTheme.typography.body2)
        } else {
            Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(
                    "Ukupno treninga: ${report.total}",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))

                UtilizationTable(
                    title = "Po danu u tjednu",
                    leftHeader = "Dan",
                    rows = report.byWeekday.map { WEEKDAY_LABELS[it.isoDay - 1] to it.count },
                )
                Spacer(Modifier.height(16.dp))
                UtilizationTable(
                    title = "Po satu",
                    leftHeader = "Sat",
                    rows = report.byHour.map { (if (it.hour == 0) "bez vremena" else "%02d:00".format(it.hour)) to it.count },
                )
                Spacer(Modifier.height(16.dp))
                UtilizationTable(
                    title = "Po mjesecu",
                    leftHeader = "Mjesec",
                    rows = report.byMonth.map { formatMonth(it.month) to it.count },
                )
            }
        }
    }
}

@Composable
private fun UtilizationTable(title: String, leftHeader: String, rows: List<Pair<String, Int>>) {
    Text(title, style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(leftHeader, modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
        Text("Treninga", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
    Divider()
    if (rows.isEmpty()) {
        Text(
            "Nema podataka u razdoblju.",
            modifier = Modifier.padding(vertical = 6.dp),
            style = MaterialTheme.typography.body2,
        )
    } else {
        rows.forEach { (label, count) ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(label, modifier = Modifier.weight(2f))
                Text(count.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            Divider()
        }
    }
}
