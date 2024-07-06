package hr.kotwave.scorpiongym.member

import hr.kotwave.scorpiongym.util.parseToLocalDate
import hr.kotwave.scorpiongym.util.parseToLocalDateTime
import java.sql.Connection
import java.sql.SQLException

class MemberDaoImpl(private val dbConnection: Connection) : MemberDao {
    override fun getAllMembers(): List<Member> {
        val members = mutableListOf<Member>()
        val query = "SELECT * FROM Member"

        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val member = Member(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    surname = resultSet.getString("surname"),
                    phoneNumber = resultSet.getString("phoneNumber"),
                    signedUpDate = parseToLocalDateTime(resultSet.getString("signedUpDate")),
                    membershipRecordId = resultSet.getInt("membershipRecordId"),
                    organizationId = resultSet.getInt("organizationId"),
                    statusId = resultSet.getInt("statusId"),
                    remark = resultSet.getString("remark"),
                    dateOfBirth = resultSet.getString("dateOfBirth")?.let { parseToLocalDate(it) }
                )
                members.add(member)
            }
        }

        return members
    }

    override fun getMemberById(id: Int): Member? {
        val query = "SELECT * FROM Member WHERE id = ?"
        var member: Member? = null

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                member = Member(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    surname = resultSet.getString("surname"),
                    phoneNumber = resultSet.getString("phoneNumber"),
                    signedUpDate = parseToLocalDateTime(resultSet.getString("signedUpDate")),
                    membershipRecordId = resultSet.getInt("membershipRecordId"),
                    organizationId = resultSet.getInt("organizationId"),
                    statusId = resultSet.getInt("statusId"),
                    remark = resultSet.getString("remark"),
                    dateOfBirth = resultSet.getString("dateOfBirth")?.let { parseToLocalDate(it) }
                )
            }
        }

        return member
    }

    override fun insertMember(member: Member) : Int {
        val query = """
            INSERT INTO Member (name, surname, phoneNumber, signedUpDate, organizationId, remark)
            VALUES (?, ?, ?, ?, ?, ?)
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, member.name)
            statement.setString(2, member.surname)
            statement.setString(3, member.phoneNumber)
            statement.setString(4, member.signedUpDate.toString())
            member.organizationId?.let { statement.setInt(5, it) } ?: statement.setNull(5, java.sql.Types.INTEGER)
            statement.setString(6, member.remark)

            statement.executeUpdate()
            val generatedKeys = statement.generatedKeys
            return if (generatedKeys.next()) {
                generatedKeys.getInt(1)
            } else {
                throw SQLException("Neuspješno kreiranje člana!")
            }
        }
    }

    override fun updateMember(member: Member) {
        val query = """
            UPDATE Member SET name = ?, surname = ?, phoneNumber = ?, organizationId = ?, remark = ?, membershipRecordId = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, member.name)
            statement.setString(2, member.surname)
            statement.setString(3, member.phoneNumber)
            (member.organizationId?.takeIf { it != 0 })?.let {
                statement.setInt(4, it)
            } ?: statement.setNull(4, java.sql.Types.INTEGER)
            statement.setString(5, member.remark)
            (member.membershipRecordId?.takeIf { it != 0 })?.let {
                statement.setInt(6, it)
            } ?: statement.setNull(6, java.sql.Types.INTEGER)
            statement.setInt(7, member.id)
            statement.executeUpdate()
        }

    }

    override fun deleteMember(id: Int) {
        val query = "DELETE FROM Member WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }
}
