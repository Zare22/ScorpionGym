package hr.kotwave.scorpiongym.report.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hr.kotwave.scorpiongym.ui.custom.elements.CustomBackIcon

/**
 * Host for all reports (R1–R7). Each report is a self-contained section
 * composable selected via the tab strip; sections read their data from the
 * shared ReportViewModel. New reports plug in by adding a tab title + a branch.
 */
class ReportScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Prodaja članarina", "Prihodi po kategoriji", "Prihodi kroz vrijeme", "Novi članovi", "Dugovanja", "Iskorištenost", "Demografija")

        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomBackIcon(navigator = navigator)
                Text("Izvještaji", style = MaterialTheme.typography.h5)
            }
            Divider(modifier = Modifier.fillMaxWidth())

            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                when (selectedTab) {
                    0 -> MembershipSalesSection()
                    1 -> RevenueBreakdownSection()
                    2 -> RevenueOverTimeSection()
                    3 -> NewMembersSection()
                    4 -> OutstandingSection()
                    5 -> UtilizationSection()
                    6 -> DemographicsSection()
                }
            }
        }
    }
}
