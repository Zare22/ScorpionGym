package hr.kotwave.scorpiongym.otherservice

import androidx.compose.runtime.mutableStateListOf
import org.koin.core.component.KoinComponent

class OtherServiceViewModel(private val otherServiceDao: OtherServiceDao) : KoinComponent {
    private val _otherServices = mutableStateListOf<OtherService>()
    val otherServices: List<OtherService> get() = _otherServices

    init {
        getOtherServices()
    }

    private fun getOtherServices() {
        val loadedOtherServices = otherServiceDao.getAllOtherServices()
        _otherServices.addAll(loadedOtherServices)
    }

    fun addOtherService(otherService: OtherService) {
        otherServiceDao.insertOtherService(otherService)
        _otherServices.add(otherService)
    }

    fun updateOtherService(otherService: OtherService) {
        otherServiceDao.updateOtherService(otherService)
        val index = _otherServices.indexOfFirst { it.id == otherService.id }
        if (index != -1) {
            _otherServices[index] = otherService
        }
    }

    fun deleteOtherService(otherService: OtherService) {
        otherServiceDao.deleteOtherServiceById(otherService)
        _otherServices.remove(otherService)
    }
}