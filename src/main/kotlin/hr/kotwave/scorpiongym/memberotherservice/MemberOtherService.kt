package hr.kotwave.scorpiongym.memberotherservice

import java.time.LocalDateTime

data class MemberOtherService(
    val id: Int = 0,
    val dateOfService: LocalDateTime,
    val memberId: Int,
    val otherServiceId: Int
)