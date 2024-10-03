package hr.kotwave.scorpiongym.unregisteredservice

import java.time.LocalDateTime

data class UnregisteredService(
    var id: Int = 0,
    val dateOfService: LocalDateTime,
    val isPaid: Boolean,
    var membershipId: Int? = null,
    var otherServiceId: Int? = null
)