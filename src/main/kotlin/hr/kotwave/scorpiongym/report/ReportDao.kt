package hr.kotwave.scorpiongym.report

import java.time.LocalDate

interface ReportDao {

    /**
     * R1 — membership sales by type for the (inclusive) period [from]..[to].
     * Either bound may be null for an open-ended range.
     */
    fun membershipSalesByType(from: LocalDate?, to: LocalDate?): MembershipSalesReport

    /**
     * R2 — net cash collected in the (inclusive) period [from]..[to], split into
     * the four revenue categories. Either bound may be null for an open range.
     */
    fun revenueBreakdown(from: LocalDate?, to: LocalDate?): RevenueBreakdownReport

    /**
     * R3 — net cash collected per calendar month over the (inclusive) period
     * [from]..[to], oldest first. Either bound may be null for an open range.
     */
    fun revenueOverTime(from: LocalDate?, to: LocalDate?): RevenueOverTimeReport

    /**
     * R4 — count of members who signed up per calendar month over the (inclusive)
     * period [from]..[to], oldest first. Either bound may be null for an open range.
     */
    fun newMembersByMonth(from: LocalDate?, to: LocalDate?): NewMembersReport

    /**
     * R5 — every currently-unpaid item (membership, member service, or walk-in),
     * newest first, valued at current list price. The (inclusive) [from]..[to]
     * filters on each item's date; null bounds list all outstanding debts.
     */
    fun outstanding(from: LocalDate?, to: LocalDate?): OutstandingReport

    /**
     * R6 — training-session utilization over the (inclusive) period [from]..[to]:
     * counts by weekday, hour of day, and calendar month. Null bounds = all time.
     */
    fun utilization(from: LocalDate?, to: LocalDate?): UtilizationReport

    /**
     * R7 — member base split by gender and age band. The (inclusive) [from]..[to]
     * filters on signup date (`signedUpDate`); null bounds include all members.
     */
    fun demographics(from: LocalDate?, to: LocalDate?): DemographicsReport
}
