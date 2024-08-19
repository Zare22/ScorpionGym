package hr.kotwave.scorpiongym.membershiprecord

import java.time.LocalDateTime

data class MembershipRecord(
    val id: Int,
    val memberId: Int,
    var membershipId: Int,
    var dateStarted: LocalDateTime = LocalDateTime.now(),
    var dateFinished: LocalDateTime,
    val isActive: Boolean,
    val isPaid: Boolean,
)
