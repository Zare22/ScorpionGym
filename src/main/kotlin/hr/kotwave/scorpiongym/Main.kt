package hr.kotwave.scorpiongym

import androidx.compose.foundation.DarkDefaultContextMenuRepresentation
import androidx.compose.foundation.Image
import androidx.compose.foundation.LightDefaultContextMenuRepresentation
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import hr.kotwave.scorpiongym.database.DatabaseFactory
import hr.kotwave.scorpiongym.di.appModule
import hr.kotwave.scorpiongym.di.memberScopeModule
import hr.kotwave.scorpiongym.member.ui.screen.MainScreen
import hr.kotwave.scorpiongym.ui.theme.ScorpionGymTheme
import hr.kotwave.scorpiongym.util.PreferencesHelper
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import java.awt.Dimension
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterialApi::class)
fun main() = application {
    startKoin {
        modules(appModule, memberScopeModule)
    }

    DatabaseFactory.initDB()

    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Scorpion Gym",
        state = windowState,
        icon = painterResource("ScorpionWindowIcon.png")
    ) {
        window.minimumSize = Dimension(1000, 800)
        val preferencesHelper = remember { PreferencesHelper() }
        var darkTheme by remember { mutableStateOf(preferencesHelper.isDarkTheme) }
        val coroutineScope = rememberCoroutineScope()

        val contextMenuRepresentation = if (darkTheme) {
            DarkDefaultContextMenuRepresentation
        } else {
            LightDefaultContextMenuRepresentation
        }
        ScorpionGymTheme(darkTheme = darkTheme) {
            CompositionLocalProvider(
                LocalContextMenuRepresentation provides contextMenuRepresentation,
                LocalMinimumInteractiveComponentEnforcement provides false
            ) {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MenuBar {
                            Menu("Postavke") {
                                Item("Promijeni temu") {
                                    darkTheme = !darkTheme
                                    coroutineScope.launch {
                                        preferencesHelper.isDarkTheme = darkTheme
                                    }
                                }
                                Item("Napravi backup") {
                                    createDatabaseBackup()
                                }
                            }
                        }
                        val backgroundImage = if (darkTheme) {
                            painterResource("ScorpionWhiteTransparent.png")
                        } else {
                            painterResource("ScorpionBlack_transparent.png")
                        }

                        Image(
                            painter = backgroundImage,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.1f)
                        )

                        Column {
                            Navigator(MainScreen()) { navigator ->
                                SlideTransition(navigator)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun createDatabaseBackup() {
    val userHome = System.getProperty("user.home")
    val separator = FileSystems.getDefault().separator
    val dbPath = "$userHome${separator}ScorpionGym${separator}gymdatabase.db"

    val backupDirPath = "$userHome${separator}ScorpionGym${separator}Backup"
    val backupDir = File(backupDirPath)

    // Ensure the backup directory exists
    if (!backupDir.exists()) {
        backupDir.mkdirs()
    }

    // Define the backup file name using the current date and time
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MMddyyyy_HHmmss")
    val backupFileName = currentDateTime.format(formatter) + "_gymdatabase_backup.db"

    // Define the backup file path
    val backupFilePath = Path.of(backupDirPath, backupFileName)

    // Copy the database file to the backup directory with the new name
    Files.copy(Path.of(dbPath), backupFilePath, StandardCopyOption.REPLACE_EXISTING)
}
