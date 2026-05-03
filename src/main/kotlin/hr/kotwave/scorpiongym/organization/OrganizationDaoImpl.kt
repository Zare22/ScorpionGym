package hr.kotwave.scorpiongym.organization

import hr.kotwave.scorpiongym.util.AuditLog
import java.sql.Connection
import java.sql.SQLException

class OrganizationDaoImpl(private val dbConnection: Connection) : OrganizationDao {
    private val auditLog = AuditLog(dbConnection)

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
        auditLog.write("Kreirana nova organizacija ${organization.name}")
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
        auditLog.write("Ažurirani podatci organizacije ${organization.name}")
    }

    override fun deleteOrganization(organization: Organization) {
        val query = "DELETE FROM Organization WHERE id = ?"

        auditLog.write("Pobrisana članarina ${organization.name}")
        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, organization.id)
            statement.executeUpdate()
        }
    }

}
