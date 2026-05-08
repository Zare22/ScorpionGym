package hr.kotwave.scorpiongym.appuser

import java.time.LocalDateTime

/**
 * One row from `UserActivityLog`, joined with the [AppUser] who performed it.
 *
 * The DAO returns these as typed values (with [LocalDateTime] timestamps) so
 * callers can filter on dates directly instead of parsing display strings.
 */
data class ActivityLogEntry(
    val action: String,
    val timestamp: LocalDateTime,
    val username: String,
)
