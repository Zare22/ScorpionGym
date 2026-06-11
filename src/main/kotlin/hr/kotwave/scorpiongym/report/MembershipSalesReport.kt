package hr.kotwave.scorpiongym.report

/**
 * One line of the "membership sales by type" report (R1), for a chosen period.
 *
 * The two figures use deliberately different bases (see [MembershipSalesReport]):
 *  - [soldCount]    counts MembershipRecords whose dateStarted falls in the period
 *                   (accrual / "prodano"), regardless of whether they were paid.
 *  - [netCollected] is the net cash booked in the period from the payment ledger
 *                   (cash / "naplaćeno"), reversals subtracted.
 */
data class MembershipSalesRow(
    val membershipId: Int,
    val membershipName: String,
    val soldCount: Int,
    val netCollected: Double,
)

/**
 * Result of R1 for a period: one [rows] entry per registered-member membership
 * type, plus a single separate walk-in line (Q2 — walk-in memberships are
 * reported on their own, not merged into the per-type rows).
 *
 * Totals are grand totals that include the walk-in line.
 */
data class MembershipSalesReport(
    val rows: List<MembershipSalesRow>,
    val walkInCount: Int,
    val walkInCollected: Double,
) {
    val totalSold: Int get() = rows.sumOf { it.soldCount } + walkInCount
    val totalCollected: Double get() = rows.sumOf { it.netCollected } + walkInCollected
}
