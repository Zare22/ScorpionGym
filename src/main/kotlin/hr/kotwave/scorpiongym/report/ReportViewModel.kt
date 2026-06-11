package hr.kotwave.scorpiongym.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

class ReportViewModel(private val reportDao: ReportDao) {

    /** Last loaded R1 result, or null before the first "Prikaži". */
    var membershipSales by mutableStateOf<MembershipSalesReport?>(null)
        private set

    /** Last loaded R2 result, or null before the first "Prikaži". */
    var revenueBreakdown by mutableStateOf<RevenueBreakdownReport?>(null)
        private set

    /** Last loaded R3 result, or null before the first "Prikaži". */
    var revenueOverTime by mutableStateOf<RevenueOverTimeReport?>(null)
        private set

    fun loadMembershipSales(from: LocalDate?, to: LocalDate?) {
        val report = reportDao.membershipSalesByType(from, to)
        // Show only types with activity in the period (every type is returned by the
        // query, most with zeros). Dropping empties doesn't change the totals.
        val activeRows = report.rows.filter { it.soldCount > 0 || it.netCollected != 0.0 }
        membershipSales = report.copy(rows = activeRows)
    }

    fun loadRevenueBreakdown(from: LocalDate?, to: LocalDate?) {
        revenueBreakdown = reportDao.revenueBreakdown(from, to)
    }

    fun loadRevenueOverTime(from: LocalDate?, to: LocalDate?) {
        revenueOverTime = reportDao.revenueOverTime(from, to)
    }
}
