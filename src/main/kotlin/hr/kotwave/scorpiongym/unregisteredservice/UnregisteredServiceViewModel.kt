package hr.kotwave.scorpiongym.unregisteredservice

import androidx.compose.runtime.mutableStateListOf

class UnregisteredServiceViewModel(private val unregisteredServiceDao: UnregisteredServiceDao) {
    private val _unregisteredServices = mutableStateListOf<UnregisteredService>()
    val unregisteredServices: List<UnregisteredService> get() = _unregisteredServices

    private val modifiedUnregisteredServices = mutableListOf<UnregisteredService>()

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

    fun updateUnregisteredService(index: Int, unregisteredService: UnregisteredService) {
        if (index in _unregisteredServices.indices) {

            _unregisteredServices[index] = unregisteredService
            val existingService = modifiedUnregisteredServices.find { it.id == unregisteredService.id}

            if (existingService != null) {
                val existingIndex = modifiedUnregisteredServices.indexOf(existingService)
                modifiedUnregisteredServices[existingIndex] = unregisteredService
            } else if (unregisteredService.id != 0)
                modifiedUnregisteredServices.add(unregisteredService)
        }
    }

    fun confirmUnregisteredServicesUpdates() {
        _unregisteredServices.forEach { unregisteredService ->
            if(unregisteredService.id == 0)
                unregisteredServiceDao.insertUnregisteredService(unregisteredService)
        }
        modifiedUnregisteredServices.forEach { unregisteredService ->
            if (unregisteredService.id != 0)
                unregisteredServiceDao.updateUnregisteredService(unregisteredService)
        }
        _unregisteredServices.clear()
        modifiedUnregisteredServices.clear()
        getUnregisteredServices()
    }
}