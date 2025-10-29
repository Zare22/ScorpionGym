package hr.kotwave.scorpiongym.database

import java.nio.file.FileSystems
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

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
                val trainingSessionSchema = getTableSchema(con, "TrainingSession")
                if (trainingSessionSchema != null && trainingSessionSchema.contains("ON DELETE RESTRICT")) {
                    println("Applying migration: Rebuilding TrainingSession table with ON DELETE CASCADE...")
                    con.createStatement().use { stmt ->
                        val beforeCount = getTableCount(con, "TrainingSession")
                        println("  - Original row count: $beforeCount")

                        stmt.execute("""
                        CREATE TABLE TrainingSession_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            membershipRecordId INTEGER NOT NULL,
                            sessionDateTime DATETIME NOT NULL,
                            FOREIGN KEY (membershipRecordId) 
                                REFERENCES MembershipRecord(id) 
                                ON DELETE CASCADE
                        )
                    """)

                        stmt.execute("""
                        INSERT INTO TrainingSession_new (id, membershipRecordId, sessionDateTime)
                        SELECT id, membershipRecordId, sessionDateTime FROM TrainingSession
                    """)

                        val afterCount = getTableCount(con, "TrainingSession_new")
                        println("  - New table row count: $afterCount")

                        if (beforeCount != afterCount) {
                            con.rollback()
                            throw SQLException("Data migration failed: Row count mismatch for TrainingSession. Rolling back.")
                        }

                        stmt.execute("DROP TABLE TrainingSession")

                        stmt.execute("ALTER TABLE TrainingSession_new RENAME TO TrainingSession")

                        println("Migration successful.")
                    }
                }

                println("Applying database triggers...")
                con.createStatement().use { stmt ->
                    stmt.execute("DROP TRIGGER IF EXISTS UpdateMembershipStatus;")
                    stmt.execute("""
                        CREATE TRIGGER UpdateMembershipStatus AFTER INSERT ON TrainingSession
                        FOR EACH ROW
                        BEGIN
                            UPDATE MembershipRecord
                            SET isActive = FALSE,
                                dateFinished = strftime('%Y-%m-%d', datetime('now', 'localtime'))
                            WHERE id = NEW.membershipRecordId
                              AND isActive = TRUE 
                              AND 
                              (SELECT COUNT(*) FROM TrainingSession WHERE membershipRecordId = NEW.membershipRecordId) >= 
                              COALESCE(
                                  (SELECT m.numberOfTrainingsAvailable 
                                   FROM Membership AS m
                                   JOIN MembershipRecord AS mr ON m.id = mr.membershipId
                                   WHERE mr.id = NEW.membershipRecordId),
                                  0 
                              );
                        END;
                    """)
                    println("Trigger 'UpdateMembershipStatus' created/updated successfully.")
                }
                con.commit()

            } catch (e: Exception) {
                con.rollback()
                e.printStackTrace()
            }
        }
    }

    fun getTableSchema(con: Connection, tableName: String): String? {
        con.prepareStatement("SELECT sql FROM sqlite_master WHERE type='table' AND name = ?").use { stmt ->
            stmt.setString(1, tableName)
            val rs = stmt.executeQuery()
            return if (rs.next()) rs.getString("sql") else null
        }
    }


    fun getTableCount(con: Connection, tableName: String): Long {
        con.prepareStatement("SELECT COUNT(*) FROM $tableName").use { stmt ->
            val rs = stmt.executeQuery()
            return if (rs.next()) rs.getLong(1) else 0L
        }
    }
}