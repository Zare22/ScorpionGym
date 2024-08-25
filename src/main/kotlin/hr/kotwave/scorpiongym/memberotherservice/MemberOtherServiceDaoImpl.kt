package hr.kotwave.scorpiongym.memberotherservice

import hr.kotwave.scorpiongym.util.PreferencesHelper
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

        var insertedId: Int
        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, memberOtherService.dateOfService.toString())
            statement.setInt(2, memberOtherService.memberId)
            statement.setInt(3, memberOtherService.otherServiceId)
            statement.setBoolean(4, memberOtherService.isPaid)

            val resultSet = statement.executeQuery()

            insertedId = resultSet.takeIf { it.next() }?.getInt(1)
                ?: throw SQLException("ID ostale usluge se nije kreirao")

        }
        logAction(insertedId, "Unesena nova usluga")
        return insertedId
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
        logAction(memberOtherService.id, "Ažurirana je usluga")
    }

    override fun deleteMemberOtherServiceById(memberOtherService: MemberOtherService) {
        val query = "DELETE FROM MemberOtherService WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, memberOtherService.id)
            statement.executeUpdate()
        }
        logAction(memberOtherService.id, "Obrisana je usluga")
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
                    isPaid = resultSet.getBoolean("isPaid"),
                    memberId = resultSet.getInt("memberId"),
                    otherServiceId = resultSet.getInt("otherServiceId"),
                )
                memberOtherServices.add(otherService)
            }
        }

        return memberOtherServices
    }

    private fun fetchMemberAndServiceDetails(serviceId: Int): Triple<String, String, String> {
        val detailsQuery = """
            SELECT m.name AS memberName, m.surname AS memberSurname, os.name AS otherServiceName
            FROM MemberOtherService mos
            JOIN Member m ON mos.memberId = m.id
            JOIN OtherService os ON mos.otherServiceId = os.id
            WHERE mos.id = ?
        """

        dbConnection.prepareStatement(detailsQuery).use { statement ->
            statement.setInt(1, serviceId)
            val resultSet = statement.executeQuery()

            if (resultSet.next()) {
                val memberName = resultSet.getString("memberName")
                val memberSurname = resultSet.getString("memberSurname")
                val otherServiceName = resultSet.getString("otherServiceName")
                return Triple(memberName, memberSurname, otherServiceName)
            } else {
                throw SQLException("Nije moguće dohvatiti detalje za uslugu i člana.")
            }
        }
    }

    private fun logAction(serviceId: Int, actionDescription: String) {
        val (memberName, memberSurname, otherServiceName) = fetchMemberAndServiceDetails(serviceId)
        logActionOnMemberOtherService("$actionDescription $otherServiceName za člana $memberName $memberSurname")
    }

    private fun logActionOnMemberOtherService(text: String) {
        val query = "INSERT INTO UserActivityLog(appUserId, action) VALUES (?, ?)"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, PreferencesHelper().loggedInUserId!!)
            statement.setString(2, text)
            statement.executeUpdate()
        }
    }

}
