package hr.kotwave.scorpiongym.status

class StatusRepositoryImpl(private val statusDao: StatusDao) : StatusRepository {
    override fun getAllStatuses(): List<Status> = statusDao.getAllStatuses()
    override fun getStatusById(id: Int): Status? = statusDao.getStatusById(id)
    override fun insertStatus(status: Status) = statusDao.insertStatus(status)
    override fun updateStatus(status: Status) = statusDao.updateStatus(status)
    override fun deleteStatusId(id: Int) = statusDao.deleteStatusById(id)
}