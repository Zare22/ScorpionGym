package hr.kotwave.scorpiongym.trainingsession

class TrainingSessionRepositoryImpl(private val trainingSessionDao: TrainingSessionDao) : TrainingSessionRepository {
    override fun getAllTrainingSessions(): List<TrainingSession> = trainingSessionDao.getAllTrainingSessions()
    override fun getSessionById(id: Int): TrainingSession? = trainingSessionDao.getSessionById(id)
    override fun insertTrainingSession(trainingSession: TrainingSession) = trainingSessionDao.insertTrainingSession(trainingSession)
    override fun updateTrainingSession(trainingSession: TrainingSession) = trainingSessionDao.updateTrainingSession(trainingSession)
    override fun deleteSessionById(id: Int) = trainingSessionDao.deleteSessionById(id)
}