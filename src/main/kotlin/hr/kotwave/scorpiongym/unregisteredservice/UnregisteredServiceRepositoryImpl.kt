package hr.kotwave.scorpiongym.unregisteredservice

class UnregisteredServiceRepositoryImpl(private val unregisteredServiceDao: UnregisteredServiceDao) : UnregisteredServiceRepository {
    override fun getAllUnregisteredServices(): List<UnregisteredService> = unregisteredServiceDao.getAllUnregisteredServices()
    override fun getUnregisteredServiceById(id: Int): UnregisteredService? = unregisteredServiceDao.getUnregisteredServiceById(id)
    override fun insertUnregisteredService(unregisteredService: UnregisteredService): Int = unregisteredServiceDao.insertUnregisteredService(unregisteredService)
    override fun updateUnregisteredService(unregisteredService: UnregisteredService) = unregisteredServiceDao.updateUnregisteredService(unregisteredService)
    override fun deleteUnregisteredServiceById(unregisteredService: UnregisteredService) = unregisteredServiceDao.deleteUnregisteredService(unregisteredService)
}