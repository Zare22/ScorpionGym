package hr.kotwave.scorpiongym.otherservice

interface OtherServiceDao {
    fun getAllOtherServices(): List<OtherService>
    fun getOtherServiceById(id:Int): OtherService?
    fun insertOtherService(otherService: OtherService): Int
    fun updateOtherService(otherService: OtherService)
    fun deleteOtherService(otherService: OtherService)
}