package hr.kotwave.scorpiongym.report

/**
 * Revenue categories for R2. Names are used verbatim as the SQL category codes
 * (see ReportDaoImpl.REVENUE_BREAKDOWN_QUERY); [label] is the Croatian UI text.
 * Walk-in (unregistered) revenue is kept separate from member revenue (Q2).
 */
enum class RevenueCategory(val label: String) {
    MEMBER_MEMBERSHIP("Članarine (članovi)"),
    MEMBER_SERVICE("Ostale usluge (članovi)"),
    WALKIN_MEMBERSHIP("Neregistrirane članarine"),
    WALKIN_SERVICE("Neregistrirane usluge"),
}

data class RevenueCategoryRow(
    val category: RevenueCategory,
    val netCollected: Double,
)

/** R2 result: net cash booked in the period, split across the four categories. */
data class RevenueBreakdownReport(
    val rows: List<RevenueCategoryRow>,
) {
    val total: Double get() = rows.sumOf { it.netCollected }
}
