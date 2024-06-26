package hr.kotwave.scorpiongym.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun parseToLocalDateTime(dateString: String): LocalDateTime {
    return try {
        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } catch (e: Exception) {
        LocalDateTime.parse("${dateString}T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

fun parseToLocalDate(dateString: String): LocalDate? {
    val formatter = DateTimeFormatter.ofPattern("d.M.yyyy")
    return dateString.trimEnd('.').let {
        LocalDate.parse(it, formatter)
    }
}