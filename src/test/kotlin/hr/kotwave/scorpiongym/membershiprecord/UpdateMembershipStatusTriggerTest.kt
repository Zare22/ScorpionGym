package hr.kotwave.scorpiongym.membershiprecord

import hr.kotwave.scorpiongym.testutil.countTrainingSessions
import hr.kotwave.scorpiongym.testutil.createTestDatabase
import hr.kotwave.scorpiongym.testutil.insertMember
import hr.kotwave.scorpiongym.testutil.insertMembership
import hr.kotwave.scorpiongym.testutil.insertMembershipRecord
import hr.kotwave.scorpiongym.testutil.selectMemberMembershipRecordId
import hr.kotwave.scorpiongym.testutil.selectMembershipRecord
import hr.kotwave.scorpiongym.testutil.setMemberMembershipRecordId
import hr.kotwave.scorpiongym.trainingsession.TrainingSession
import hr.kotwave.scorpiongym.trainingsession.TrainingSessionDaoImpl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the `UpdateMembershipStatus` trigger in schema.sql:290.
 *
 * The trigger fires AFTER INSERT ON TrainingSession and deactivates the parent
 * MembershipRecord (sets isActive = FALSE, dateFinished = today) when the
 * training count for that record meets-or-exceeds the membership's
 * `numberOfTrainingsAvailable`.
 *
 * Chain effect: deactivating the record also fires `SetMembershipRecordIdToNull`,
 * which clears `Member.membershipRecordId` if it pointed at the now-inactive
 * record. Case 1 below asserts that knock-on effect explicitly.
 *
 * Writes go through `TrainingSessionDaoImpl` (the real production code path).
 * Reads use raw-SQL helpers so a buggy DAO read method can't poison the assertions.
 */
class UpdateMembershipStatusTriggerTest {

    private lateinit var connection: Connection
    private lateinit var dao: TrainingSessionDaoImpl

    @BeforeEach
    fun setUp() {
        connection = createTestDatabase()
        dao = TrainingSessionDaoImpl(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    // ----- Case 1 ------------------------------------------------------------
    @Test
    fun `inserting the Nth training on a capped record deactivates it and clears the member pointer`() {
        val today = LocalDate.now()
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(numberOfTrainingsAvailable = 3, isNoLimit = false)
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            isActive = true,
            dateFinished = today.plusDays(30)
        )
        connection.setMemberMembershipRecordId(memberId, recordId)

        // Fire 2 sessions below the cap — record stays active.
        repeat(2) {
            dao.insertTrainingSession(
                TrainingSession(membershipRecordId = recordId, sessionDateTime = LocalDateTime.now())
            )
        }
        val midRecord = connection.selectMembershipRecord(recordId)!!
        assertTrue(midRecord.isActive, "Record should remain active after ${midRecord.dateFinished?.let { "until cap" } ?: ""} 2/3 sessions")

        // The 3rd (== cap) session deactivates the record.
        dao.insertTrainingSession(
            TrainingSession(membershipRecordId = recordId, sessionDateTime = LocalDateTime.now())
        )

        val finalRecord = connection.selectMembershipRecord(recordId)!!
        assertFalse(finalRecord.isActive, "Record should be deactivated after 3rd session on a cap-3 membership")
        assertEquals(today, finalRecord.dateFinished, "dateFinished should be overwritten to today on cap hit")
        assertNull(
            connection.selectMemberMembershipRecordId(memberId),
            "Chain effect: SetMembershipRecordIdToNull should have cleared Member.membershipRecordId"
        )
        assertEquals(3, connection.countTrainingSessions(recordId))
    }

    // ----- Case 2 ------------------------------------------------------------
    @Test
    fun `inserting the N-1 th training on a capped record leaves it active`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(numberOfTrainingsAvailable = 5, isNoLimit = false)
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            isActive = true
        )

        repeat(4) {
            dao.insertTrainingSession(
                TrainingSession(membershipRecordId = recordId, sessionDateTime = LocalDateTime.now())
            )
        }

        val record = connection.selectMembershipRecord(recordId)!!
        assertTrue(record.isActive, "Record should still be active after 4/5 sessions")
        assertEquals(4, connection.countTrainingSessions(recordId))
    }

    // ----- Case 3 ------------------------------------------------------------
    @Test
    fun `no-limit membership never deactivates regardless of session count`() {
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(
            numberOfTrainingsAvailable = Int.MAX_VALUE,
            isNoLimit = true
        )
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            isActive = true
        )

        // Insert a handful of sessions — far below Int.MAX_VALUE, so the trigger's
        // COUNT >= cap predicate can never hold.
        repeat(20) {
            dao.insertTrainingSession(
                TrainingSession(membershipRecordId = recordId, sessionDateTime = LocalDateTime.now())
            )
        }

        val record = connection.selectMembershipRecord(recordId)!!
        assertTrue(record.isActive, "No-limit record should remain active")
        assertEquals(20, connection.countTrainingSessions(recordId))
    }

    // ----- Case 4 ------------------------------------------------------------
    @Test
    fun `inserting a training on an already-inactive record does not mutate it`() {
        val originalDateFinished = LocalDate.now().minusDays(5)
        val memberId = connection.insertMember()
        val membershipId = connection.insertMembership(numberOfTrainingsAvailable = 1, isNoLimit = false)
        val recordId = connection.insertMembershipRecord(
            memberId = memberId,
            membershipId = membershipId,
            isActive = false,
            dateFinished = originalDateFinished
        )

        // Even though cap=1 and we're about to insert a session (which would
        // normally trip the deactivate UPDATE), the trigger's `WHERE isActive = TRUE`
        // guard means it cannot touch a record that's already inactive.
        dao.insertTrainingSession(
            TrainingSession(membershipRecordId = recordId, sessionDateTime = LocalDateTime.now())
        )

        val record = connection.selectMembershipRecord(recordId)!!
        assertFalse(record.isActive, "Record was inactive and should stay inactive")
        assertEquals(
            originalDateFinished,
            record.dateFinished,
            "dateFinished must not be overwritten on an already-inactive record"
        )
    }
}
