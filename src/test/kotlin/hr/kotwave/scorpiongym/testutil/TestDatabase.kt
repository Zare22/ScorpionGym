package hr.kotwave.scorpiongym.testutil

import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.atomic.AtomicBoolean
import java.util.prefs.Preferences

/**
 * Creates a fresh in-memory SQLite connection with the production schema applied
 * (from src/main/resources/schema.sql) and a minimal baseline seed so that
 * trigger-touching inserts can succeed without per-test bootstrapping.
 *
 * Baseline rows seeded (all with id = 1):
 * - TypeOfOrganization, Organization, Status (required by Member FK constraints)
 * - AppUser, CurrentSessionUser (required by payment-audit triggers, which read
 *   `(SELECT currentAppUserId FROM CurrentSessionUser)` and write a NOT NULL FK
 *   into PaymentAuditLog.loggedInUserId)
 *
 * The first call also installs a test "logged-in user id" in JVM Preferences
 * (snapshotting and restoring the original on JVM exit) because `AuditLog`
 * reads `PreferencesHelper().loggedInUserId!!` whenever a DAO write happens.
 *
 * Each call returns a NEW database. Tests must close the connection in tearDown.
 */
fun createTestDatabase(): Connection {
    ensureTestPreferencesInstalled()
    val connection = DriverManager.getConnection("jdbc:sqlite::memory:")
    connection.createStatement().use { it.execute("PRAGMA foreign_keys = ON") }
    applySchema(connection)
    seedBaseline(connection)
    return connection
}

private const val PREFERENCES_NODE = "hr.kotwave.scorpiongym.util.PreferencesHelper"
private const val USER_ID_KEY = "logged_in_user_id"
private const val TEST_USER_ID = 1
private val preferencesInstalled = AtomicBoolean(false)

/**
 * Idempotent. Backs up the real `logged_in_user_id` pref, overwrites it with the
 * test user id (matching the seeded `AppUser.id = 1`), and registers a JVM
 * shutdown hook that restores the original value. This keeps tests from
 * permanently clobbering the developer's logged-in state.
 *
 * Caveat: if the JVM is killed forcefully (e.g. `kill -9`), the shutdown hook
 * does not run. Re-running any test suite afterwards re-snapshots cleanly, so
 * the worst-case is the developer being silently "logged in as user 1" until
 * the next test run.
 */
private fun ensureTestPreferencesInstalled() {
    if (!preferencesInstalled.compareAndSet(false, true)) return
    val prefs = Preferences.userRoot().node(PREFERENCES_NODE)
    val snapshot = prefs.getInt(USER_ID_KEY, -1)
    prefs.putInt(USER_ID_KEY, TEST_USER_ID)
    Runtime.getRuntime().addShutdownHook(Thread {
        if (snapshot != -1) prefs.putInt(USER_ID_KEY, snapshot)
        else prefs.remove(USER_ID_KEY)
    })
}

private fun applySchema(connection: Connection) {
    val schemaSql = Thread.currentThread().contextClassLoader
        .getResourceAsStream("schema.sql")
        ?.bufferedReader()
        ?.use { it.readText() }
        ?: error("schema.sql not found on test classpath")

    connection.createStatement().use { statement ->
        for (sql in splitSqlStatements(schemaSql)) {
            if (sql.isNotBlank()) statement.execute(sql)
        }
    }
}

/**
 * Splits a SQL script into individual statements, respecting BEGIN/END trigger
 * bodies (their inner `;` characters do NOT terminate the outer statement).
 *
 * Strategy: strip `--` line comments first, then scan for BEGIN/END/`;` tokens.
 * Maintain a depth counter; only treat `;` as a terminator when depth == 0.
 *
 * Known limitation: does not handle BEGIN/END appearing inside string literals.
 * schema.sql contains none, so this is fine for this project.
 */
internal fun splitSqlStatements(sql: String): List<String> {
    val stripped = sql.lineSequence()
        .map { line ->
            val commentStart = line.indexOf("--")
            if (commentStart >= 0) line.substring(0, commentStart) else line
        }
        .joinToString("\n")

    val statements = mutableListOf<String>()
    val current = StringBuilder()
    var depth = 0
    val tokenPattern = Regex("\\bBEGIN\\b|\\bEND\\b|;", RegexOption.IGNORE_CASE)
    var cursor = 0

    for (match in tokenPattern.findAll(stripped)) {
        current.append(stripped, cursor, match.range.first)
        val token = match.value.uppercase()
        when (token) {
            "BEGIN" -> {
                current.append(match.value)
                depth++
            }
            "END" -> {
                current.append(match.value)
                depth--
            }
            ";" -> {
                if (depth == 0) {
                    statements.add(current.toString().trim())
                    current.clear()
                } else {
                    current.append(';')
                }
            }
        }
        cursor = match.range.last + 1
    }
    current.append(stripped, cursor, stripped.length)
    val tail = current.toString().trim()
    if (tail.isNotEmpty()) statements.add(tail)

    return statements
}

private fun seedBaseline(connection: Connection) {
    connection.createStatement().use { statement ->
        statement.executeUpdate(
            "INSERT INTO TypeOfOrganization (id, name) VALUES (1, 'TestOrgType')"
        )
        statement.executeUpdate(
            "INSERT INTO Organization (id, name, typeOfOrganizationId) VALUES (1, 'TestOrg', 1)"
        )
        statement.executeUpdate(
            "INSERT INTO Status (id, description) VALUES (1, 'Aktivan')"
        )
        statement.executeUpdate(
            "INSERT INTO AppUser (id, username, password, isAdmin) VALUES (1, 'testuser', 'testpass', 0)"
        )
        statement.executeUpdate(
            "INSERT INTO CurrentSessionUser (currentAppUserId) VALUES (1)"
        )
    }
}
