package hr.kotwave.scorpiongym.report

import hr.kotwave.scorpiongym.testutil.createTestDatabase
import hr.kotwave.scorpiongym.testutil.insertMember
import hr.kotwave.scorpiongym.testutil.insertMembership
import hr.kotwave.scorpiongym.testutil.insertMembershipRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

/**
 * Tests for R1 (ReportDaoImpl.membershipSalesByType). Covers the two deliberately
 * different bases:
 *   - "Prodano"   = COUNT of MembershipRecord by dateStarted (incl. unpaid).
 *   - "Naplaćeno" = net ledger cash by changedAt: SUM((isPaidNew-isPaidOld)*price).
 * plus the separate walk-in line (Q2) and open-ended ranges.
 *
 * The payment-audit triggers stamp PaymentAuditLog.changedAt = today, so tests
 * that exercise the collected-date filter backdate those rows via raw SQL.
 */
class ReportDaoTest {

    private lateinit var connection: Connection
    private lateinit var dao: ReportDaoImpl

    @BeforeEach
    fun setUp() {
        connection = createTestDatabase()
        dao = ReportDaoImpl(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Test
    fun `prodano counts records by dateStarted, grouped by type, including unpaid`() {
        val memberId = connection.insertMember()
        val typeA = connection.insertMembership(name = "A", price = 30.0)
        val typeB = connection.insertMembership(name = "B", price = 40.0)

        // 2x A in January, 1x A in February (out of range), 1x B in January — all unpaid.
        connection.insertMembershipRecord(memberId, typeA, dateStarted = LocalDate.of(2025, 1, 5), isPaid = false)
        connection.insertMembershipRecord(memberId, typeA, dateStarted = LocalDate.of(2025, 1, 20), isPaid = false)
        connection.insertMembershipRecord(memberId, typeA, dateStarted = LocalDate.of(2025, 2, 3), isPaid = false)
        connection.insertMembershipRecord(memberId, typeB, dateStarted = LocalDate.of(2025, 1, 10), isPaid = false)

        val report = dao.membershipSalesByType(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31))

        assertEquals(2, report.row("A").soldCount, "2 type-A records started in January")
        assertEquals(1, report.row("B").soldCount, "1 type-B record started in January")
        assertEquals(0.0, report.row("A").netCollected, 0.001, "unpaid records collect nothing")
    }

    @Test
    fun `naplaceno sums net cash and subtracts reversals`() {
        val memberId = connection.insertMember()
        val typeA = connection.insertMembership(name = "A", price = 30.0)
        val today = LocalDate.now()

        // rec1: paid (+30).
        connection.insertMembershipRecord(memberId, typeA, dateStarted = today, isPaid = true)
        // rec2: paid (+30) then reversed (-30) => net 0.
        val rec2 = connection.insertMembershipRecord(memberId, typeA, dateStarted = today, isPaid = true)
        connection.setIsPaid(rec2, false)

        val report = dao.membershipSalesByType(today, today)

        assertEquals(2, report.row("A").soldCount)
        assertEquals(30.0, report.row("A").netCollected, 0.001, "30 paid + (30 paid then reversed) = 30 net")
    }

    @Test
    fun `naplaceno respects the changedAt range, independent of dateStarted`() {
        val memberId = connection.insertMember()
        val typeA = connection.insertMembership(name = "A", price = 30.0)
        val today = LocalDate.now()

        // Record started today and paid today (trigger stamps changedAt = today)...
        val rec = connection.insertMembershipRecord(memberId, typeA, dateStarted = today, isPaid = true)
        // ...then backdate the payment to 2020 to simulate cash booked in a past period.
        connection.backdatePayments(rec, LocalDate.of(2020, 6, 1))

        // The payment's period: collected shows, but sold = 0 (record started today).
        val pastReport = dao.membershipSalesByType(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31))
        assertEquals(0, pastReport.row("A").soldCount, "record started today, not in 2020")
        assertEquals(30.0, pastReport.row("A").netCollected, 0.001, "payment booked in 2020")

        // Today: sold shows, but collected = 0 (payment is dated 2020).
        val todayReport = dao.membershipSalesByType(today, today)
        assertEquals(1, todayReport.row("A").soldCount)
        assertEquals(0.0, todayReport.row("A").netCollected, 0.001, "payment is out of range")
    }

