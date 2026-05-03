package hr.kotwave.scorpiongym.util

import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val LOG_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

/**
 * Writes rows to the UserActivityLog table.
 *
 * Each entry records the currently-logged-in app user (from [PreferencesHelper]),
 * the action description, and the current timestamp.
 */
class AuditLog(private val connection: Connection) {
    fun write(action: String) {
        val query = "INSERT INTO UserActivityLog(appUserId, action, dateOfAction) VALUES (?, ?, ?)"
        connection.prepareStatement(query).use { statement ->
            statement.setInt(1, PreferencesHelper().loggedInUserId!!)
            statement.setString(2, action)
            statement.setString(3, LocalDateTime.now().format(LOG_DATE_FORMATTER))
            statement.executeUpdate()
        }
    }
}
