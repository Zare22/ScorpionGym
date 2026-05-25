package hr.kotwave.scorpiongym.membershiprecord

import hr.kotwave.scorpiongym.testutil.createTestDatabase
import hr.kotwave.scorpiongym.testutil.insertMember
import hr.kotwave.scorpiongym.testutil.insertMembership
import hr.kotwave.scorpiongym.testutil.insertMembershipRecord
import hr.kotwave.scorpiongym.testutil.insertTrainingSession
import hr.kotwave.scorpiongym.testutil.selectMembershipRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for `MembershipRecordDaoImpl.refreshMembershipStatuses()` — the Kotlin
 * routine that runs on app startup to:
 *   1. deactivate records whose dateFinished has passed,
 *   2. activate eligible inactive records (subject to per-member uniqueness
 *      and remaining-sessions checks).
 *
 * These tests assert only the `MembershipRecord.isActive` flip. They are
 * deliberately silent on `Member.membershipRecordId` — the production code
 * does not update that field during activation (see spawned investigation
 * task), and these tests should not lock in either side of that ambiguity.
 */
class RefreshMembershipStatusesTest {

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

    // ----- Case 12 -----------------------------------------------------------
    @Test
    fun `expired record is deactivated`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership()
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            dateStarted = LocalDate.now().minusMonths(2),
            dateFinished = LocalDate.now().minusDays(1),
            isActive = true,
            isPaid = true
        )

        dao.refreshMembershipStatuses()

        val record = connection.selectMembershipRecord(recordId)!!
        assertFalse(record.isActive, "Record with dateFinished < today should be deactivated")
    }

    // ----- Case 13 -----------------------------------------------------------
    @Test
    fun `eligible inactive record is activated`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(numberOfTrainingsAvailable = 10)
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            dateStarted = LocalDate.now().minusDays(2),
            dateFinished = LocalDate.now().plusDays(30),
            isActive = false,
            isPaid = true
        )

        dao.refreshMembershipStatuses()

        val record = connection.selectMembershipRecord(recordId)!!
        assertTrue(record.isActive, "Inactive record that has started and not finished should be activated")
    }

    // ----- Case 14 -----------------------------------------------------------
    @Test
    fun `capped record with no remaining sessions is not activated`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(numberOfTrainingsAvailable = 3, isNoLimit = false)
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            dateStarted = LocalDate.now().minusDays(2),
            dateFinished = LocalDate.now().plusDays(30),
            isActive = false,
            isPaid = true
        )
        // 3 sessions exhaust the cap. Inserted via raw SQL: the UpdateMembershipStatus
        // trigger guard (`AND isActive = TRUE`) means an inactive record cannot
        // be touched by the trigger, so the sessions stack up without side effect.
        repeat(3) {
            connection.insertTrainingSession(membershipRecordId = recordId)
        }

        dao.refreshMembershipStatuses()

        val record = connection.selectMembershipRecord(recordId)!!
        assertFalse(
            record.isActive,
            "Capped record with trainings_used >= cap must not be activated by refresh"
        )
    }

    // ----- Case 15 -----------------------------------------------------------
    @Test
    fun `no-limit record with many sessions is activated`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(
            numberOfTrainingsAvailable = Int.MAX_VALUE,
            isNoLimit = true
        )
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            dateStarted = LocalDate.now().minusDays(2),
            dateFinished = LocalDate.now().plusDays(30),
            isActive = false,
            isPaid = true
        )
        repeat(50) {
            connection.insertTrainingSession(membershipRecordId = recordId)
        }

        dao.refreshMembershipStatuses()

        val record = connection.selectMembershipRecord(recordId)!!
        assertTrue(record.isActive, "No-limit record should be activated regardless of training count")
    }

    // ----- Case 16 -----------------------------------------------------------
    @Test
    fun `member with an existing active record has no other record activated`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(numberOfTrainingsAvailable = 10)
        val activeRecordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            dateStarted = LocalDate.now().minusDays(5),
            dateFinished = LocalDate.now().plusDays(25),
            isActive = true,
            isPaid = true
        )
        // A second, otherwise-eligible record for the same member.
        val candidateRecordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            dateStarted = LocalDate.now().minusDays(1),
            dateFinished = LocalDate.now().plusDays(60),
            isActive = false,
            isPaid = true
        )

        dao.refreshMembershipStatuses()

        val activeRecord = connection.selectMembershipRecord(activeRecordId)!!
        val candidateRecord = connection.selectMembershipRecord(candidateRecordId)!!
        assertTrue(activeRecord.isActive, "Existing active record should remain active")
        assertFalse(
            candidateRecord.isActive,
            "A second record must not be activated while the member already has one active"
        )
    }
}
