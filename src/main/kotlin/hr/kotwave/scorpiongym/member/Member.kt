package hr.kotwave.scorpiongym.member

import java.time.LocalDate
import java.time.LocalDateTime

data class Member(
    var id: Int = 0,
    val name: String = "",
    val surname: String = "",
    val phoneNumber: String? = "",
    val signedUpDate: LocalDateTime = LocalDateTime.now(),
    var membershipRecordId: Int? = 0,
    val organizationId: Int? = 0,
    val statusId: Int? = 0,
    val remark: String? = "",
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null
)

enum class Gender(val label: String) {
    MALE("Muško"),
    FEMALE("Žensko")
}