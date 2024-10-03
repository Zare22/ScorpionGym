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

    fun initDB() {
        val connection = connect()
        connection?.use { con ->
            con.autoCommit = false
            try {
                con.createStatement().use { statement ->
                    statement.execute("CREATE TABLE IF NOT EXISTS CurrentSessionUser (currentAppUserId INTEGER)")
                    val resultSet = statement.executeQuery("SELECT COUNT(*) FROM CurrentSessionUser")
                    var rowCount = 0
                    if (resultSet.next()) { rowCount = resultSet.getInt(1) }
                    if (rowCount == 0) { statement.execute("INSERT INTO CurrentSessionUser (currentAppUserId) VALUES (NULL)") }
                    statement.execute(
                        """CREATE TABLE IF NOT EXISTS PaymentAuditLog (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            membershipRecordId INTEGER,
                            memberOtherServiceId INTEGER,
                            unregisteredServiceId INTEGER,
                            isPaidOld BOOLEAN NOT NULL,
                            isPaidNew BOOLEAN NOT NULL,
                            price DECIMAL(10, 2) NOT NULL,
                            changedAt DATETIME NOT NULL,
                            loggedInUserId INTEGER NOT NULL,
                            FOREIGN KEY (membershipRecordId) REFERENCES MembershipRecord(id) ON DELETE CASCADE,
                            FOREIGN KEY (memberOtherServiceId) REFERENCES MemberOtherService(id) ON DELETE CASCADE,
                            FOREIGN KEY (unregisteredServiceId) REFERENCES UnregisteredService(id) ON DELETE CASCADE,
                            FOREIGN KEY (loggedInUserId) REFERENCES AppUser(id) ON DELETE CASCADE
                        );"""
                    )
                    statement.execute(
                        """CREATE TRIGGER IF NOT EXISTS LogMembershipRecordPaymentUpdate
                            AFTER UPDATE OF isPaid ON MembershipRecord
                            FOR EACH ROW
                            WHEN OLD.isPaid != NEW.isPaid
                            BEGIN
                                INSERT INTO PaymentAuditLog (
                                    membershipRecordId, memberOtherServiceId, unregisteredServiceId, 
                                    isPaidOld, isPaidNew, price, changedAt, loggedInUserId
                                )
                                VALUES (
                                    OLD.id, NULL, NULL, 
                                    OLD.isPaid, NEW.isPaid, 
                                    (SELECT price FROM Membership WHERE id = OLD.membershipId), 
                                    strftime('%Y-%m-%d', 'now', 'localtime'),
                                    (SELECT currentAppUserId FROM CurrentSessionUser)
                                );
                            END;"""
                    )
                    statement.execute(
                        """CREATE TRIGGER IF NOT EXISTS LogMembershipRecordPaymentInsert
                            AFTER INSERT ON MembershipRecord
                            FOR EACH ROW
                            WHEN NEW.isPaid = 1
                            BEGIN
                                INSERT INTO PaymentAuditLog (
                                    membershipRecordId, memberOtherServiceId, unregisteredServiceId, 
                                    isPaidOld, isPaidNew, price, changedAt, loggedInUserId
                                )
                                VALUES (
                                    NEW.id, NULL, NULL, 
                                    0, NEW.isPaid,
                                    (SELECT price FROM Membership WHERE id = NEW.membershipId), 
                                    strftime('%Y-%m-%d', 'now', 'localtime'),
                                    (SELECT currentAppUserId FROM CurrentSessionUser)
                                );
                            END;"""
                    )

                    statement.execute(
                        """CREATE TRIGGER IF NOT EXISTS LogOtherServicePaymentUpdate
                            AFTER UPDATE OF isPaid ON MemberOtherService
                            FOR EACH ROW
                            WHEN OLD.isPaid != NEW.isPaid
                            BEGIN
                                INSERT INTO PaymentAuditLog (
                                    membershipRecordId, memberOtherServiceId, unregisteredServiceId, 
                                    isPaidOld, isPaidNew, price, changedAt, loggedInUserId
                                )
                                VALUES (
                                    NULL, OLD.id, NULL, 
                                    OLD.isPaid, NEW.isPaid, 
                                    (SELECT price FROM OtherService WHERE id = OLD.otherServiceId), 
                                    strftime('%Y-%m-%d', 'now', 'localtime'),
                                    (SELECT currentAppUserId FROM CurrentSessionUser)
                                );
                            END;"""
                    )
                    statement.execute(
                        """CREATE TRIGGER IF NOT EXISTS LogOtherServicePaymentInsert
                            AFTER INSERT ON MemberOtherService
                            FOR EACH ROW
                            WHEN NEW.isPaid = 1
                            BEGIN
                                INSERT INTO PaymentAuditLog (
                                    membershipRecordId, memberOtherServiceId, unregisteredServiceId, 
                                    isPaidOld, isPaidNew, price, changedAt, loggedInUserId
                                )
                                VALUES (
                                    NULL, NEW.id, NULL, 
                                    0, NEW.isPaid, 
                                    (SELECT price FROM OtherService WHERE id = NEW.otherServiceId), 
                                    strftime('%Y-%m-%d', 'now', 'localtime'),
                                    (SELECT currentAppUserId FROM CurrentSessionUser)
                                );
                            END;"""
                    )

                    statement.execute(
                    """CREATE TRIGGER IF NOT EXISTS LogUnregisteredServicePaymentUpdate
                        AFTER UPDATE OF isPaid ON UnregisteredService
                        FOR EACH ROW
                        WHEN OLD.isPaid != NEW.isPaid
                        BEGIN
                            INSERT INTO PaymentAuditLog (
                                membershipRecordId, memberOtherServiceId, unregisteredServiceId, 
                                isPaidOld, isPaidNew, price, changedAt, loggedInUserId
                            )
                            VALUES (
                                NULL, NULL, OLD.id, 
                                OLD.isPaid, NEW.isPaid, 
                                COALESCE(
                                    (SELECT price FROM OtherService WHERE id = OLD.otherServiceId),
                                    (SELECT price FROM Membership WHERE id = OLD.membershipId)
                                ),
                                strftime('%Y-%m-%d', 'now', 'localtime'),
                                (SELECT currentAppUserId FROM CurrentSessionUser)
                            );
                        END;"""
                    )
                    statement.execute(
                    """CREATE TRIGGER IF NOT EXISTS LogUnregisteredServicePaymentInsert
                        AFTER INSERT ON UnregisteredService
                        FOR EACH ROW
                        WHEN NEW.isPaid = 1
                        BEGIN
                            INSERT INTO PaymentAuditLog (
                                membershipRecordId, memberOtherServiceId, unregisteredServiceId, 
                                isPaidOld, isPaidNew, price, changedAt, loggedInUserId
                            )
                            VALUES (
                                NULL, NULL, NEW.id, 
                                0, NEW.isPaid,
                                COALESCE(
                                    (SELECT price FROM OtherService WHERE id = NEW.otherServiceId),
                                    (SELECT price FROM Membership WHERE id = NEW.membershipId)
                                ),
                                strftime('%Y-%m-%d', 'now', 'localtime'),
                                (SELECT currentAppUserId FROM CurrentSessionUser)
                            );
                        END;"""
                    )
                }
                con.commit()
            } catch (e: Exception) {
                con.rollback()
                e.printStackTrace()
            }
        }
    }
}