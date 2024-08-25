package hr.kotwave.scorpiongym.unregisteredservice

import androidx.compose.runtime.mutableStateListOf

class UnregisteredServiceViewModel(private val unregisteredServiceDao: UnregisteredServiceDao) {
    private val _unregisteredServices = mutableStateListOf<UnregisteredService>()
    val unregisteredServices: List<UnregisteredService> get() = _unregisteredServices

    init {
        getUnregisteredServices()
    }

    private fun getUnregisteredServices() {
        val loadedUnregisteredService = unregisteredServiceDao.getAllUnregisteredServices()
        _unregisteredServices.addAll(loadedUnregisteredService)
    }

    fun addUnregisteredService(unregisteredServices: UnregisteredService) {
        unregisteredServiceDao.insertUnregisteredService(unregisteredServices)
        _unregisteredServices.add(unregisteredServices)
    }

    fun deleteUnregisteredService(unregisteredServices: UnregisteredService) {
        unregisteredServiceDao.deleteUnregisteredService(unregisteredServices)
        _unregisteredServices.remove(unregisteredServices)
    }
}