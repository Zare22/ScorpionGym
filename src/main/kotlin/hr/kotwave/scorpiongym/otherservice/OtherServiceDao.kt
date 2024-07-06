package hr.kotwave.scorpiongym.otherservice

interface OtherServiceDao {
    fun getAllOtherServices(): List<OtherService>
    fun getServiceById(id:Int): OtherService?
    fun insertOtherService(otherService: OtherService): Int
    fun updateOtherService(otherService: OtherService)
    fun deleteOtherServiceById(id: Int)
}