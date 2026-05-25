package hr.kotwave.scorpiongym.membershiprecord

import hr.kotwave.scorpiongym.testutil.createTestDatabase
import hr.kotwave.scorpiongym.testutil.insertMember
import hr.kotwave.scorpiongym.testutil.insertMembership
import hr.kotwave.scorpiongym.testutil.insertMembershipRecord
import hr.kotwave.scorpiongym.testutil.selectPaymentAuditLogForMembershipRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the MembershipRecord payment-audit triggers in schema.sql:
 *   - LogMembershipRecordPaymentInsert (line 162)
 *   - LogMembershipRecordPaymentUpdate (line 180)
 *
 * Insert-trigger fires AFTER INSERT WHEN NEW.isPaid = 1
 * Update-trigger fires AFTER UPDATE OF isPaid WHEN OLD.isPaid != NEW.isPaid
 *
 * Each fires writes a PaymentAuditLog row with the membership's price, the
 * logged-in user id from CurrentSessionUser, and today's date.
 *
 * Note on cases 9-10: the DAO's `updateMembershipRecord` UPDATEs every column,
 * not just `isPaid`. The `LogMembershipRecordPaymentUpdate` trigger is scoped
 * `AFTER UPDATE OF isPaid`, which in SQLite means "fires when the SQL touches
 * the isPaid column", regardless of whether the value actually changed. The
 * second `WHEN OLD.isPaid != NEW.isPaid` guard then filters out no-op flips.
 */
class PaymentAuditLogTriggerTest {

    private lateinit var connection: Connection
    private lateinit var dao: MembershipRecordDaoImpl
    private val membershipPrice = 49.99

    @BeforeEach
    fun setUp() {
        connection = createTestDatabase()
        dao = MembershipRecordDaoImpl(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    // ----- Case 7 ------------------------------------------------------------
    @Test
    fun `inserting a record with isPaid=1 writes an audit row`() {
        val today = LocalDate.now()
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(price = membershipPrice)

        val newId = dao.insertMembershipRecord(
            MembershipRecord(
                id = 0,
                memberId = memberId,
                membershipId = membershipId,
                dateStarted = today,
                dateFinished = today.plusMonths(1),
                isActive = true,
                isPaid = true
            )
        )

        val rows = connection.selectPaymentAuditLogForMembershipRecord(newId)
        assertEquals(1, rows.size, "Exactly one audit row should be written on a paid insert")
        val row = rows[0]
        assertEquals(false, row.isPaidOld, "isPaidOld defaults to 0 on insert")
        assertEquals(true, row.isPaidNew)
        assertEquals(membershipPrice, row.price, 0.001)
        assertEquals(today, row.changedAt)
        assertEquals(1, row.loggedInUserId, "Audit row should record the test session user")
    }

    // ----- Case 8 ------------------------------------------------------------
    @Test
    fun `inserting a record with isPaid=0 writes no audit row`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(price = membershipPrice)

        val newId = dao.insertMembershipRecord(
            MembershipRecord(
                id = 0,
                memberId = memberId,
                membershipId = membershipId,
                dateStarted = LocalDate.now(),
                dateFinished = LocalDate.now().plusMonths(1),
                isActive = true,
                isPaid = false
            )
        )

        val rows = connection.selectPaymentAuditLogForMembershipRecord(newId)
        assertTrue(rows.isEmpty(), "Insert with isPaid=0 must not produce an audit row")
    }

    // ----- Case 9 ------------------------------------------------------------
    @Test
    fun `updating isPaid from 0 to 1 writes an audit row`() {
        val today = LocalDate.now()
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(price = membershipPrice)
        // Seed via raw SQL so the insert-trigger does NOT fire (isPaid=0 from the start).
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            isActive = true,
            isPaid = false
        )

        dao.updateMembershipRecord(
            MembershipRecord(
                id = recordId,
                memberId = memberId,
                membershipId = membershipId,
                dateStarted = today,
                dateFinished = today.plusMonths(1),
                isActive = true,
                isPaid = true
            )
        )

        val rows = connection.selectPaymentAuditLogForMembershipRecord(recordId)
        assertEquals(1, rows.size, "Exactly one audit row on 0->1 flip")
        val row = rows[0]
        assertEquals(false, row.isPaidOld)
        assertEquals(true, row.isPaidNew)
        assertEquals(membershipPrice, row.price, 0.001)
        assertEquals(today, row.changedAt)
        assertEquals(1, row.loggedInUserId)
    }

    // ----- Case 10 -----------------------------------------------------------
    @Test
    fun `updating isPaid from 1 to 0 writes an audit row`() {
        val today = LocalDate.now()
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(price = membershipPrice)
        // Raw-SQL seed with isPaid=true so the insert path triggers an audit row.
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            isActive = true,
            isPaid = true
        )
        // Sanity: insert-trigger wrote one row already.
        assertEquals(1, connection.selectPaymentAuditLogForMembershipRecord(recordId).size)

        dao.updateMembershipRecord(
            MembershipRecord(
                id = recordId,
                memberId = memberId,
                membershipId = membershipId,
                dateStarted = today,
                dateFinished = today.plusMonths(1),
                isActive = true,
                isPaid = false
            )
        )

        val rows = connection.selectPaymentAuditLogForMembershipRecord(recordId)
        assertEquals(2, rows.size, "A second audit row should be added on the 1->0 flip")
        val latest = rows.last()
        assertEquals(true, latest.isPaidOld)
        assertEquals(false, latest.isPaidNew)
        assertEquals(membershipPrice, latest.price, 0.001)
        assertEquals(today, latest.changedAt)
    }

    // ----- Case 11 -----------------------------------------------------------
    @Test
    fun `updating isPaid to the same value writes no audit row`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(price = membershipPrice)
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            isActive = true,
            isPaid = false
        )

        // Update with the SAME isPaid value (false -> false). Trigger's
        // `WHEN OLD.isPaid != NEW.isPaid` guard must reject.
        dao.updateMembershipRecord(
            MembershipRecord(
                id = recordId,
                memberId = memberId,
                membershipId = membershipId,
                dateStarted = LocalDate.now(),
                dateFinished = LocalDate.now().plusMonths(2),
                isActive = true,
                isPaid = false
            )
        )

        val rows = connection.selectPaymentAuditLogForMembershipRecord(recordId)
        assertTrue(rows.isEmpty(), "No audit row should be written when isPaid is unchanged")
    }
}
