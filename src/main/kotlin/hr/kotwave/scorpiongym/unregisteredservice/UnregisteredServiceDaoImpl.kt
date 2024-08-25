package hr.kotwave.scorpiongym.unregisteredservice

import hr.kotwave.scorpiongym.util.PreferencesHelper
import hr.kotwave.scorpiongym.util.parseToLocalDateTime
import java.sql.Connection
import java.sql.SQLException

class UnregisteredServiceDaoImpl(private val dbConnection: Connection) : UnregisteredServiceDao {

    override fun getAllUnregisteredServices(): List<UnregisteredService> {
        val unregisteredServices = mutableListOf<UnregisteredService>()
        val query = "SELECT * FROM UnregisteredService"

        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val unregisteredService = UnregisteredService(
                    id = resultSet.getInt("id"),
                    dateOfService = parseToLocalDateTime(resultSet.getString("dateOfService")),
                    membershipId = resultSet.getInt("membershipId"),
                    otherServiceId = resultSet.getInt("otherServiceId"),
                    isPaid = resultSet.getBoolean("isPaid")
                )
                unregisteredServices.add(unregisteredService)
            }
        }

        return unregisteredServices
    }

    override fun getUnregisteredServiceById(id: Int): UnregisteredService? {
        val query = "SELECT * FROM UnregisteredService WHERE id = ?"
        var unregisteredService: UnregisteredService? = null

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                unregisteredService = UnregisteredService(
                    id = resultSet.getInt("id"),
                    dateOfService = parseToLocalDateTime(resultSet.getString("dateOfService")),
                    membershipId = resultSet.getInt("membershipId"),
                    otherServiceId = resultSet.getInt("otherServiceId"),
                    isPaid = resultSet.getBoolean("isPaid")
                )
            }
        }

        return unregisteredService
    }

    override fun insertUnregisteredService(unregisteredService: UnregisteredService): Int {
        val query = """
            INSERT INTO UnregisteredService (dateOfService, membershipId, otherServiceId, isPaid)
            VALUES (?, ?, ?, ?)
            RETURNING id
        """

        var insertedId: Int
        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, unregisteredService.dateOfService.toString())
            unregisteredService.membershipId?.let { statement.setInt(2, it) }
            unregisteredService.otherServiceId?.let { statement.setInt(3, it) }
            statement.setBoolean(4, unregisteredService.isPaid)

            val resultSet = statement.executeQuery()

            insertedId = resultSet.takeIf { it.next() }?.getInt(1)
                ?: throw SQLException("ID ostale usluge se nije kreirao")

        }
        logAction(insertedId, "Unesena nova usluga/trening za neregistriranog člana")
        return insertedId
    }

    override fun updateUnregisteredService(unregisteredService: UnregisteredService) {
        val query = """
            UPDATE UnregisteredService SET dateOfService = ?, membershipId = ?, otherServiceId = ?, isPaid = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, unregisteredService.dateOfService.toString())
            unregisteredService.membershipId?.let { statement.setInt(2, it) }
            unregisteredService.otherServiceId?.let { statement.setInt(3, it) }
            statement.setBoolean(4, unregisteredService.isPaid)
            statement.setInt(5, unregisteredService.id)
            statement.executeUpdate()
        }
        logAction(unregisteredService.id, "Ažurirana je usluga")
    }

    override fun deleteUnregisteredService(unregisteredService: UnregisteredService) {
        val query = "DELETE FROM UnregisteredService WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, unregisteredService.id)
            statement.executeUpdate()
        }
        logAction(unregisteredService.id, "Obrisana je usluga")
    }

    private fun fetchMembershipAndServiceDetails(serviceId: Int): Pair<String?, String?> {
        val detailsQuery = """
            SELECT m.name AS membershipName, os.name AS otherServiceName
            FROM UnregisteredService us
            LEFT JOIN Membership m ON us.membershipId = m.id
            LEFT JOIN OtherService os ON us.otherServiceId = os.id
            WHERE us.id = ?
        """

        dbConnection.prepareStatement(detailsQuery).use { statement ->
            statement.setInt(1, serviceId)
            val resultSet = statement.executeQuery()

            if (resultSet.next()) {
                val membershipName = resultSet.getString("membershipName")
                val otherServiceName = resultSet.getString("otherServiceName")
                return Pair(membershipName, otherServiceName)
            } else {
                throw SQLException("Nije moguće dohvatiti detalje za uslugu.")
            }
        }
    }


    private fun logAction(serviceId: Int, actionDescription: String) {
        val (membershipName, otherServiceName) = fetchMembershipAndServiceDetails(serviceId)
        val serviceName = membershipName ?: otherServiceName ?: "Nepoznata usluga"

        logActionOnMemberOtherService("$actionDescription $serviceName")
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
