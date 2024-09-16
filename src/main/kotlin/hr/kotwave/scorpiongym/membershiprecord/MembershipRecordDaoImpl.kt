package hr.kotwave.scorpiongym.membershiprecord

import hr.kotwave.scorpiongym.util.PreferencesHelper
import java.sql.Connection
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MembershipRecordDaoImpl(private val dbConnection: Connection) : MembershipRecordDao {

    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private val querySelectInactive = "SELECT id FROM MembershipRecord WHERE dateFinished < ? AND isActive = 1"
    private val queryDeactivateMembership = "UPDATE MembershipRecord SET isActive = 0 WHERE id = ?"

    private val querySelectToActivate = """
        WITH minMembershipRecords AS (
            SELECT MIN(mr.id) AS minId, mr.memberId
            FROM MembershipRecord mr
            WHERE mr.dateStarted <= ? 
            AND (mr.dateFinished IS NULL OR mr.dateFinished > ?)
            AND mr.isActive = 0
            AND mr.memberId NOT IN (SELECT memberId FROM MembershipRecord WHERE isActive = 1)
            GROUP BY mr.memberId
        )
        SELECT mr.id, mr.membershipId, m.isNoLimit, m.numberOfTrainingsAvailable
        FROM minMembershipRecords minMr
        JOIN MembershipRecord mr ON mr.id = minMr.minId
        JOIN Membership m ON mr.membershipId = m.id
    """
    private val queryActivateMembership = "UPDATE MembershipRecord SET isActive = 1 WHERE id = ?"

    override fun getAllMembershipRecords(): List<MembershipRecord> {
        val records = mutableListOf<MembershipRecord>()
        val query = "SELECT * FROM MembershipRecord"

        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val record = MembershipRecord(
                    id = resultSet.getInt("id"),
                    memberId = resultSet.getInt("memberId"),
                    membershipId = resultSet.getInt("membershipId"),
                    dateStarted = LocalDate.parse(resultSet.getString("dateStarted")),
                    dateFinished = LocalDate.parse(resultSet.getString("dateFinished")),
                    isActive = resultSet.getBoolean("isActive"),
                    isPaid = resultSet.getBoolean("isPaid")
                )
                records.add(record)
            }
        }

        return records
    }

    override fun getMembershipRecordById(id: Int): MembershipRecord? {
        val query = "SELECT * FROM MembershipRecord WHERE id = ?"
        var record: MembershipRecord? = null

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                record = MembershipRecord(
                    id = resultSet.getInt("id"),
                    memberId = resultSet.getInt("memberId"),
                    membershipId = resultSet.getInt("membershipId"),
                    dateStarted = LocalDate.parse(resultSet.getString("dateStarted")),
                    dateFinished = LocalDate.parse(resultSet.getString("dateFinished")),
                    isActive = resultSet.getBoolean("isActive"),
                    isPaid = resultSet.getBoolean("isPaid")
                )
            }
        }

        return record
    }

    override fun insertMembershipRecord(record: MembershipRecord): Int {
        val query = """
            INSERT INTO MembershipRecord (memberId, membershipId, dateStarted, dateFinished, isActive, isPaid)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
        """
        var insertedId: Int
        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, record.memberId)
            statement.setInt(2, record.membershipId)
            statement.setString(3, record.dateStarted.toString())
            statement.setString(4, record.dateFinished.toString())
            statement.setBoolean(5, record.isActive)
            statement.setBoolean(6, record.isPaid)

            val resultSet = statement.executeQuery()
            insertedId = resultSet.takeIf { it.next() }?.getInt(1) ?: throw SQLException("ID članarine nije kreiran!")
        }
        logActionOnMembershipRecord(record.memberId, record.membershipId, "Kreirana je članarina")

        return insertedId
    }

    override fun updateMembershipRecord(record: MembershipRecord) {
        val query = """
            UPDATE MembershipRecord SET memberId = ?, membershipId = ?, dateStarted = ?, dateFinished = ?, isActive = ?, isPaid = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, record.memberId)
            statement.setInt(2, record.membershipId)
            statement.setString(3, record.dateStarted.toString())
            statement.setString(4, record.dateFinished.toString())
            statement.setBoolean(5, record.isActive)
            statement.setBoolean(6, record.isPaid)
            statement.setInt(7, record.id)
            statement.executeUpdate()
        }
        logActionOnMembershipRecord(record.memberId, record.membershipId, "Ažurirana je članarina")
    }

    override fun deleteMembershipRecord(membershipRecord: MembershipRecord) {
        val query = "DELETE FROM MembershipRecord WHERE id = ?"

        logActionOnMembershipRecord(membershipRecord.memberId, membershipRecord.membershipId, "Obrisana je članarina")
        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, membershipRecord.id)
            statement.executeUpdate()
        }
    }

    override fun getMembersMembershipRecords(id: Int): List<MembershipRecord> {
        val memberRecords = mutableListOf<MembershipRecord>()
        val query = "SELECT * FROM MembershipRecord WHERE memberId = ? ORDER BY dateStarted"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val record = MembershipRecord(
                    id = resultSet.getInt("id"),
                    memberId = resultSet.getInt("memberId"),
                    membershipId = resultSet.getInt("membershipId"),
                    dateStarted = LocalDate.parse(resultSet.getString("dateStarted")),
                    dateFinished = LocalDate.parse(resultSet.getString("dateFinished")),
                    isActive = resultSet.getBoolean("isActive"),
                    isPaid = resultSet.getBoolean("isPaid")
                )
                memberRecords.add(record)
            }
        }

        return memberRecords
    }

    override fun deleteAllTrainingsAssociatedWithRecord(membershipRecord: MembershipRecord) {
        val deleteTrainingSessionsQuery = "DELETE FROM TrainingSession WHERE membershipRecordId = ?"

        logActionOnMembershipRecord(membershipRecord.memberId, membershipRecord.membershipId, "Obrisani su treninzi vezani za članarinu")
        dbConnection.prepareStatement(deleteTrainingSessionsQuery).use { statement ->
            statement.setInt(1, membershipRecord.id)
            statement.executeUpdate()
        }

    }

    override fun validateMemberships() {
        deactivateExpiredMemberships()
        activateMemberships()
    }

    private fun fetchMemberAndMembershipDetails(memberId: Int, membershipId: Int): Triple<String, String, String> {
        val detailsQuery = """
            SELECT m.name AS memberName, m.surname AS memberSurname, ms.name AS membershipName
            FROM Member m
            JOIN Membership ms ON ms.id = ?
            WHERE m.id = ?
        """

        dbConnection.prepareStatement(detailsQuery).use { statement ->
            statement.setInt(1, membershipId)
            statement.setInt(2, memberId)
            val resultSet = statement.executeQuery()

            if (resultSet.next()) {
                val memberName = resultSet.getString("memberName")
                val memberSurname = resultSet.getString("memberSurname")
                val membershipName = resultSet.getString("membershipName")
                return Triple(memberName, memberSurname, membershipName)
            } else {
                throw SQLException("Nije moguće dohvatiti detalje za člana i članarinu.")
            }
        }
    }


    private fun logActionOnMembershipRecord(memberId: Int, membershipId: Int, actionDescription: String) {
        val (memberName, memberSurname, membershipName) = fetchMemberAndMembershipDetails(memberId, membershipId)
        val logText = "$actionDescription $membershipName za člana $memberName $memberSurname"

        val query = "INSERT INTO UserActivityLog(appUserId, action, dateOfAction) VALUES (?, ?, ?)"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, PreferencesHelper().loggedInUserId!!)
            statement.setString(2, logText)
            statement.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            statement.executeUpdate()
        }
    }

    private fun deactivateExpiredMemberships() {
        dbConnection.prepareStatement(querySelectInactive).use { selectStatement ->
            selectStatement.setString(1, LocalDate.now().toString())
            val resultSet = selectStatement.executeQuery()

            dbConnection.prepareStatement(queryDeactivateMembership).use { updateStatement ->
                while (resultSet.next()) {
                    val membershipRecordId = resultSet.getInt("id")
                    updateStatement.setInt(1, membershipRecordId)
                    updateStatement.executeUpdate()
                }
            }
        }
    }

    private fun activateMemberships() {
        val today = LocalDate.now()
        dbConnection.prepareStatement(querySelectToActivate).use { selectStatement ->
            selectStatement.setString(1, today.toString())
            selectStatement.setString(2, today.toString())
            val resultSet = selectStatement.executeQuery()

            dbConnection.prepareStatement(queryActivateMembership).use { updateStatement ->
                while (resultSet.next()) {
                    val isNoLimit = resultSet.getBoolean("isNoLimit")
                    val numberOfTrainingsAvailable = resultSet.getInt("numberOfTrainingsAvailable")
                    val membershipRecordId = resultSet.getInt("id")

                    if (!isNoLimit) {
                        val hasRemainingSessions = checkRemainingSessions(membershipRecordId, numberOfTrainingsAvailable)
                        if (!hasRemainingSessions) {
                            continue
                        }
                    }

                    updateStatement.setInt(1, membershipRecordId)
                    updateStatement.executeUpdate()
                }
            }
        }
    }

    private fun checkRemainingSessions(membershipRecordId: Int, numberOfTrainingsAvailable: Int): Boolean {
        val sessionsQuery = """
            SELECT COUNT(*) AS trainingsUsed FROM TrainingSession 
            WHERE membershipRecordId = ?
        """

        dbConnection.prepareStatement(sessionsQuery).use { sessionStatement ->
            sessionStatement.setInt(1, membershipRecordId)
            val sessionResultSet = sessionStatement.executeQuery()

            if (sessionResultSet.next()) {
                val trainingsUsed = sessionResultSet.getInt("trainingsUsed")
                return trainingsUsed < numberOfTrainingsAvailable
            }
        }
        return false
    }
}
