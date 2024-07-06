package hr.kotwave.scorpiongym.otherservice

class OtherServiceRepositoryImpl(private val otherServiceDao: OtherServiceDao) : OtherServiceRepository {
    override fun getAllOtherServices(): List<OtherService> = otherServiceDao.getAllOtherServices()
    override fun getOtherServiceById(id: Int): OtherService? = otherServiceDao.getServiceById(id)
    override fun insertOtherService(otherService: OtherService): Int = otherServiceDao.insertOtherService(otherService)
    override fun updateOtherService(otherService: OtherService) = otherServiceDao.updateOtherService(otherService)
    override fun deleteOtherServiceById(id: Int) = otherServiceDao.deleteOtherServiceById(id)
}