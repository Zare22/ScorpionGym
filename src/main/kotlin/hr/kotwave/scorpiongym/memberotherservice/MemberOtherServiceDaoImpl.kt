package hr.kotwave.scorpiongym.memberotherservice

import hr.kotwave.scorpiongym.util.parseToLocalDateTime
import java.sql.Connection
import java.sql.SQLException

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
                    isPaid = resultSet.getBoolean("isPaid")
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
                    isPaid = resultSet.getBoolean("isPaid")
                )
            }
        }

        return memberOtherService
    }

    override fun insertMemberOtherService(memberOtherService: MemberOtherService): Int {
        val query = """
            INSERT INTO MemberOtherService (dateOfService, memberId, otherServiceId, isPaid)
            VALUES (?, ?, ?, ?)
            RETURNING id
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, memberOtherService.dateOfService.toString())
            statement.setInt(2, memberOtherService.memberId)
            statement.setInt(3, memberOtherService.otherServiceId)
            statement.setBoolean(4, memberOtherService.isPaid)

            val resultSet = statement.executeQuery()

            return resultSet.takeIf { it.next() }?.getInt(1)
                ?: throw SQLException("ID ostale usluge se nije kreirao")

        }
    }

    override fun updateMemberOtherService(memberOtherService: MemberOtherService) {
        val query = """
            UPDATE MemberOtherService SET dateOfService = ?, memberId = ?, otherServiceId = ?, isPaid = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, memberOtherService.dateOfService.toString())
            statement.setInt(2, memberOtherService.memberId)
            statement.setInt(3, memberOtherService.otherServiceId)
            statement.setBoolean(4, memberOtherService.isPaid)
            statement.setInt(5, memberOtherService.id)
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

    override fun getMembersOtherServices(memberId: Int): List<MemberOtherService> {
        val memberOtherServices = mutableListOf<MemberOtherService>()
        val query = "SELECT * FROM MemberOtherService WHERE memberId = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, memberId)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val otherService = MemberOtherService(
                    id = resultSet.getInt("id"),
                    dateOfService = parseToLocalDateTime(resultSet.getString("dateOfService")),
                    isPaid =  resultSet.getBoolean("isPaid"),
                    memberId = resultSet.getInt("memberId"),
                    otherServiceId = resultSet.getInt("otherServiceId"),
                )
                memberOtherServices.add(otherService)
            }
        }

        return memberOtherServices
    }
}
