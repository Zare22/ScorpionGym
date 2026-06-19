package hr.kotwave.scorpiongym.report.print

import java.awt.Desktop
import java.io.File
import java.nio.file.FileSystems
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * Writes a report as an HTML file under ~/ScorpionGym/Reports and opens it with the
 * OS default app (browser), where the user can print or Save-as-PDF.
 *
 * Opening is tried several ways because `Desktop.browse(file://…)` proved unreliable on
 * some client machines (it routes through the URL/protocol handler, not the .html file
 * association). [Desktop.open] mirrors a double-click; rundll32 is a last-resort shell
 * fallback. If nothing opens, the saved path is shown so the user can open it manually —
 * the export is never silent.
 *
 * Runs off the Compose UI thread: file I/O and launching the OS handler can block.
 */
object ReportExporter {

    private val STAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    fun exportAndOpen(report: PrintableReport) {
        Thread {
            try {
                val file = writeReport(report)
                if (!openFile(file)) {
                    info(
                        "Izvještaj je spremljen, ali ga nije moguće automatski otvoriti.\n\n" +
                            "Datoteka:\n${file.absolutePath}",
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                info("Greška pri izradi izvještaja: ${e.message}")
            }
        }.apply { isDaemon = true }.start()
    }

    private fun writeReport(report: PrintableReport): File {
        val userHome = System.getProperty("user.home")
        val separator = FileSystems.getDefault().separator
        val dir = File("$userHome${separator}ScorpionGym${separator}Reports")
        if (!dir.exists()) dir.mkdirs()

        val safeTitle = report.title.replace(Regex("[^\\p{L}\\p{Nd}]+"), "_").trim('_')
        val file = File(dir, "${safeTitle}_${LocalDateTime.now().format(STAMP)}.html")
        file.writeText(renderReportHtml(report), Charsets.UTF_8)
        return file
    }

    /** Tries several ways to open [file] with the OS default app; returns true once one launches. */
    private fun openFile(file: File): Boolean {
        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null

        // 1) Desktop.open — uses the .html file association, i.e. the same path as double-clicking.
        if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
            try {
                desktop.open(file)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2) Desktop.browse — the URL path; works on some setups where OPEN is unavailable.
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(file.toURI())
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3) Windows shell fallback — rundll32 hands the path to the default handler. Independent
        //    of java.awt.Desktop, so it works even if Desktop reports unsupported.
        try {
            ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", file.absolutePath).start()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    private fun info(message: String) {
        try {
            SwingUtilities.invokeLater {
                JOptionPane.showMessageDialog(null, message, "Scorpion Gym", JOptionPane.INFORMATION_MESSAGE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
