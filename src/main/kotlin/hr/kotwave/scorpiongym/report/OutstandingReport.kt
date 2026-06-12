package hr.kotwave.scorpiongym.report

import java.time.LocalDate

/**
 * One unpaid item in R5. [memberName] is null for walk-in (unregistered) items.
 * [amount] is the current list price owed (no ledger snapshot exists — it's unpaid).
 */
data class UnpaidItemRow(
    val memberName: String?,
    val description: String,
    val amount: Double,
    val date: LocalDate,
)

/** R5 result: every currently-unpaid item across memberships, member services, and walk-ins. */
data class OutstandingReport(
    val rows: List<UnpaidItemRow>,
) {
    val total: Double get() = rows.sumOf { it.amount }
    val count: Int get() = rows.size
}
