package hr.kotwave.scorpiongym.trainingsession

import hr.kotwave.scorpiongym.util.PreferencesHelper
import hr.kotwave.scorpiongym.util.parseToLocalDateTime
import java.sql.Connection
import java.sql.SQLException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TrainingSessionDaoImpl(private val dbConnection: Connection) : TrainingSessionDao {

    override fun getAllTrainingSessions(): List<TrainingSession> {
        val sessions = mutableListOf<TrainingSession>()
        val query = "SELECT * FROM TrainingSession"

        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val session = TrainingSession(
                    id = resultSet.getInt("id"),
                    membershipRecordId = resultSet.getInt("membershipRecordId"),
                    sessionDateTime = parseToLocalDateTime(resultSet.getString("sessionDateTime"))
                )
                sessions.add(session)
            }
        }

        return sessions
    }

    override fun getSessionById(id: Int): TrainingSession? {
        val query = "SELECT * FROM TrainingSession WHERE id = ?"
        var trainingSession: TrainingSession? = null

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                trainingSession = TrainingSession(
                    id = resultSet.getInt("id"),
                    membershipRecordId = resultSet.getInt("membershipRecordId"),
                    sessionDateTime = parseToLocalDateTime(resultSet.getString("sessionDateTime"))
                )
            }
        }

        return trainingSession
    }

    override fun insertTrainingSession(trainingSession: TrainingSession): Int {
        val query = """
            INSERT INTO TrainingSession (membershipRecordId, sessionDateTime)
            VALUES (?, ?)
            RETURNING id
        """

        var insertedId: Int
        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, trainingSession.membershipRecordId)
            statement.setString(2, trainingSession.sessionDateTime.toString())

            val resultSet = statement.executeQuery()
            insertedId = resultSet.takeIf { it.next() }?.getInt(1)
                ?: throw SQLException("ID treninga se nije kreirao!")

        }
        logActionOnTrainingSession(insertedId, "Unesen je novi trening")
        return insertedId
    }

    override fun updateTrainingSession(trainingSession: TrainingSession) {
        val query = """
            UPDATE TrainingSession SET sessionDateTime = ?
            WHERE id = ?
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setString(1, trainingSession.sessionDateTime.toString())
            statement.setInt(2, trainingSession.id)
            statement.executeUpdate()
        }
        logActionOnTrainingSession(trainingSession.id, "Ažuriran je trening")
    }

    override fun deleteSessionById(trainingSession: TrainingSession) {
        val query = "DELETE FROM TrainingSession WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, trainingSession.id)
            statement.executeUpdate()
        }
        logActionOnTrainingSession(trainingSession.id, "Pobrisan je trening")
    }

    override fun getAllTrainingSessionsForMembershipRecord(membershipRecordId: Int): List<TrainingSession> {
        val sessions = mutableListOf<TrainingSession>()
        val query = "SELECT * FROM TrainingSession WHERE membershipRecordId = ? ORDER BY sessionDateTime DESC"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, membershipRecordId)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val session = TrainingSession(
                    id = resultSet.getInt("id"),
                    membershipRecordId = resultSet.getInt("membershipRecordId"),
                    sessionDateTime = parseToLocalDateTime(resultSet.getString("sessionDateTime"))
                )
                sessions.add(session)
            }
        }

        return sessions
    }

    private fun fetchMemberAndMembershipDetails(trainingSessionId: Int): Triple<String, String, String> {
        val detailsQuery = """
            SELECT m.name AS memberName, m.surname AS memberSurname, ms.name AS membershipName
            FROM TrainingSession ts
            JOIN MembershipRecord mr ON ts.membershipRecordId = mr.id
            JOIN Member m ON mr.memberId = m.id
            JOIN Membership ms ON mr.membershipId = ms.id
            WHERE ts.id = ?
        """

        dbConnection.prepareStatement(detailsQuery).use { statement ->
            statement.setInt(1, trainingSessionId)
            val resultSet = statement.executeQuery()

            if (resultSet.next()) {
                val memberName = resultSet.getString("memberName")
                val memberSurname = resultSet.getString("memberSurname")
                val membershipName = resultSet.getString("membershipName")
                return Triple(memberName, memberSurname, membershipName)
            } else {
                throw SQLException("Nije moguće dohvatiti detalje za člana i članarinu.")
            }
        }
    }


    private fun logActionOnTrainingSession(trainingSessionId: Int, actionDescription: String) {
        val (memberName, memberSurname, membershipName) = fetchMemberAndMembershipDetails(trainingSessionId)
        val logText = "$actionDescription za člana $memberName $memberSurname u članarini $membershipName"

        val query = "INSERT INTO UserActivityLog(appUserId, action, dateOfAction) VALUES (?, ?, ?)"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, PreferencesHelper().loggedInUserId!!)
            statement.setString(2, logText)
            statement.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            statement.executeUpdate()
        }
    }

}
