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
import hr.kotwave.scorpiongym.report.DemographicRow
import hr.kotwave.scorpiongym.report.ReportViewModel
import hr.kotwave.scorpiongym.report.print.ReportExporter
import hr.kotwave.scorpiongym.report.print.formatPeriod
import hr.kotwave.scorpiongym.report.print.toPrintable
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate

/** R7 — "Demografija": member base split by gender and by age band. */
@Composable
fun DemographicsSection() {
    val reportViewModel: ReportViewModel = getKoin().get()

    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }

    val report = reportViewModel.demographics

    Column(modifier = Modifier.fillMaxSize()) {
        ReportSectionHeader(
            "Demografija",
            onPrint = report?.let { r ->
                { ReportExporter.exportAndOpen(r.toPrintable("Demografija", formatPeriod(fromDate, toDate))) }
            },
        )
        Spacer(Modifier.height(8.dp))

        PeriodPickerRow(
            from = fromDate,
            to = toDate,
            onFromChange = { fromDate = it },
            onToChange = { toDate = it },
            onShow = { reportViewModel.loadDemographics(fromDate, toDate) },
        )

        Spacer(Modifier.height(16.dp))

        if (report == null) {
            Text(
                "Ostavite datume prazne za sve članove, ili filtrirajte po datumu upisa. Kliknite Prikaži.",
                style = MaterialTheme.typography.body2,
            )
        } else {
            Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(
                    "Ukupno članova: ${report.total}",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))

                DemographicTable("Po spolu", report.byGender, report.total)
                Spacer(Modifier.height(16.dp))
                DemographicTable("Po dobi", report.byAgeBand, report.total)
            }
        }
    }
}

@Composable
private fun DemographicTable(title: String, rows: List<DemographicRow>, total: Int) {
    Text(title, style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("Kategorija", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
        Text("Broj", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text("Udio", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
    Divider()
    if (rows.isEmpty()) {
        Text(
            "Nema podataka u razdoblju.",
            modifier = Modifier.padding(vertical = 6.dp),
            style = MaterialTheme.typography.body2,
        )
    } else {
        rows.forEach { row ->
            val share = if (total != 0) row.count.toDouble() / total * 100 else 0.0
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(row.label, modifier = Modifier.weight(2f))
                Text(row.count.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("%.1f %%".format(share), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            Divider()
        }
    }
}
