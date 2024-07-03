package hr.kotwave.scorpiongym.trainingsession

import java.time.LocalDateTime

data class TrainingSession(
    var id: Int = 0,
    val membershipRecordId: Int,
    val sessionDateTime: LocalDateTime
)