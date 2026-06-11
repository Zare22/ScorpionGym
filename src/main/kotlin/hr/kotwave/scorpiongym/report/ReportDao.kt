package hr.kotwave.scorpiongym.report

import java.time.LocalDate

interface ReportDao {

    /**
     * R1 — membership sales by type for the (inclusive) period [from]..[to].
     * Either bound may be null for an open-ended range.
     */
    fun membershipSalesByType(from: LocalDate?, to: LocalDate?): MembershipSalesReport
}
