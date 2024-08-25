package hr.kotwave.scorpiongym.membershiprecord

import hr.kotwave.scorpiongym.util.PreferencesHelper
import java.sql.Connection
import java.sql.SQLException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MembershipRecordDaoImpl(private val dbConnection: Connection) : MembershipRecordDao {

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

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

    override fun insertMembershipRecord(record: MembershipRecord) : Int {
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
        logActionOnMembershipRecord(record.memberId, record.membershipId, "Produžena je članarina")

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

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, membershipRecord.id)
            statement.executeUpdate()
        }
        logActionOnMembershipRecord(membershipRecord.memberId, membershipRecord.membershipId, "Obrisana je članarina")
    }

    override fun getMembersMembershipRecords(id: Int): List<MembershipRecord> {
        val memberRecords = mutableListOf<MembershipRecord>()
        val query = "SELECT * FROM MembershipRecord WHERE memberId = ?"

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

    override fun validateMemberships() {
        val today = LocalDate.now()
        val querySelect = """
            SELECT id FROM MembershipRecord
            WHERE dateFinished < ? AND isActive = 1
        """
        val queryUpdate = """
            UPDATE MembershipRecord SET isActive = 0 WHERE id = ?
        """

        try {
            dbConnection.prepareStatement(querySelect).use { selectStatement ->
                selectStatement.setString(1, today.toString())
                val resultSet = selectStatement.executeQuery()

                dbConnection.prepareStatement(queryUpdate).use { updateStatement ->
                    while (resultSet.next()) {
                        val membershipId = resultSet.getInt("id")
                        updateStatement.setInt(1, membershipId)
                        updateStatement.executeUpdate()
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()  // Handle exceptions as needed
        }
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

        val query = "INSERT INTO UserActivityLog(appUserId, action) VALUES (?, ?)"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, PreferencesHelper().loggedInUserId!!)
            statement.setString(2, logText)
            statement.executeUpdate()
        }
    }
}