    @Test
    fun `walk-in memberships are counted on a separate line, not in per-type rows`() {
        val typeA = connection.insertMembership(name = "A", price = 30.0)
        val today = LocalDate.now()

        connection.insertWalkInMembership(typeA, LocalDateTime.now(), isPaid = true)

        val report = dao.membershipSalesByType(today, today)

        assertEquals(1, report.walkInCount)
        assertEquals(30.0, report.walkInCollected, 0.001)
        // The per-type row for A must be untouched by the walk-in purchase.
        assertEquals(0, report.row("A").soldCount)
        assertEquals(0.0, report.row("A").netCollected, 0.001)
    }

    @Test
    fun `null bounds include all periods`() {
        val memberId = connection.insertMember()
        val typeA = connection.insertMembership(name = "A", price = 30.0)

        connection.insertMembershipRecord(memberId, typeA, dateStarted = LocalDate.of(2019, 1, 1), isPaid = false)
        connection.insertMembershipRecord(memberId, typeA, dateStarted = LocalDate.of(2025, 1, 1), isPaid = true)

        val report = dao.membershipSalesByType(null, null)

        assertEquals(2, report.row("A").soldCount, "both records counted regardless of date")
        // The paid record's payment is stamped today by the trigger; an open range includes it.
        assertEquals(30.0, report.row("A").netCollected, 0.001)
    }

    // ----- R2: revenue breakdown ---------------------------------------------

    @Test
    fun `revenue breakdown splits net cash into the four categories`() {
        val memberId = connection.insertMember()
        val typeA = connection.insertMembership(name = "A", price = 30.0)
        val svc = connection.insertOtherService(name = "Solarij", price = 5.0)
        val today = LocalDate.now()

        connection.insertMembershipRecord(memberId, typeA, dateStarted = today, isPaid = true) // +30 member membership
        connection.insertMemberOtherService(memberId, svc, isPaid = true)                      // +5  member service
        connection.insertWalkInMembership(typeA, LocalDateTime.now(), isPaid = true)           // +30 walk-in membership
        connection.insertWalkInOtherService(svc, LocalDateTime.now(), isPaid = true)           // +5  walk-in service

        val report = dao.revenueBreakdown(today, today)

        assertEquals(30.0, report.net(RevenueCategory.MEMBER_MEMBERSHIP), 0.001)
        assertEquals(5.0, report.net(RevenueCategory.MEMBER_SERVICE), 0.001)
        assertEquals(30.0, report.net(RevenueCategory.WALKIN_MEMBERSHIP), 0.001)
        assertEquals(5.0, report.net(RevenueCategory.WALKIN_SERVICE), 0.001)
        assertEquals(70.0, report.total, 0.001)
    }

    @Test
    fun `revenue breakdown always returns all four categories, even when empty`() {
        val report = dao.revenueBreakdown(null, null)

        assertEquals(4, report.rows.size)
        assertEquals(0.0, report.total, 0.001)
    }

    @Test
    fun `revenue breakdown respects the changedAt range`() {
        val memberId = connection.insertMember()
        val typeA = connection.insertMembership(name = "A", price = 30.0)
        val rec = connection.insertMembershipRecord(memberId, typeA, isPaid = true)
        connection.backdatePayments(rec, LocalDate.of(2020, 6, 1))

        val today = LocalDate.now()
        assertEquals(
            0.0,
            dao.revenueBreakdown(today, today).net(RevenueCategory.MEMBER_MEMBERSHIP),
            0.001,
            "payment is dated 2020, out of today's range",
        )
        assertEquals(
            30.0,
            dao.revenueBreakdown(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31))
                .net(RevenueCategory.MEMBER_MEMBERSHIP),
            0.001,
        )
    }

    // ----- R3: revenue over time ---------------------------------------------

    @Test
    fun `revenue over time groups net cash by month, oldest first`() {
        val memberId = connection.insertMember()
        val typeA = connection.insertMembership(name = "A", price = 30.0)

        // Three paid records; backdate their ledger rows into specific months.
        val r1 = connection.insertMembershipRecord(memberId, typeA, isPaid = true)
        connection.backdatePayments(r1, LocalDate.of(2025, 1, 15))
        val r2 = connection.insertMembershipRecord(memberId, typeA, isPaid = true)
        connection.backdatePayments(r2, LocalDate.of(2025, 3, 2))
        val r3 = connection.insertMembershipRecord(memberId, typeA, isPaid = true)
        connection.backdatePayments(r3, LocalDate.of(2025, 1, 20))

        val report = dao.revenueOverTime(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31))

        assertEquals(2, report.rows.size, "two distinct months")
        assertEquals("2025-01", report.rows[0].month, "oldest first")
        assertEquals(60.0, report.rows[0].netCollected, 0.001, "two payments in January")
        assertEquals("2025-03", report.rows[1].month)
        assertEquals(30.0, report.rows[1].netCollected, 0.001)
        assertEquals(90.0, report.total, 0.001)
    }

    @Test
    fun `revenue over time respects the period bounds`() {
        val memberId = connection.insertMember()
        val typeA = connection.insertMembership(name = "A", price = 30.0)

        val before = connection.insertMembershipRecord(memberId, typeA, isPaid = true)
        connection.backdatePayments(before, LocalDate.of(2024, 12, 31))
        val inside = connection.insertMembershipRecord(memberId, typeA, isPaid = true)
        connection.backdatePayments(inside, LocalDate.of(2025, 1, 10))

        val report = dao.revenueOverTime(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31))

        assertEquals(1, report.rows.size)
        assertEquals("2025-01", report.rows[0].month)
        assertEquals(30.0, report.total, 0.001)
    }
}

