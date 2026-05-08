package hr.kotwave.scorpiongym.trainingsession

interface TrainingSessionDao {
    fun getAllTrainingSessions(): List<TrainingSession>
    fun getTrainingSessionById(id:Int): TrainingSession?
    fun insertTrainingSession(trainingSession: TrainingSession): Int
    fun updateTrainingSession(trainingSession: TrainingSession)
    fun deleteTrainingSession(trainingSession: TrainingSession)
    fun getAllTrainingSessionsForMembershipRecord(membershipRecordId: Int): List<TrainingSession>
}