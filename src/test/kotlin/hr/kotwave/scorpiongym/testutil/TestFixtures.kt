package hr.kotwave.scorpiongym.testutil

import java.sql.Connection
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Per-test data builders. All inserts go via raw SQL (NOT through DAOs) so the
 * test setup itself never depends on the code under test. DAOs are exercised
 * later inside the actual test methods.
 *
 * Defaults are minimal: the baseline seed from TestDatabase already provides
 * the FK targets these helpers need (TypeOfOrganization=1, Organization=1,
 * Status=1, AppUser=1).
 */

fun Connection.insertMember(
    name: String = "Pero",
    surname: String = "Perić",
    organizationId: Int = 1,
    statusId: Int = 1,
    signedUpDate: LocalDate = LocalDate.now(),
    membershipRecordId: Int? = null
): Int {
    val sql = """
        INSERT INTO Member (name, surname, signedUpDate, organizationId, statusId, membershipRecordId)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING id
    """
    prepareStatement(sql).use { statement ->
        statement.setString(1, name)
        statement.setString(2, surname)
        statement.setString(3, signedUpDate.toString())
        statement.setInt(4, organizationId)
        statement.setInt(5, statusId)
        if (membershipRecordId != null) statement.setInt(6, membershipRecordId)
        else statement.setNull(6, java.sql.Types.INTEGER)
        val resultSet = statement.executeQuery()
        return if (resultSet.next()) resultSet.getInt(1)
        else throw SQLException("Member insert returned no id")
    }
}

fun Connection.insertMembership(
    name: String = "TestČlanarina",
    price: Double = 30.0,
    duration: Long = 1,
    numberOfTrainingsAvailable: Int = 8,
    isNoLimit: Boolean = false
): Int {
    val sql = """
        INSERT INTO Membership (name, price, duration, numberOfTrainingsAvailable, isNoLimit)
        VALUES (?, ?, ?, ?, ?)
        RETURNING id
    """
    prepareStatement(sql).use { statement ->
        statement.setString(1, name)
        statement.setDouble(2, price)
        statement.setLong(3, duration)
        statement.setInt(4, numberOfTrainingsAvailable)
        statement.setBoolean(5, isNoLimit)
        val resultSet = statement.executeQuery()
        return if (resultSet.next()) resultSet.getInt(1)
        else throw SQLException("Membership insert returned no id")
    }
}

fun Connection.insertMembershipRecord(
    memberId: Int,
    membershipId: Int,
    dateStarted: LocalDate = LocalDate.now(),
    dateFinished: LocalDate? = LocalDate.now().plusMonths(1).minusDays(1),
    isActive: Boolean = true,
    isPaid: Boolean = false
): Int {
    val sql = """
        INSERT INTO MembershipRecord (memberId, membershipId, dateStarted, dateFinished, isActive, isPaid)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING id
    """
    prepareStatement(sql).use { statement ->
        statement.setInt(1, memberId)
        statement.setInt(2, membershipId)
        statement.setString(3, dateStarted.toString())
        if (dateFinished != null) statement.setString(4, dateFinished.toString())
        else statement.setNull(4, java.sql.Types.VARCHAR)
        statement.setBoolean(5, isActive)
        statement.setBoolean(6, isPaid)
        val resultSet = statement.executeQuery()
        return if (resultSet.next()) resultSet.getInt(1)
        else throw SQLException("MembershipRecord insert returned no id")
    }
}

fun Connection.insertTrainingSession(
    membershipRecordId: Int,
    sessionDateTime: LocalDateTime = LocalDateTime.now()
): Int {
    val sql = """
        INSERT INTO TrainingSession (membershipRecordId, sessionDateTime)
        VALUES (?, ?)
        RETURNING id
    """
    prepareStatement(sql).use { statement ->
        statement.setInt(1, membershipRecordId)
        statement.setString(2, sessionDateTime.toString())
        val resultSet = statement.executeQuery()
        return if (resultSet.next()) resultSet.getInt(1)
        else throw SQLException("TrainingSession insert returned no id")
    }
}

