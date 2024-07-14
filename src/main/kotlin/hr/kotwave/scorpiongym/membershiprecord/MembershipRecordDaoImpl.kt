package hr.kotwave.scorpiongym.membershiprecord

import hr.kotwave.scorpiongym.util.parseToLocalDateTime
import java.sql.Connection
import java.sql.SQLException

class MembershipRecordDaoImpl(private val dbConnection: Connection) : MembershipRecordDao {
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
                    dateStarted = parseToLocalDateTime(resultSet.getString("dateStarted")),
                    dateFinished = parseToLocalDateTime(resultSet.getString("dateFinished")),
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
                    dateStarted = parseToLocalDateTime(resultSet.getString("signedUpDate")),
                    dateFinished = parseToLocalDateTime(resultSet.getString("dateFinished")),
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

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, record.memberId)
            statement.setInt(2, record.membershipId)
            statement.setString(3, record.dateStarted.toString())
            statement.setString(4, record.dateFinished.toString())
            statement.setBoolean(5, record.isActive)
            statement.setBoolean(6, record.isPaid)

            val resultSet = statement.executeQuery()

            return resultSet.takeIf { it.next() }?.getInt(1) ?: throw SQLException("ID članarine nije kreiran!")
        }
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
    }

    override fun deleteMembershipRecord(id: Int) {
        val query = "DELETE FROM MembershipRecord WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
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
                    dateStarted = parseToLocalDateTime(resultSet.getString("dateStarted")),
                    dateFinished = parseToLocalDateTime(resultSet.getString("dateFinished")),
                    isActive = resultSet.getBoolean("isActive"),
                    isPaid = resultSet.getBoolean("isPaid")
                )
                memberRecords.add(record)
            }
        }

        return memberRecords
    }
}
