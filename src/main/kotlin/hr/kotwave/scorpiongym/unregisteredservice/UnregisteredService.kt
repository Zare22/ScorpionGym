package hr.kotwave.scorpiongym.unregisteredservice

import java.time.LocalDateTime

data class UnregisteredService(
    var id: Int = 0,
    val dateOfService: LocalDateTime,
    val isPaid: Boolean,
    val membershipId: Int? = null,
    val otherServiceId: Int? = null
)