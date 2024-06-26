package hr.kotwave.scorpiongym.memberotherservice

import hr.kotwave.scorpiongym.util.parseToLocalDateTime
import java.sql.Connection

class MemberOtherServiceDaoImpl(private val dbConnection: Connection) : MemberOtherServiceDao {
    override fun getAllMemberOtherServices(): List<MemberOtherService> {
        val memberOtherServices = mutableListOf<MemberOtherService>()
        val query = "SELECT * FROM MemberOtherService"

        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val memberOtherService = MemberOtherService(
                    id = resultSet.getInt("id"),
                    dateOfService = parseToLocalDateTime(resultSet.getString("dateOfService")),
                    memberId = resultSet.getInt("memberId"),
                    otherServiceId = resultSet.getInt("otherServiceId"),
                )
                memberOtherServices.add(memberOtherService)
            }
        }

        return memberOtherServices
    }

    override fun getMemberOtherServiceById(id: Int): MemberOtherService? {
        val query = "SELECT * FROM MemberOtherService WHERE id = ?"
        var memberOtherService: MemberOtherService? = null

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                memberOtherService = MemberOtherService(
                    id = resultSet.getInt("id"),
                    dateOfService = parseToLocalDateTime(resultSet.getString("dateOfService")),
                    memberId = resultSet.getInt("memberId"),
                    otherServiceId = resultSet.getInt("otherServiceId"),
                )
            }
        }

        return memberOtherService
    }

    override fun insertMemberOtherService(memberOtherService: MemberOtherService) {
        val query = """
            INSERT INTO MemberOtherService (dateOfService, memberId, otherServiceId)
            VALUES (?, ?, ?)
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, memberOtherService.dateOfService.toString())
            statement.setInt(2, memberOtherService.memberId)
            statement.setInt(3, memberOtherService.otherServiceId)
            statement.executeUpdate()
        }
    }

    override fun updateMemberOtherService(memberOtherService: MemberOtherService) {
        val query = """
            UPDATE MemberOtherService SET dateOfService = ?, memberId = ?, otherServiceId = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, memberOtherService.dateOfService.toString())
            statement.setInt(2, memberOtherService.memberId)
            statement.setInt(3, memberOtherService.otherServiceId)
            statement.setInt(2, memberOtherService.id)
            statement.executeUpdate()
        }
    }

    override fun deleteMemberOtherServiceById(id: Int) {
        val query = "DELETE FROM MemberOtherService WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }
}
