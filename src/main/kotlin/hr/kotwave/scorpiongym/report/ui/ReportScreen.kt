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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hr.kotwave.scorpiongym.report.ReportViewModel
import hr.kotwave.scorpiongym.ui.custom.elements.CustomBackIcon
import hr.kotwave.scorpiongym.ui.custom.elements.DatePickerField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate

class ReportScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val reportViewModel: ReportViewModel = getKoin().get()

        var fromDate by remember { mutableStateOf<LocalDate?>(null) }
        var toDate by remember { mutableStateOf<LocalDate?>(null) }

        val report = reportViewModel.membershipSales
        val listState = rememberLazyListState()

        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomBackIcon(navigator = navigator)
                Text("Izvještaji", style = MaterialTheme.typography.h5)
            }
            Divider(modifier = Modifier.fillMaxWidth())

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Prodaja članarina po tipu", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                        text = "Ovaj mjesec",
                        onClick = {
                            val now = LocalDate.now()
                            fromDate = now.withDayOfMonth(1)
                            toDate = now.withDayOfMonth(now.lengthOfMonth())
                            reportViewModel.loadMembershipSales(fromDate, toDate)
                        },
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    HoverableButton(
                        text = "Prikaži",
                        onClick = { reportViewModel.loadMembershipSales(fromDate, toDate) },
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (report == null) {
                    Text(
                        "Odaberite razdoblje i kliknite Prikaži.",
                        style = MaterialTheme.typography.body2,
                    )
                } else {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("Tip članarine", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                        Text("Prodano", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        Text("Naplaćeno", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    }
                    Divider()

                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            if (report.rows.isEmpty()) {
                                item {
                                    Text(
                                        "Nema prodaje članarina u odabranom razdoblju.",
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        style = MaterialTheme.typography.body2,
                                    )
                                }
                            }
                            items(report.rows) { row ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Text(row.membershipName, modifier = Modifier.weight(2f))
                                    Text(row.soldCount.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
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

                    // Walk-in memberships reported on their own line (Q2).
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text("Neregistrirane (walk-in) članarine", modifier = Modifier.weight(2f))
                        Text(report.walkInCount.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("%.2f €".format(report.walkInCollected), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    Divider(modifier = Modifier.padding(top = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("Ukupno", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                        Text(report.totalSold.toString(), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        Text("%.2f €".format(report.totalCollected), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}
