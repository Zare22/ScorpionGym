package hr.kotwave.scorpiongym.membershiprecord

import java.time.LocalDate

data class MembershipRecord(
    val id: Int,
    val memberId: Int,
    var membershipId: Int,
    var dateStarted: LocalDate = LocalDate.now(),
    var dateFinished: LocalDate,
    val isActive: Boolean,
    val isPaid: Boolean,
)
