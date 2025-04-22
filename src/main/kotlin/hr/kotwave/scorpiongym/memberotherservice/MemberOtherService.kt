package hr.kotwave.scorpiongym.memberotherservice

import java.time.LocalDateTime

data class MemberOtherService(
    var id: Int = 0,
    var dateOfService: LocalDateTime,
    val isPaid: Boolean,
    var memberId: Int,
    var otherServiceId: Int
)