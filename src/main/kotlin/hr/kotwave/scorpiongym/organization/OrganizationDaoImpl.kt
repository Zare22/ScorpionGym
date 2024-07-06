package hr.kotwave.scorpiongym.organization

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
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, organization.name)
            statement.setInt(2, organization.typeOfOrganizationId)
            statement.executeUpdate()

            val generatedKeys = statement.generatedKeys
            return if (generatedKeys.next()) {
                generatedKeys.getInt(1)
            } else
                throw SQLException("Neuspješno kreiranje organizacije!")
        }
    }

    override fun updateOrganization(organization: Organization) {
        val query = """
            UPDATE Organization SET name = ?, typeOfOrganizationId = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, organization.name)
            statement.setInt(2, organization.typeOfOrganizationId)
            statement.setInt(3, organization.id)
            statement.executeUpdate()
        }
    }

    override fun deleteOrganization(id: Int) {
        val query = "DELETE FROM Organization WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }
}
