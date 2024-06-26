package hr.kotwave.scorpiongym.otherservice

interface OtherServiceRepository {
    fun getAllOtherServices(): List<OtherService>
    fun getOtherServiceById(id:Int): OtherService?
    fun insertOtherService(otherService: OtherService)
    fun updateOtherService(otherService: OtherService)
    fun deleteOtherServiceById(id: Int)
}