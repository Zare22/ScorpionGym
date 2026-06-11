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
}
