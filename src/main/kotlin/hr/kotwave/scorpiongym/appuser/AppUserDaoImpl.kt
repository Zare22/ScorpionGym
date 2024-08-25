package hr.kotwave.scorpiongym.appuser

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

class AppUserDaoImpl(private val connection: Connection) : AppUserDao {

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
}

private fun mapResultSetToAppUser(resultSet: ResultSet): AppUser {
    return AppUser(
        id = resultSet.getInt("id"),
        username = resultSet.getString("username"),
        password = resultSet.getString("password"),
        isAdmin = resultSet.getBoolean("isAdmin")
    )
}