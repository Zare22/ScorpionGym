package hr.kotwave.scorpiongym.report.print

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val GENERATED: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")

/** Renders a [PrintableReport] to a standalone, print-friendly HTML document. */
fun renderReportHtml(report: PrintableReport): String = buildString {
    append("<!DOCTYPE html>\n<html lang=\"hr\">\n<head>\n<meta charset=\"UTF-8\">\n")
    append("<title>").append(esc(report.title)).append("</title>\n")
    append("<style>\n").append(CSS).append("\n</style>\n</head>\n<body>\n")

    append("<header>\n")
    append("<div class=\"brand\">Scorpion Gym</div>\n")
    append("<h1>").append(esc(report.title)).append("</h1>\n")
    append("<div class=\"meta\">Razdoblje: ").append(esc(report.period)).append("</div>\n")
    if (report.summary != null) {
        append("<div class=\"meta\">").append(esc(report.summary)).append("</div>\n")
    }
    append("<div class=\"meta\">Izrađeno: ").append(LocalDateTime.now().format(GENERATED)).append("</div>\n")
    append("</header>\n")

    for (table in report.tables) {
        if (table.title != null) append("<h2>").append(esc(table.title)).append("</h2>\n")
        append("<table>\n<thead>\n<tr>")
        table.columns.forEachIndexed { i, c -> appendCell("th", i, c) }
        append("</tr>\n</thead>\n<tbody>\n")
        for (row in table.rows) {
            append("<tr>")
            row.forEachIndexed { i, cell -> appendCell("td", i, cell) }
            append("</tr>\n")
        }
        append("</tbody>\n")
        if (table.footer != null) {
            append("<tfoot>\n<tr>")
            table.footer.forEachIndexed { i, cell -> appendCell("td", i, cell) }
            append("</tr>\n</tfoot>\n")
        }
        append("</table>\n")
    }

    append("</body>\n</html>\n")
}

/** First column is the label (default align); the rest are numeric (right-aligned). */
private fun StringBuilder.appendCell(tag: String, index: Int, value: String) {
    if (index == 0) append("<").append(tag).append(">")
    else append("<").append(tag).append(" class=\"num\">")
    append(esc(value)).append("</").append(tag).append(">")
}

private fun esc(s: String): String = buildString {
    for (c in s) when (c) {
        '&' -> append("&amp;")
        '<' -> append("&lt;")
        '>' -> append("&gt;")
        '"' -> append("&quot;")
        else -> append(c)
    }
}

private val CSS = """
    * { box-sizing: border-box; }
    body { font-family: Arial, Helvetica, sans-serif; color: #1b1f24; margin: 32px; }
    header { border-bottom: 3px solid #b3361f; padding-bottom: 12px; margin-bottom: 20px; }
    .brand { color: #b3361f; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; font-size: 13px; }
    h1 { font-size: 22px; margin: 6px 0 8px; }
    h2 { font-size: 16px; margin: 22px 0 8px; }
    .meta { color: #5b6671; font-size: 13px; }
    table { width: 100%; border-collapse: collapse; margin: 8px 0 4px; font-size: 14px; }
    th, td { text-align: left; padding: 7px 10px; border-bottom: 1px solid #e1e6ea; }
    th { background: #f0f3f5; font-size: 12px; text-transform: uppercase; letter-spacing: .03em; color: #5b6671; }
    td.num, th.num { text-align: right; }
    tfoot td { font-weight: 700; border-top: 2px solid #cdd5db; border-bottom: none; }
    @media print { body { margin: 0; } th { -webkit-print-color-adjust: exact; print-color-adjust: exact; } }
""".trimIndent()
