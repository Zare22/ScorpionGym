package hr.kotwave.scorpiongym.report

/** One month bucket of R3 (revenue over time). [month] is an ISO "YYYY-MM" string. */
data class MonthlyRevenueRow(
    val month: String,
    val netCollected: Double,
)

/** R3 result: net cash booked per calendar month over the period, oldest first. */
data class RevenueOverTimeReport(
    val rows: List<MonthlyRevenueRow>,
) {
    val total: Double get() = rows.sumOf { it.netCollected }
}
