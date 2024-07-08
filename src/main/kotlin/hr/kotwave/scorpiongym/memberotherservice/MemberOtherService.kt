package hr.kotwave.scorpiongym.memberotherservice

import java.time.LocalDateTime

data class MemberOtherService(
    var id: Int = 0,
    val dateOfService: LocalDateTime,
    val isPaid: Boolean,
    val memberId: Int,
    val otherServiceId: Int
)