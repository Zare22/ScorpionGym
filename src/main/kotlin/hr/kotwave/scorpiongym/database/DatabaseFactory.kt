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
            DriverManager.getConnection(url)
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
                    statement.execute(
                        """
                        CREATE TABLE IF NOT EXISTS Member (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            surname TEXT NOT NULL,
                            phoneNumber TEXT NOT NULL,
                            signedUpDate DATE NOT NULL,
                            membershipRecordId INTEGER,
                            organizationId INTEGER,
                            statusId INTEGER,
                            remark TEXT,
                            FOREIGN KEY (membershipRecordId) REFERENCES MembershipRecord(id),
                            FOREIGN KEY (organizationId) REFERENCES Organization(id),
                            FOREIGN KEY (statusId) REFERENCES Status(id)
                        )
                    """
                    )
                    statement.execute(
                        """
                        CREATE TABLE IF NOT EXISTS MembershipRecord (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            memberId INTEGER NOT NULL,
                            membershipId INTEGER NOT NULL,
                            dateStarted DATE NOT NULL,
                            dateFinished DATE,
                            isActive BOOLEAN NOT NULL DEFAULT TRUE,
                            FOREIGN KEY (memberId) REFERENCES Member(id),
                            FOREIGN KEY (membershipId) REFERENCES Membership(id)
                        )
                    """
                    )
                    statement.execute(
                        """
                        CREATE TABLE IF NOT EXISTS Membership (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            price DECIMAL(10, 2) NOT NULL,
                            numberOfTrainingsAvailable INTEGER NOT NULL
                        )
                    """
                    )
                    statement.execute(
                        """
                        CREATE TABLE IF NOT EXISTS Organization (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            typeOfOrganizationId INTEGER NOT NULL,
                            FOREIGN KEY (typeOfOrganizationId) REFERENCES TypeOfOrganization(id)
                        )
                    """
                    )
                    statement.execute(
                        """
                        CREATE TABLE IF NOT EXISTS TypeOfOrganization (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            discountRate DECIMAL(3, 2) DEFAULT 0.00
                        )
                    """
                    )
                    statement.execute(
                        """
                        CREATE TABLE IF NOT EXISTS Status (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            description TEXT NOT NULL
                        )
                    """
                    )
                    statement.execute(
                        """
                        CREATE TABLE IF NOT EXISTS TrainingSession (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            membershipRecordId INTEGER NOT NULL,
                            sessionDateTime DATETIME NOT NULL,
                            FOREIGN KEY (membershipRecordId) REFERENCES MembershipRecord(id)
                        )
                    """
                    )
                    statement.execute(
                        """
                        CREATE TABLE IF NOT EXISTS OtherServices (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            price DECIMAL(10, 2) NOT NULL
                        )
                    """
                    )
                    statement.execute(
                        """
                        CREATE TABLE IF NOT EXISTS MemberOtherServices (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            dateOfService DATETIME NOT NULL,
                            memberId INTEGER NOT NULL,
                            otherServiceId INTEGER NOT NULL,
                            FOREIGN KEY (memberId) REFERENCES Member(id),
                            FOREIGN KEY (otherServiceId) REFERENCES OtherServices(id)
                        )
                    """
                    )
                    statement.execute(
                        """
                        CREATE TRIGGER IF NOT EXISTS UpdateMembershipStatus AFTER INSERT ON TrainingSession
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
                                    THEN CURRENT_DATE
                                    ELSE dateFinished
                                END
                                WHERE id = NEW.membershipRecordId;
                            END
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