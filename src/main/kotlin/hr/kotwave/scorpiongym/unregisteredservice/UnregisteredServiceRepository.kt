package hr.kotwave.scorpiongym.unregisteredservice

interface UnregisteredServiceRepository {
    fun getAllUnregisteredServices(): List<UnregisteredService>
    fun getUnregisteredServiceById(id:Int): UnregisteredService?
    fun insertUnregisteredService(unregisteredService: UnregisteredService): Int
    fun updateUnregisteredService(unregisteredService: UnregisteredService)
    fun deleteUnregisteredServiceById(unregisteredService: UnregisteredService)
}