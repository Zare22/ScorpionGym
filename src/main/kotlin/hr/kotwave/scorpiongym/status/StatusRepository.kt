package hr.kotwave.scorpiongym.status

interface StatusRepository {
    fun getAllStatuses(): List<Status>
    fun getStatusById(id:Int): Status?
    fun insertStatus(status: Status)
    fun updateStatus(status: Status)
    fun deleteStatusId(id: Int)
}