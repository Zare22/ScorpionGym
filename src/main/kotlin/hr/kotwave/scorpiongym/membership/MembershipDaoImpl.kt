package hr.kotwave.scorpiongym.membership

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
                    numberOfTrainingsAvailable = resultSet.getInt("numberOfTrainingsAvailable")
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
                    numberOfTrainingsAvailable = resultSet.getInt("numberOfTrainingsAvailable")
                )
            }
        }

        return membership
    }

    override fun insertMembership(membership: Membership) : Int {
        val query = """
            INSERT INTO Membership (name, price, numberOfTrainingsAvailable)
            VALUES (?, ?, ?)
            RETURNING id
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, membership.name)
            statement.setDouble(2, membership.price)
            statement.setInt(3, membership.numberOfTrainingsAvailable)

            val resultSet = statement.executeQuery()

            return resultSet.takeIf { it.next() }?.getInt(1) ?: throw SQLException("ID tipa članarine nije kreiran!")
        }
    }

    override fun updateMembership(membership: Membership) {
        val query = """
            UPDATE Membership SET name = ?, price = ?, numberOfTrainingsAvailable = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, membership.name)
            statement.setDouble(2, membership.price)
            statement.setInt(3, membership.numberOfTrainingsAvailable)
            statement.setInt(4, membership.id)
            statement.executeUpdate()
        }
    }

    override fun deleteMembership(id: Int) {
        val query = "DELETE FROM Membership WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }
}
