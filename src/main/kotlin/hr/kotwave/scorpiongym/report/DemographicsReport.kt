package hr.kotwave.scorpiongym.report

/** One labelled bucket of R7 (gender value or age band) with its member count. */
data class DemographicRow(
    val label: String,
    val count: Int,
)

/** R7 result: the member base split by gender and by age band. */
data class DemographicsReport(
    val byGender: List<DemographicRow>,
    val byAgeBand: List<DemographicRow>,
) {
    val total: Int get() = byGender.sumOf { it.count }
}
