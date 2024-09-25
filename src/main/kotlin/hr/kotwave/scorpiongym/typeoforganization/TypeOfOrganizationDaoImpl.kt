package hr.kotwave.scorpiongym.typeoforganization

import hr.kotwave.scorpiongym.util.PreferencesHelper
import java.sql.Connection
import java.sql.SQLException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TypeOfOrganizationDaoImpl(private val dbConnection: Connection) : TypeOfOrganizationDao {
    override fun getAllTypesOfOrganizations(): List<TypeOfOrganization> {
        val typesOfOrganizations = mutableListOf<TypeOfOrganization>()
        val query = "SELECT * FROM TypeOfOrganization"

        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val typeOfOrganization = TypeOfOrganization(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    discountRate = resultSet.getDouble("discountRate")
                )
                typesOfOrganizations.add(typeOfOrganization)
            }
        }

        return typesOfOrganizations
    }

    override fun getTypeOfOrganizationById(id: Int): TypeOfOrganization? {
        val query = "SELECT * FROM TypeOfOrganization WHERE id = ?"
        var typeOfOrganization: TypeOfOrganization? = null

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                typeOfOrganization = TypeOfOrganization(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    discountRate = resultSet.getDouble("discountRate")
                )
            }
        }
        return typeOfOrganization
    }

    override fun insertTypeOfOrganization(typeOfOrganization: TypeOfOrganization): Int {
        val query = """
            INSERT INTO TypeOfOrganization (name, discountRate)
            VALUES (?, ?)
            RETURNING id
        """

        val insertedId: Int
        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, typeOfOrganization.name)
            statement.setDouble(2, typeOfOrganization.discountRate)

            val resultSet = statement.executeQuery()

            insertedId = resultSet.takeIf { it.next() }?.getInt(1) ?: throw SQLException("ID tipa organizacije nije kreiran!")
        }
        logActionOnTypeOfOrganization("Kreiran novi tip organizacije ${typeOfOrganization.name}")
        return insertedId
    }

    override fun updateTypeOfOrganization(typeOfOrganization: TypeOfOrganization) {
        val query = """
            UPDATE TypeOfOrganization SET name = ?, discountRate = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, typeOfOrganization.name)
            statement.setDouble(2, typeOfOrganization.discountRate)
            statement.setInt(3, typeOfOrganization.id)
            statement.executeUpdate()
        }
        logActionOnTypeOfOrganization("Ažurirani podatci organizacije ${typeOfOrganization.name}")
    }

    override fun deleteTypeOfOrganization(typeOfOrganization: TypeOfOrganization) {
        val query = "DELETE FROM TypeOfOrganization WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, typeOfOrganization.id)
            statement.executeUpdate()
        }
        logActionOnTypeOfOrganization("Pobrisan tip organizacije ${typeOfOrganization.name}")
    }

    private fun logActionOnTypeOfOrganization(text: String) {
        val query = "INSERT INTO UserActivityLog(appUserId, action, dateOfAction) VALUES (?, ?, ?)"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, PreferencesHelper().loggedInUserId!!)
            statement.setString(2, text)
            statement.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            statement.executeUpdate()
        }
    }
}
