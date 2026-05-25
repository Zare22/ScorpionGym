package hr.kotwave.scorpiongym.membershiprecord

import hr.kotwave.scorpiongym.testutil.createTestDatabase
import hr.kotwave.scorpiongym.testutil.insertMember
import hr.kotwave.scorpiongym.testutil.insertMembership
import hr.kotwave.scorpiongym.testutil.insertMembershipRecord
import hr.kotwave.scorpiongym.testutil.selectMemberMembershipRecordId
import hr.kotwave.scorpiongym.testutil.setMemberMembershipRecordId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the `SetMembershipRecordIdToNull` trigger in schema.sql:277.
 *
 * Trigger: AFTER UPDATE ON MembershipRecord WHEN NEW.isActive = FALSE
 *   -> UPDATE Member SET membershipRecordId = NULL WHERE membershipRecordId = NEW.id
 *
 * Effect: when a MembershipRecord goes inactive, any Member still pointing at it
 * has their `membershipRecordId` cleared. Updates that leave the record active
 * must NOT clear the pointer.
 */
class SetMembershipRecordIdToNullTriggerTest {

    private lateinit var connection: Connection
    private lateinit var dao: MembershipRecordDaoImpl

    @BeforeEach
    fun setUp() {
        connection = createTestDatabase()
        dao = MembershipRecordDaoImpl(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    // ----- Case 5 ------------------------------------------------------------
    @Test
    fun `deactivating a record clears the member's membershipRecordId pointer`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership()
        val originalDateStarted = LocalDate.now().minusDays(10)
        val originalDateFinished = LocalDate.now().plusDays(20)
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            dateStarted = originalDateStarted,
            dateFinished = originalDateFinished,
            isActive = true,
            isPaid = true
        )
        connection.setMemberMembershipRecordId(memberId, recordId)

        // Sanity: the Member starts out pointing at this record.
        assertEquals(recordId, connection.selectMemberMembershipRecordId(memberId))

        // Flip isActive: true -> false via the DAO. Trigger fires.
        dao.updateMembershipRecord(
            MembershipRecord(
                id = recordId,
                memberId = memberId,
                membershipId = membershipId,
                dateStarted = originalDateStarted,
                dateFinished = originalDateFinished,
                isActive = false,
                isPaid = true
            )
        )

        assertNull(
            connection.selectMemberMembershipRecordId(memberId),
            "Trigger should have cleared Member.membershipRecordId when the record went inactive"
        )
    }

    // ----- Case 6 ------------------------------------------------------------
    @Test
    fun `updating a record without deactivating it leaves the member pointer intact`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership()
        val originalDateStarted = LocalDate.now().minusDays(10)
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            dateStarted = originalDateStarted,
            dateFinished = LocalDate.now().plusDays(20),
            isActive = true,
            isPaid = false
        )
        connection.setMemberMembershipRecordId(memberId, recordId)

        // Same record, isActive still true, but with a different dateFinished and
        // isPaid flipped. Trigger guard (`WHEN NEW.isActive = FALSE`) must reject.
        dao.updateMembershipRecord(
            MembershipRecord(
                id = recordId,
                memberId = memberId,
                membershipId = membershipId,
                dateStarted = originalDateStarted,
                dateFinished = LocalDate.now().plusDays(40),
                isActive = true,
                isPaid = true
            )
        )

        assertEquals(
            recordId,
            connection.selectMemberMembershipRecordId(memberId),
            "Member.membershipRecordId must stay pointed at the record while it remains active"
        )
    }
}
