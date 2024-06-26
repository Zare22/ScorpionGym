package hr.kotwave.scorpiongym.member

import java.time.LocalDate
import java.time.LocalDateTime

data class Member(
    val id: Int,
    val name: String,
    val surname: String,
    val phoneNumber: String?,
    val signedUpDate: LocalDateTime,
    val membershipRecordId: Int?,
    val organizationId: Int?,
    val statusId: Int?,
    val remark: String?,
    val dateOfBirth: LocalDate?
)
