package hr.kotwave.scorpiongym.trainingsession

import hr.kotwave.scorpiongym.util.parseToLocalDateTime
import java.sql.Connection

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

    override fun insertTrainingSession(trainingSession: TrainingSession) {
        val query = """
            INSERT INTO TrainingSession (membershipRecordId, sessionDateTime)
            VALUES (?, ?)
        """

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, trainingSession.membershipRecordId)
            statement.setString(2, trainingSession.sessionDateTime.toString())
            statement.executeUpdate()
        }
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
    }

    override fun deleteSessionById(id: Int) {
        val query = "DELETE FROM TrainingSession WHERE id = ?"

        dbConnection.prepareStatement(query).use { statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }
}
