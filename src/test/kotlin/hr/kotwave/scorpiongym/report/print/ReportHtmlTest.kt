package hr.kotwave.scorpiongym.report.print

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/** Pure-renderer tests for the report HTML (no DB needed). */
class ReportHtmlTest {

    @Test
    fun `renders header, summary, table headings, footer and escapes text`() {
        val report = PrintableReport(
            title = "Test & Report",
            period = "Sva razdoblja",
            summary = "Ukupno: 3",
            tables = listOf(
                PrintableTable(
                    title = "Tablica",
                    columns = listOf("Naziv", "Iznos"),
                    rows = listOf(listOf("x <b>", "1,00 €")),
                    footer = listOf("Ukupno", "1,00 €"),
                ),
            ),
        )

        val html = renderReportHtml(report)

        assertTrue(html.contains("Scorpion Gym"), "brand header")
        assertTrue(html.contains("Test &amp; Report"), "title is HTML-escaped")
        assertTrue(html.contains("x &lt;b&gt;"), "cell text is HTML-escaped")
        assertTrue(html.contains("Ukupno: 3"), "summary line")
        assertTrue(html.contains("<h2>Tablica</h2>"), "sub-table heading")
        assertTrue(html.contains("<tfoot>"), "footer row")
        assertTrue(html.contains("class=\"num\""), "numeric columns right-aligned")
    }

    @Test
    fun `period formatting covers open and closed ranges`() {
        assertTrue(formatPeriod(null, null) == "Sva razdoblja")
        assertTrue(formatPeriod(java.time.LocalDate.of(2025, 1, 1), java.time.LocalDate.of(2025, 1, 31)).contains("–"))
    }
}
