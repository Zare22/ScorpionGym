package hr.kotwave.scorpiongym.report.print

import hr.kotwave.scorpiongym.report.*
import java.time.format.DateTimeFormatter

private val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")
private val WEEKDAYS = listOf("Pon", "Uto", "Sri", "Čet", "Pet", "Sub", "Ned")

private fun eur(value: Double): String = "%.2f €".format(value)
private fun pct(part: Double, total: Double): String =
    "%.1f %%".format(if (total != 0.0) part / total * 100 else 0.0)

private fun monthLabel(ym: String): String {
    val parts = ym.split("-")
    return if (parts.size == 2) "${parts[1]}.${parts[0]}." else ym
}

fun MembershipSalesReport.toPrintable(title: String, period: String): PrintableReport {
    val tableRows = rows.map { listOf(it.membershipName, it.soldCount.toString(), eur(it.netCollected)) } +
        listOf(listOf("Neregistrirane (walk-in) članarine", walkInCount.toString(), eur(walkInCollected)))
    return PrintableReport(
        title = title,
        period = period,
        tables = listOf(
            PrintableTable(
                title = null,
                columns = listOf("Tip članarine", "Prodano", "Naplaćeno"),
                rows = tableRows,
                footer = listOf("Ukupno", totalSold.toString(), eur(totalCollected)),
            ),
        ),
    )
}

fun RevenueBreakdownReport.toPrintable(title: String, period: String): PrintableReport =
    PrintableReport(
        title = title,
        period = period,
        tables = listOf(
            PrintableTable(
                title = null,
                columns = listOf("Kategorija", "Iznos", "Udio"),
                rows = rows.map { listOf(it.category.label, eur(it.netCollected), pct(it.netCollected, total)) },
                footer = listOf("Ukupno", eur(total), ""),
            ),
        ),
    )

fun RevenueOverTimeReport.toPrintable(title: String, period: String): PrintableReport =
    PrintableReport(
        title = title,
        period = period,
        tables = listOf(
            PrintableTable(
                title = null,
                columns = listOf("Mjesec", "Naplaćeno"),
                rows = rows.map { listOf(monthLabel(it.month), eur(it.netCollected)) },
                footer = listOf("Ukupno", eur(total)),
            ),
        ),
    )

fun NewMembersReport.toPrintable(title: String, period: String): PrintableReport =
    PrintableReport(
        title = title,
        period = period,
        tables = listOf(
            PrintableTable(
                title = null,
                columns = listOf("Mjesec", "Novi članovi"),
                rows = rows.map { listOf(monthLabel(it.month), it.count.toString()) },
                footer = listOf("Ukupno", total.toString()),
            ),
        ),
    )

fun OutstandingReport.toPrintable(title: String, period: String): PrintableReport =
    PrintableReport(
        title = title,
        period = period,
        tables = listOf(
            PrintableTable(
                title = null,
                columns = listOf("Član", "Stavka", "Datum", "Iznos"),
                rows = rows.map {
                    listOf(it.memberName ?: "Neregistrirano", it.description, it.date.format(DATE), eur(it.amount))
                },
                footer = listOf("Ukupno ($count)", "", "", eur(total)),
            ),
        ),
    )

fun UtilizationReport.toPrintable(title: String, period: String): PrintableReport =
    PrintableReport(
        title = title,
        period = period,
        summary = "Ukupno treninga: $total",
        tables = listOf(
            PrintableTable(
                title = "Po danu u tjednu",
                columns = listOf("Dan", "Treninga"),
                rows = byWeekday.map { listOf(WEEKDAYS[it.isoDay - 1], it.count.toString()) },
            ),
            PrintableTable(
                title = "Po satu",
                columns = listOf("Sat", "Treninga"),
                rows = byHour.map {
                    listOf(if (it.hour == 0) "bez vremena" else "%02d:00".format(it.hour), it.count.toString())
                },
            ),
            PrintableTable(
                title = "Po mjesecu",
                columns = listOf("Mjesec", "Treninga"),
                rows = byMonth.map { listOf(monthLabel(it.month), it.count.toString()) },
            ),
        ),
    )

fun DemographicsReport.toPrintable(title: String, period: String): PrintableReport =
    PrintableReport(
        title = title,
        period = period,
        summary = "Ukupno članova: $total",
        tables = listOf(
            PrintableTable(
                title = "Po spolu",
                columns = listOf("Kategorija", "Broj", "Udio"),
                rows = byGender.map { listOf(it.label, it.count.toString(), pct(it.count.toDouble(), total.toDouble())) },
            ),
            PrintableTable(
                title = "Po dobi",
                columns = listOf("Kategorija", "Broj", "Udio"),
                rows = byAgeBand.map { listOf(it.label, it.count.toString(), pct(it.count.toDouble(), total.toDouble())) },
            ),
        ),
    )
