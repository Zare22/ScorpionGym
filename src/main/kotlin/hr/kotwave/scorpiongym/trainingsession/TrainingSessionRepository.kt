package hr.kotwave.scorpiongym.trainingsession

interface TrainingSessionRepository {
    fun getAllTrainingSessions(): List<TrainingSession>
    fun getSessionById(id:Int): TrainingSession?
    fun insertTrainingSession(trainingSession: TrainingSession)
    fun updateTrainingSession(trainingSession: TrainingSession)
    fun deleteSessionById(id: Int)
}