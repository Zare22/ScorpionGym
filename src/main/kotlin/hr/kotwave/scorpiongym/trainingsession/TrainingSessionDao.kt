package hr.kotwave.scorpiongym.trainingsession

interface TrainingSessionDao {
    fun getAllTrainingSessions(): List<TrainingSession>
    fun getSessionById(id:Int): TrainingSession?
    fun insertTrainingSession(trainingSession: TrainingSession): Int
    fun updateTrainingSession(trainingSession: TrainingSession)
    fun deleteSessionById(id: Int)
    fun getAllTrainingSessionsForMembershipRecord(membershipRecordId: Int): List<TrainingSession>
}