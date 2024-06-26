package hr.kotwave.scorpiongym.status

import org.koin.core.component.KoinComponent

class StatusViewModel(private val statusDao: StatusDao) : KoinComponent {
    fun getAllStatuses() = statusDao.getAllStatuses()
}