package hr.kotwave.scorpiongym.trainingsession

interface TrainingSessionRepository {
    fun getAllTrainingSessions(): List<TrainingSession>
    fun getSessionById(id:Int): TrainingSession?
    fun insertTrainingSession(trainingSession: TrainingSession): Int
    fun updateTrainingSession(trainingSession: TrainingSession)
    fun deleteSessionById(trainingSession: TrainingSession)
}