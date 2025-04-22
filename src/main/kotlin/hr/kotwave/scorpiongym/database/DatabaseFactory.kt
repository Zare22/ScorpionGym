package hr.kotwave.scorpiongym.database

import java.nio.file.FileSystems
import java.sql.Connection
import java.sql.DriverManager

object DatabaseFactory {

    init {
        try {
            Class.forName("org.sqlite.JDBC")
            initDB()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    fun connect(): Connection? {
        return try {
            val userHome = System.getProperty("user.home")
            val dbPath =
                "$userHome${FileSystems.getDefault().separator}ScorpionGym${FileSystems.getDefault().separator}gymdatabase.db"
            val url = "jdbc:sqlite:$dbPath"
            val connection = DriverManager.getConnection(url)

            connection.createStatement().use { statement ->
                statement.execute("PRAGMA foreign_keys = ON;")
            }

            connection

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun initDB() {
        val connection = connect()
        connection?.use { con ->
            con.autoCommit = false
            try {
                if (!columnExists(con, "Member", "gender")) {
                    con.createStatement().use { stmt ->
                        stmt.execute("ALTER TABLE Member ADD COLUMN gender TEXT")
                    }
                }
                con.commit()
            } catch (e: Exception) {
                con.rollback()
                e.printStackTrace()
            }
        }
    }

    fun columnExists(connection: Connection, tableName: String, columnName: String): Boolean {
        val resultSet = connection.createStatement().executeQuery("PRAGMA table_info($tableName)")
        while (resultSet.next()) {
            if (resultSet.getString("name") == columnName) return true
        }
        return false
    }
}