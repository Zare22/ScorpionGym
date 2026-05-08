package hr.kotwave.scorpiongym.otherservice

import hr.kotwave.scorpiongym.util.CrudViewModel

class OtherServiceViewModel(dao: OtherServiceDao) : CrudViewModel<OtherService>(
    loader = dao::getAllOtherServices,
    inserter = dao::insertOtherService,
    updater = dao::updateOtherService,
    deleter = dao::deleteOtherService,
) {
    val otherServices: List<OtherService> get() = items

    fun addOtherService(otherService: OtherService) = add(otherService)
    fun updateOtherService(otherService: OtherService) = update(otherService)
    fun deleteOtherService(otherService: OtherService) = delete(otherService)
}
