package hr.kotwave.scorpiongym.membershiprecord

import java.time.LocalDateTime

data class MembershipRecord(
    val id: Int,
    val memberId: Int,
    val membershipId: Int,
    val dateStarted: LocalDateTime = LocalDateTime.now(),
    val dateFinished: LocalDateTime = LocalDateTime.now().plusMonths(1),
    val isActive: Boolean,
    val isPaid: Boolean,
)
