package hr.kotwave.scorpiongym.database

import java.nio.file.FileSystems
import java.sql.Connection
import java.sql.DriverManager

object DatabaseFactory {

    init {
        try {
            Class.forName("org.sqlite.JDBC")
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
}