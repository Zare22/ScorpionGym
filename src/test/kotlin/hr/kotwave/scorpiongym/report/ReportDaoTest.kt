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
}

private fun MembershipSalesReport.row(name: String): MembershipSalesRow =
    rows.first { it.membershipName == name }

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
