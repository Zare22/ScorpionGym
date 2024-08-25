package hr.kotwave.scorpiongym.membership

import hr.kotwave.scorpiongym.util.PreferencesHelper
import java.sql.Connection
import java.sql.SQLException

class MembershipDaoImpl(private val dbConnection: Connection) : MembershipDao {
    override fun getAllMemberships(): List<Membership> {
        val memberships = mutableListOf<Membership>()
        val query = "SELECT * FROM Membership"

        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val membership = Membership(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    price = resultSet.getDouble("price"),
                    numberOfTrainingsAvailable = resultSet.getInt("numberOfTrainingsAvailable"),
                    duration = resultSet.getLong("duration"),
                    isNoLimit = resultSet.getBoolean("isNoLimit")
                )
                memberships.add(membership)
            }
        }

        return memberships
    }

    override fun getMembershipById(id: Int): Membership? {
        val query = "SELECT * FROM Membership WHERE id = ?"
        var membership: Membership? = null

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                membership = Membership(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    price = resultSet.getDouble("price"),
                    numberOfTrainingsAvailable = resultSet.getInt("numberOfTrainingsAvailable"),
                    duration = resultSet.getLong("duration"),
                    isNoLimit = resultSet.getBoolean("isNoLimit")
                )
            }
        }

        return membership
    }

    override fun insertMembership(membership: Membership): Int {
        val query = """
            INSERT INTO Membership (name, price, numberOfTrainingsAvailable, duration, isNoLimit)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
        """

        var insertedId: Int
        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, membership.name)
            statement.setDouble(2, membership.price)
            statement.setInt(3, membership.numberOfTrainingsAvailable)
            statement.setLong(4, membership.duration)
            statement.setBoolean(5, membership.isNoLimit)

            val resultSet = statement.executeQuery()

            insertedId =
                resultSet.takeIf { it.next() }?.getInt(1) ?: throw SQLException("ID tipa članarine nije kreiran!")
        }
        logActionOnMembership("Kreirana nova članarina ${membership.name}")
        return insertedId
    }

    override fun updateMembership(membership: Membership) {
        val query = """
            UPDATE Membership SET name = ?, price = ?, numberOfTrainingsAvailable = ?, duration = ?, isNoLimit = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, membership.name)
            statement.setDouble(2, membership.price)
            statement.setInt(3, membership.numberOfTrainingsAvailable)
            statement.setLong(4, membership.duration)
            statement.setBoolean(5, membership.isNoLimit)
            statement.setInt(6, membership.id)
            statement.executeUpdate()
        }
        logActionOnMembership("Ažurirani podatci članarine ${membership.name}")
    }

    override fun deleteMembership(membership: Membership) {
        val query = "DELETE FROM Membership WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, membership.id)
            statement.executeUpdate()
        }
        logActionOnMembership("Pobrisana članarina ${membership.name}")
    }

    private fun logActionOnMembership(text: String) {
        val query = "INSERT INTO UserActivityLog(appUserId, action) VALUES (?, ?)"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, PreferencesHelper().loggedInUserId!!)
            statement.setString(2, text)
            statement.executeUpdate()
        }
    }
}
