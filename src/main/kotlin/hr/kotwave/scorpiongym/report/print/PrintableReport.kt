package hr.kotwave.scorpiongym.report.print

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Presentation-agnostic shape every report maps to for printing. Cells are
 * already-formatted strings so the HTML matches what's on screen. The first
 * column is treated as a label (left-aligned); the rest are right-aligned.
 */
data class PrintableTable(
    val title: String?,
    val columns: List<String>,
    val rows: List<List<String>>,
    val footer: List<String>? = null,
)

data class PrintableReport(
    val title: String,
    val period: String,
    val summary: String? = null,
    val tables: List<PrintableTable>,
)

private val DAY_MONTH_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")

/** Human-readable period label for a (possibly open) date range. */
fun formatPeriod(from: LocalDate?, to: LocalDate?): String = when {
    from != null && to != null -> "${from.format(DAY_MONTH_YEAR)} – ${to.format(DAY_MONTH_YEAR)}"
    from != null -> "od ${from.format(DAY_MONTH_YEAR)}"
    to != null -> "do ${to.format(DAY_MONTH_YEAR)}"
    else -> "Sva razdoblja"
}
