package hr.kotwave.scorpiongym.appuser

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat

class AppUserDaoImpl(private val connection: Connection) : AppUserDao {

    override fun getAllUsers(): ArrayList<AppUser> {
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

    override fun getUserActivityLogs(appUserId: Int): List<Pair<String, String>> {
        val activityLogs = mutableListOf<Pair<String, String>>()
        val query = "SELECT action, dateOfAction FROM UserActivityLog WHERE appUserId = ?"

        connection.prepareStatement(query).use { statement ->
            statement.setInt(1, appUserId)
            val resultSet = statement.executeQuery()

            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Input format from SQLite
            val outputDateFormat = SimpleDateFormat("MM.dd.yyyy HH:mm")

            while (resultSet.next()) {
                val action = resultSet.getString("action")
                val dateOfActionString = resultSet.getString("dateOfAction")
                val formattedDate = outputDateFormat.format(inputDateFormat.parse(dateOfActionString) ?: "")

                activityLogs.add(Pair(action, formattedDate))
            }
        }

        return activityLogs
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