private fun MembershipSalesReport.row(name: String): MembershipSalesRow =
    rows.first { it.membershipName == name }

private fun RevenueBreakdownReport.net(category: RevenueCategory): Double =
    rows.first { it.category == category }.netCollected

private fun Connection.insertOtherService(name: String, price: Double): Int {
    prepareStatement("INSERT INTO OtherService (name, price) VALUES (?, ?) RETURNING id").use {
        it.setString(1, name)
        it.setDouble(2, price)
        val rs = it.executeQuery()
        return if (rs.next()) rs.getInt(1) else error("OtherService insert returned no id")
    }
}

/** Member buys an other service; fires LogOtherServicePaymentInsert when paid. */
private fun Connection.insertMemberOtherService(
    memberId: Int,
    otherServiceId: Int,
    dateOfService: LocalDateTime = LocalDateTime.now(),
    isPaid: Boolean,
) {
    prepareStatement(
        "INSERT INTO MemberOtherService (dateOfService, memberId, otherServiceId, isPaid) VALUES (?, ?, ?, ?)"
    ).use {
        it.setString(1, dateOfService.toString())
        it.setInt(2, memberId)
        it.setInt(3, otherServiceId)
        it.setBoolean(4, isPaid)
        it.executeUpdate()
    }
}

/** Walk-in (unregistered) other-service purchase; fires LogUnregisteredServicePaymentInsert when paid. */
private fun Connection.insertWalkInOtherService(otherServiceId: Int, dateOfService: LocalDateTime, isPaid: Boolean) {
    prepareStatement("INSERT INTO UnregisteredService (dateOfService, otherServiceId, isPaid) VALUES (?, ?, ?)").use {
        it.setString(1, dateOfService.toString())
        it.setInt(2, otherServiceId)
        it.setBoolean(3, isPaid)
        it.executeUpdate()
    }
}

/** Raw-SQL flip of isPaid; fires the LogMembershipRecordPayment* triggers. */
private fun Connection.setIsPaid(recordId: Int, paid: Boolean) {
    prepareStatement("UPDATE MembershipRecord SET isPaid = ? WHERE id = ?").use {
        it.setBoolean(1, paid)
        it.setInt(2, recordId)
        it.executeUpdate()
    }
}

/** Moves a record's ledger rows to a past date (triggers always stamp today). */
private fun Connection.backdatePayments(membershipRecordId: Int, date: LocalDate) {
    prepareStatement("UPDATE PaymentAuditLog SET changedAt = ? WHERE membershipRecordId = ?").use {
        it.setString(1, date.toString())
        it.setInt(2, membershipRecordId)
        it.executeUpdate()
    }
}

/** Walk-in (unregistered) membership purchase; fires LogUnregisteredServicePaymentInsert when paid. */
private fun Connection.insertWalkInMembership(membershipId: Int, dateOfService: LocalDateTime, isPaid: Boolean) {
    prepareStatement("INSERT INTO UnregisteredService (dateOfService, membershipId, isPaid) VALUES (?, ?, ?)").use {
        it.setString(1, dateOfService.toString())
        it.setInt(2, membershipId)
        it.setBoolean(3, isPaid)
        it.executeUpdate()
    }
}
