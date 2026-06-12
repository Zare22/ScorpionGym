package hr.kotwave.scorpiongym.report

/** One month bucket of R4 (new members per month). [month] is an ISO "YYYY-MM" string. */
data class NewMembersRow(
    val month: String,
    val count: Int,
)

/** R4 result: count of members who signed up per calendar month, oldest first. */
data class NewMembersReport(
    val rows: List<NewMembersRow>,
) {
    val total: Int get() = rows.sumOf { it.count }
}
