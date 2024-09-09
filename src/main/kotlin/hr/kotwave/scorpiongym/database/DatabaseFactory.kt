package hr.kotwave.scorpiongym.database

import java.nio.file.FileSystems
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

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
            val dbPath = "$userHome${FileSystems.getDefault().separator}ScorpionGym${FileSystems.getDefault().separator}gymdatabase.db"
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
                con.createStatement().use { statement ->
                    statement.execute("DROP TRIGGER IF EXISTS UpdateMembershipStatus;")
                    statement.execute(
                    """
                            CREATE TRIGGER UpdateMembershipStatus AFTER INSERT ON TrainingSession
                            FOR EACH ROW
                            BEGIN
                                UPDATE MembershipRecord
                                SET isActive = CASE
                                                   WHEN (SELECT COUNT(*) FROM TrainingSession WHERE membershipRecordId = NEW.membershipRecordId) >=
                                                        (SELECT numberOfTrainingsAvailable FROM Membership WHERE id = (SELECT membershipId FROM MembershipRecord WHERE id = NEW.membershipRecordId))
                                                       THEN FALSE
                                                   ELSE isActive
                                               END,
                                    dateFinished = CASE
                                                       WHEN (SELECT COUNT(*) FROM TrainingSession WHERE membershipRecordId = NEW.membershipRecordId) >=
                                                            (SELECT numberOfTrainingsAvailable FROM Membership WHERE id = (SELECT membershipId FROM MembershipRecord WHERE id = NEW.membershipRecordId))
                                                           THEN strftime('%Y-%m-%d', datetime('now', 'localtime'))
                                                       ELSE dateFinished
                                                   END
                                WHERE id = NEW.membershipRecordId;
                            END;
                        """
                    )
                    statement.execute(
                    """
                            UPDATE MembershipRecord
                            SET dateFinished = strftime('%Y-%m-%d', dateFinished)
                            WHERE dateFinished IS NOT NULL;
                        """
                    )
                }
                con.commit()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
}