package hr.kotwave.scorpiongym.report.print

import java.awt.Desktop
import java.io.File
import java.nio.file.FileSystems
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Writes a report as an HTML file under ~/ScorpionGym/Reports and opens it in the
 * default browser (where the user can print or Save-as-PDF). Mirrors the DB-backup
 * file pattern. Best-effort: failures are logged, never thrown into the UI.
 */
object ReportExporter {

    private val STAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    fun exportAndOpen(report: PrintableReport) {
        try {
            val userHome = System.getProperty("user.home")
            val separator = FileSystems.getDefault().separator
            val dir = File("$userHome${separator}ScorpionGym${separator}Reports")
            if (!dir.exists()) dir.mkdirs()

            val safeTitle = report.title.replace(Regex("[^\\p{L}\\p{Nd}]+"), "_").trim('_')
            val file = File(dir, "${safeTitle}_${LocalDateTime.now().format(STAMP)}.html")
            file.writeText(renderReportHtml(report), Charsets.UTF_8)

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(file.toURI())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
