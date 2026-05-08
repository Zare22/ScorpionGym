package hr.kotwave.scorpiongym.appuser

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat

class AppUserDaoImpl(private val connection: Connection) : AppUserDao {

    override fun getAllAppUsers(): ArrayList<AppUser> {
        val users = ArrayList<AppUser>()
        val query = "SELECT * FROM AppUser"

        connection.prepareStatement(query).use { statement ->
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val user = mapResultSetToAppUser(resultSet)
                users.add(user)
            }
        }

        return users
    }


    override fun loginAppUser(username: String, password: String): AppUser {
        val query = "SELECT * FROM AppUser WHERE username = ? AND password = ?"
        connection.prepareStatement(query).use { statement ->
            statement.setString(1, username)
            statement.setString(2, password)
            val resultSet = statement.executeQuery()

            return if (resultSet.next()) {
                mapResultSetToAppUser(resultSet)
            } else {
                throw Exception("Pogrešno korisničko ime ili lozinka")
            }
        }
    }

    override fun registerAppUser(username: String, password: String, isAdmin: Boolean): AppUser {
        val insertQuery = """
            INSERT INTO AppUser (username, password, isAdmin)
            VALUES (?, ?, ?)
            RETURNING id
        """
        connection.prepareStatement(insertQuery).use { statement ->
            statement.setString(1, username)
            statement.setString(2, password)
            statement.setBoolean(3, isAdmin)

            val resultSet = statement.executeQuery()
            return if (resultSet.next()) {
                AppUser(
                    id = resultSet.getInt("id"),
                    username = username,
                    password = password,
                    isAdmin = isAdmin
                )
            } else {
                throw SQLException("Greška pri registraciji")
            }
        }
    }

    override fun getAllActivityLogs(): List<Triple<String, String, String>> {
        val activityLogs = mutableListOf<Triple<String, String, String>>()
        val query = "SELECT action, dateOfAction, au.username FROM UserActivityLog ual INNER JOIN AppUser au on ual.appUserId = au.id"

        connection.prepareStatement(query).use { statement ->
            val resultSet = statement.executeQuery()

            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val outputDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

            while (resultSet.next()) {
                val action = resultSet.getString("action")
                val dateOfActionString = resultSet.getString("dateOfAction")
                val username = resultSet.getString("username")
                val formattedDate = outputDateFormat.format(inputDateFormat.parse(dateOfActionString) ?: "")

                activityLogs.add(Triple(action, formattedDate, username))
            }
        }
        return activityLogs
    }

    override fun deleteAppUser(user: AppUser) {
        connection.autoCommit = false

        try {
            val deleteLogs = "DELETE FROM UserActivityLog WHERE appUserId = ?"
            val deleteAudits = "DELETE FROM PaymentAuditLog WHERE loggedInUserId = ?"
            val deleteUser = "DELETE FROM AppUser WHERE id = ?"

            connection.prepareStatement(deleteLogs).use { stmt ->
                stmt.setInt(1, user.id)
                stmt.executeUpdate()
            }

            connection.prepareStatement(deleteAudits).use { stmt ->
                stmt.setInt(1, user.id)
                stmt.executeUpdate()
            }

            connection.prepareStatement(deleteUser).use { stmt ->
                stmt.setInt(1, user.id)
                stmt.executeUpdate()
            }

            connection.commit()
        } catch (e: SQLException) {
            connection.rollback()
            throw SQLException("Greška pri brisanju korisnika")
        } finally {
            connection.autoCommit = true
        }
    }

}

private fun mapResultSetToAppUser(resultSet: ResultSet): AppUser {
    return AppUser(
        id = resultSet.getInt("id"),
        username = resultSet.getString("username"),
        password = resultSet.getString("password"),
        isAdmin = resultSet.getBoolean("isAdmin")
    )
}