/**
 * Sets Member.membershipRecordId directly. Useful for cases where a test needs
 * the Member pointer to start in a specific state (e.g. SetMembershipRecordIdToNull
 * trigger assertions).
 */
fun Connection.setMemberMembershipRecordId(memberId: Int, membershipRecordId: Int?) {
    prepareStatement("UPDATE Member SET membershipRecordId = ? WHERE id = ?").use { statement ->
        if (membershipRecordId != null) statement.setInt(1, membershipRecordId)
        else statement.setNull(1, java.sql.Types.INTEGER)
        statement.setInt(2, memberId)
        statement.executeUpdate()
    }
}

// ----------------------------------------------------------------------------
// Read helpers (raw SQL, NOT through DAOs)
// ----------------------------------------------------------------------------

data class MembershipRecordRow(
    val id: Int,
    val memberId: Int,
    val membershipId: Int,
    val dateStarted: LocalDate,
    val dateFinished: LocalDate?,
    val isActive: Boolean,
    val isPaid: Boolean
)

fun Connection.selectMembershipRecord(id: Int): MembershipRecordRow? {
    prepareStatement("SELECT * FROM MembershipRecord WHERE id = ?").use { statement ->
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()
        if (!resultSet.next()) return null
        return MembershipRecordRow(
            id = resultSet.getInt("id"),
            memberId = resultSet.getInt("memberId"),
            membershipId = resultSet.getInt("membershipId"),
            dateStarted = LocalDate.parse(resultSet.getString("dateStarted")),
            dateFinished = resultSet.getString("dateFinished")?.let { LocalDate.parse(it) },
            isActive = resultSet.getBoolean("isActive"),
            isPaid = resultSet.getBoolean("isPaid")
        )
    }
}

fun Connection.selectMemberMembershipRecordId(memberId: Int): Int? {
    prepareStatement("SELECT membershipRecordId FROM Member WHERE id = ?").use { statement ->
        statement.setInt(1, memberId)
        val resultSet = statement.executeQuery()
        if (!resultSet.next()) return null
        val value = resultSet.getInt("membershipRecordId")
        return if (resultSet.wasNull()) null else value
    }
}

data class PaymentAuditLogRow(
    val id: Int,
    val membershipRecordId: Int?,
    val memberOtherServiceId: Int?,
    val unregisteredServiceId: Int?,
    val isPaidOld: Boolean,
    val isPaidNew: Boolean,
    val price: Double,
    val changedAt: LocalDate,
    val loggedInUserId: Int
)

fun Connection.selectPaymentAuditLogForMembershipRecord(membershipRecordId: Int): List<PaymentAuditLogRow> {
    val rows = mutableListOf<PaymentAuditLogRow>()
    prepareStatement(
        "SELECT * FROM PaymentAuditLog WHERE membershipRecordId = ? ORDER BY id"
    ).use { statement ->
        statement.setInt(1, membershipRecordId)
        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
            rows.add(
                PaymentAuditLogRow(
                    id = resultSet.getInt("id"),
                    membershipRecordId = resultSet.getInt("membershipRecordId")
                        .takeUnless { resultSet.wasNull() },
                    memberOtherServiceId = resultSet.getInt("memberOtherServiceId")
                        .takeUnless { resultSet.wasNull() },
                    unregisteredServiceId = resultSet.getInt("unregisteredServiceId")
                        .takeUnless { resultSet.wasNull() },
                    isPaidOld = resultSet.getBoolean("isPaidOld"),
                    isPaidNew = resultSet.getBoolean("isPaidNew"),
                    price = resultSet.getDouble("price"),
                    changedAt = LocalDate.parse(resultSet.getString("changedAt")),
                    loggedInUserId = resultSet.getInt("loggedInUserId")
                )
            )
        }
    }
    return rows
}

fun Connection.countTrainingSessions(membershipRecordId: Int): Int {
    prepareStatement(
        "SELECT COUNT(*) FROM TrainingSession WHERE membershipRecordId = ?"
    ).use { statement ->
        statement.setInt(1, membershipRecordId)
        val resultSet = statement.executeQuery()
        return if (resultSet.next()) resultSet.getInt(1) else 0
    }
}
