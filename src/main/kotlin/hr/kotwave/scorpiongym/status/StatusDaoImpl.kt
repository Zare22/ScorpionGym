package hr.kotwave.scorpiongym.status

import java.sql.Connection

class StatusDaoImpl(private val dbConnection: Connection) : StatusDao {
    override fun getAllStatuses(): List<Status> {
        val statuses = mutableListOf<Status>()
        val query = "SELECT * FROM Status"

        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val status = Status(
                    id = resultSet.getInt("id"),
                    description = resultSet.getString("description")
                )
                statuses.add(status)
            }
        }

        return statuses
    }

    override fun getStatusById(id: Int): Status? {
        val query = "SELECT * FROM Status WHERE id = ?"
        var status: Status? = null

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                status = Status(
                    id = resultSet.getInt("id"),
                    description = resultSet.getString("description")
                )
            }
        }

        return status
    }

    override fun insertStatus(status: Status) {
        val query = """
            INSERT INTO Status (description)
            VALUES (?)
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, status.description)
            statement.executeUpdate()
        }
    }

    override fun updateStatus(status: Status) {
        val query = """
            UPDATE Status SET description = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, status.description)
            statement.setInt(2, status.id)
            statement.executeUpdate()
        }
    }

    override fun deleteStatusById(id: Int) {
        val query = "DELETE FROM Status WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }
}
