package hr.kotwave.scorpiongym.unregisteredservice

import androidx.compose.runtime.mutableStateListOf

class UnregisteredServiceViewModel(private val unregisteredServiceDao: UnregisteredServiceDao) {
    private val _unregisteredServices = mutableStateListOf<UnregisteredService>()
    val unregisteredServices: List<UnregisteredService> get() = _unregisteredServices

    private val modifiedUnregisteredOtherServices = mutableListOf<UnregisteredService>()

    init {
        getUnregisteredServices()
    }

    private fun getUnregisteredServices() {
        val loadedUnregisteredService = unregisteredServiceDao.getAllUnregisteredServices()
        _unregisteredServices.addAll(loadedUnregisteredService)
    }

    fun addUnregisteredService(unregisteredServices: UnregisteredService) {
        unregisteredServices.id = unregisteredServiceDao.insertUnregisteredService(unregisteredServices)
        _unregisteredServices.add(unregisteredServices)
    }

    fun deleteUnregisteredService(unregisteredServices: UnregisteredService) {
        unregisteredServiceDao.deleteUnregisteredService(unregisteredServices)
        _unregisteredServices.remove(unregisteredServices)
    }

    fun updateUnregisteredOtherService(index: Int, unregisteredService: UnregisteredService) {
        if (index in _unregisteredServices.indices) {

            _unregisteredServices[index] = unregisteredService
            val existingService = modifiedUnregisteredOtherServices.find { it.id == unregisteredService.id}

            if (existingService != null) {
                val existingIndex = modifiedUnregisteredOtherServices.indexOf(existingService)
                modifiedUnregisteredOtherServices[existingIndex] = unregisteredService
            } else if (unregisteredService.id != 0)
                modifiedUnregisteredOtherServices.add(unregisteredService)
        }
    }

    fun confirmUnregisteredOtherServicesUpdates() {
        _unregisteredServices.forEach { unregisteredService ->
            if(unregisteredService.id == 0)
                unregisteredServiceDao.insertUnregisteredService(unregisteredService)
        }
        modifiedUnregisteredOtherServices.forEach { unregisteredService ->
            if (unregisteredService.id != 0)
                unregisteredServiceDao.updateUnregisteredService(unregisteredService)
        }
        _unregisteredServices.clear()
        modifiedUnregisteredOtherServices.clear()
        getUnregisteredServices()
    }
}