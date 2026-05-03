package hr.kotwave.scorpiongym.otherservice

import hr.kotwave.scorpiongym.util.AuditLog
import java.sql.Connection
import java.sql.SQLException

class OtherServiceDaoImpl(private val dbConnection: Connection) : OtherServiceDao {
    private val auditLog = AuditLog(dbConnection)

    override fun getAllOtherServices(): List<OtherService> {
        val otherServices = mutableListOf<OtherService>()
        val query = "SELECT * FROM OtherService"

        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val otherService = OtherService(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    price = resultSet.getDouble("price")
                )
                otherServices.add(otherService)
            }
        }

        return otherServices
    }

    override fun getServiceById(id: Int): OtherService? {
        val query = "SELECT * FROM OtherService WHERE id = ?"
        var otherService: OtherService? = null

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                otherService = OtherService(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    price = resultSet.getDouble("price")
                )
            }
        }

        return otherService
    }

    override fun insertOtherService(otherService: OtherService): Int {
        val query = """
            INSERT INTO OtherService (name, price)
            VALUES (?, ?)
            RETURNING id
        """

        var insertedId: Int
        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, otherService.name)
            statement.setDouble(2, otherService.price)

            val resultSet = statement.executeQuery()

            insertedId =
                resultSet.takeIf { it.next() }?.getInt(1) ?: throw SQLException("ID ostale usluge nije kreiran!")
        }
        auditLog.write("Kreirana nova usluga ${otherService.name}")
        return insertedId
    }

    override fun updateOtherService(otherService: OtherService) {
        val query = """
            UPDATE OtherService SET name = ?, price = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, otherService.name)
            statement.setDouble(2, otherService.price)
            statement.setInt(3, otherService.id)
            statement.executeUpdate()
        }
        auditLog.write("Ažurirani podatci usluge ${otherService.name}")
    }

    override fun deleteOtherServiceById(otherService: OtherService) {
        val query = "DELETE FROM OtherService WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, otherService.id)
            statement.executeUpdate()
        }
        auditLog.write("Pobrisana usluga ${otherService.name}")
    }

}
