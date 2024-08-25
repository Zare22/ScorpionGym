package hr.kotwave.scorpiongym.organization

import hr.kotwave.scorpiongym.util.PreferencesHelper
import java.sql.Connection
import java.sql.SQLException

class OrganizationDaoImpl(private val dbConnection: Connection) : OrganizationDao {
    override fun getAllOrganizations(): List<Organization> {
        val organizations = mutableListOf<Organization>()
        val query = "SELECT * FROM Organization"

        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val organization = Organization(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    typeOfOrganizationId = resultSet.getInt("typeOfOrganizationId")
                )
                organizations.add(organization)
            }
        }

        return organizations
    }

    override fun getOrganizationById(id: Int): Organization? {
        val query = "SELECT * FROM Organization WHERE id = ?"
        var organization: Organization? = null

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                organization = Organization(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    typeOfOrganizationId = resultSet.getInt("typeOfOrganizationId")
                )
            }
        }

        return organization
    }

    override fun insertOrganization(organization: Organization): Int {
        val query = """
            INSERT INTO Organization (name, typeOfOrganizationId)
            VALUES (?, ?)
            RETURNING id
        """

        var insertedId: Int
        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, organization.name)
            organization.typeOfOrganizationId?.let {
                statement.setInt(2, it)
            } ?: run {
                statement.setNull(2, java.sql.Types.INTEGER)
            }

            val resultSet = statement.executeQuery()

            insertedId = resultSet.takeIf { it.next() }?.getInt(1) ?: throw SQLException("ID organizacije nije kreiran!")
        }
        logActionOnOrganization("Kreirana nova organizacija ${organization.name}")
        return insertedId
    }

    override fun updateOrganization(organization: Organization) {
        val query = """
            UPDATE Organization SET name = ?, typeOfOrganizationId = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, organization.name)
            organization.typeOfOrganizationId?.let { statement.setInt(2, it) }
            statement.setInt(3, organization.id)
            statement.executeUpdate()
        }
        logActionOnOrganization("Ažurirani podatci organizacije ${organization.name}")
    }

    override fun deleteOrganization(organization: Organization) {
        val query = "DELETE FROM Organization WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, organization.id)
            statement.executeUpdate()
        }
        logActionOnOrganization("Pobrisana članarina ${organization.name}")
    }

    private fun logActionOnOrganization(text: String) {
        val query = "INSERT INTO UserActivityLog(appUserId, action) VALUES (?, ?)"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, PreferencesHelper().loggedInUserId!!)
            statement.setString(2, text)
            statement.executeUpdate()
        }
    }
}
