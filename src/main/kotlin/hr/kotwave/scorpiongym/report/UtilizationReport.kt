package hr.kotwave.scorpiongym.report

/** R6 — gym utilization derived from TrainingSession.sessionDateTime over a period. */

/** Session count for one weekday. [isoDay] is 1 = Monday .. 7 = Sunday. */
data class WeekdayCount(val isoDay: Int, val count: Int)

/** Session count for one hour of the day. [hour] is 0..23. */
data class HourCount(val hour: Int, val count: Int)

/** Session count for one calendar month. [month] is an ISO "YYYY-MM" string. */
data class MonthCount(val month: String, val count: Int)

data class UtilizationReport(
    val byWeekday: List<WeekdayCount>, // always 7 entries, Monday..Sunday
    val byHour: List<HourCount>,       // hours with sessions, ascending
    val byMonth: List<MonthCount>,     // months with sessions, oldest first
) {
    val total: Int get() = byWeekday.sumOf { it.count }
}
