package hr.kotwave.scorpiongym

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import hr.kotwave.scorpiongym.appuser.AppUserViewModel
import hr.kotwave.scorpiongym.appuser.ui.AppUsersList
import hr.kotwave.scorpiongym.appuser.ui.LoginScreen
import hr.kotwave.scorpiongym.appuser.ui.UserActionsDialog
import hr.kotwave.scorpiongym.database.DatabaseFactory
import hr.kotwave.scorpiongym.di.appModule
import hr.kotwave.scorpiongym.di.memberScopeModule
import hr.kotwave.scorpiongym.member.ui.screen.MainScreen
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecordDao
import hr.kotwave.scorpiongym.paymentauditlog.PaymentAuditLogViewModel
import hr.kotwave.scorpiongym.paymentauditlog.ui.CashRegisterDialog
import hr.kotwave.scorpiongym.report.ui.ReportScreen
import hr.kotwave.scorpiongym.ui.custom.dialog.CreateNewAppUserDialog
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.menu.CustomMenu
import hr.kotwave.scorpiongym.ui.theme.ScorpionGymTheme
import hr.kotwave.scorpiongym.unregisteredservice.ui.dialog.AddUnregisteredServiceDialog
import hr.kotwave.scorpiongym.unregisteredservice.ui.dialog.UnregisteredServiceDialog
import hr.kotwave.scorpiongym.util.PreferencesHelper
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import java.awt.Dimension
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun main() = application {
    startKoin {
        modules(appModule, memberScopeModule)
    }

    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    var showCashRegisterDialog by remember { mutableStateOf(false) }
    var showReportScreen by remember { mutableStateOf(false) }

    Window(
        onCloseRequest = {
            PreferencesHelper().clearUser()
            DatabaseFactory.connect().use { con ->
                con?.autoCommit = false
                try {
                    con?.prepareStatement("UPDATE CurrentSessionUser SET currentAppUserId = null")?.executeUpdate()
                    con?.commit()
                } catch (e: Exception) {
                    con?.rollback()
                    println("Error setting currentAppUserId to null: ${e.message}")
                }
            }
            exitApplication()
        },
        title = "Scorpion Gym",
        state = windowState,
        icon = painterResource("ScorpionWindowIcon.png"),
        onKeyEvent = {
            if (it.type == KeyEventType.KeyDown  && it.isCtrlPressed && it.isShiftPressed && it.key == Key.K && PreferencesHelper().isAdmin) {
                getKoin().get<PaymentAuditLogViewModel>().initPaymentAuditLogs()
                showCashRegisterDialog = true
                true
            } else if (it.type == KeyEventType.KeyDown && it.isCtrlPressed && it.isShiftPressed && it.key == Key.I && PreferencesHelper().isAdmin) {
                showReportScreen = true
                true
            } else {
                false
            }
        }
    ) {
        window.minimumSize = Dimension(1000, 800)
        ScorpionGymApp(
            showCashRegisterDialog,
            onCloseCashRegister = { showCashRegisterDialog = false },
            showReportScreen = showReportScreen,
            onReportScreenConsumed = { showReportScreen = false }
        )
    }
}

@Composable
fun ScorpionGymApp(
    showCashRegisterDialog: Boolean,
    onCloseCashRegister: () -> Unit,
    showReportScreen: Boolean,
    onReportScreenConsumed: () -> Unit
) {
    val preferencesHelper = remember { PreferencesHelper() }
    var darkTheme by remember { mutableStateOf(preferencesHelper.isDarkTheme) }
    val coroutineScope = rememberCoroutineScope()

    var isLoggedIn by remember { mutableStateOf(false) }

    var showAddUnregisteredServiceDialog by remember { mutableStateOf(false) }
    var showUnregisteredServicesHistoryDialog by remember { mutableStateOf(false) }
    var showCreateNewAppUserDialog by remember { mutableStateOf(false) }
    var showAppUsersListDialog by remember { mutableStateOf(false) }

    var infoMessage by remember { mutableStateOf("") }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAllActionsDialog by remember { mutableStateOf(false) }

    val appUserViewModel: AppUserViewModel = getKoin().get()
    val membershipRecordDao: MembershipRecordDao = getKoin().get()

    try {
        membershipRecordDao.refreshMembershipStatuses()
    } catch (_: Exception) {
        infoMessage = "Greška pri ažuriranju aktivnosti članarina"
        showInfoDialog = true
    }

    ScorpionGymTheme(darkTheme = darkTheme) {
        Surface {
            when {
                showAddUnregisteredServiceDialog -> {
                    AddUnregisteredServiceDialog { showAddUnregisteredServiceDialog = false }
                }
                showInfoDialog -> {
                    InformativeDialog(infoMessage) { showInfoDialog = false }
                }
                showAllActionsDialog -> {
                    UserActionsDialog(logs = appUserViewModel.getAllActivityLogs(), onClose = { showAllActionsDialog = false })
                }
                showCreateNewAppUserDialog -> {
                    CreateNewAppUserDialog { showCreateNewAppUserDialog = false }
                }
                showUnregisteredServicesHistoryDialog -> {
                    UnregisteredServiceDialog { showUnregisteredServicesHistoryDialog = false  }
                }
                showCashRegisterDialog -> {
                    CashRegisterDialog { onCloseCashRegister() }
                }
                showAppUsersListDialog -> {
                    AppUsersList { showAppUsersListDialog = false }
                }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                if (isLoggedIn) {
                    CustomMenu(
                        onThemeChange = {
                            darkTheme = !darkTheme
                            coroutineScope.launch {
                                preferencesHelper.isDarkTheme = darkTheme
                            }
                        },
                        onBackup = {
                            try {
                                createDatabaseBackup()
                                infoMessage = "Uspješno kreiran backup"
                                showInfoDialog = true
                            } catch (_: Exception) {
                                infoMessage = "Greška kod izrade backup-a"
                                showInfoDialog = true
                            }
                        },
                        onAddUnregisteredService = {
                            showAddUnregisteredServiceDialog = true
                        },
                        onLogout = {
                            DatabaseFactory.connect().use { con ->
                                con?.autoCommit = false
                                try {
                                    con?.prepareStatement("UPDATE CurrentSessionUser SET currentAppUserId = null")?.executeUpdate()
                                    con?.commit()
                                } catch (_: Exception) {
                                    con?.rollback()
                                    infoMessage = "Greška pri postavljanju logiranog korisnika u bazu"
                                    showInfoDialog = true
                                }
                            }
                            isLoggedIn = false
                            PreferencesHelper().clearUser()
                        },
                        onAllLogsSelected = {
                            showAllActionsDialog = true
                        },
                        onCreateNewAppUser = {
                            showCreateNewAppUserDialog = true
                        },
                        onOpenUnregisteredServiceDialog = {
                            showUnregisteredServicesHistoryDialog = true
                        },
                        onOpenAppUserList = {
                            showAppUsersListDialog = true
                        },
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
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

                    Column(modifier = Modifier.fillMaxSize()) {
                        if (isLoggedIn) {
                            Navigator(MainScreen()) { navigator ->
                                LaunchedEffect(showReportScreen) {
                                    if (showReportScreen) {
                                        if (navigator.lastItem is MainScreen) {
                                            navigator.push(ReportScreen())
                                        }
                                        onReportScreenConsumed()
                                    }
                                }
                                SlideTransition(navigator)
                            }
                        } else {
                            Navigator(LoginScreen(onLoginSuccess = {
                                DatabaseFactory.connect().use { con ->
                                    con?.autoCommit = false
                                    try {
                                        con?.prepareStatement("UPDATE CurrentSessionUser SET currentAppUserId = ?").use { preparedStatement ->
                                            preparedStatement?.setInt(1, PreferencesHelper().loggedInUserId!!)
                                            preparedStatement?.executeUpdate()
                                        }
                                        con?.commit()
                                    } catch (_: Exception) {
                                        con?.rollback()
                                        infoMessage = "Greška pri postavljanju logiranog korisnika u bazu"
                                        showInfoDialog = true
                                    }
                                }
                                try {
                                    membershipRecordDao.refreshMembershipStatuses()
                                } catch (_: Exception) {
                                    infoMessage = "Greška pri ažuriranju aktivnosti članarina"
                                    showInfoDialog = true
                                }
                                isLoggedIn = true
                            })) { navigator ->
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

    if (!backupDir.exists()) {
        backupDir.mkdirs()
    }

    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MMddyyyy_HHmmss")
    val backupFileName = currentDateTime.format(formatter) + "_gymdatabase_backup.db"

    val backupFilePath = Path.of(backupDirPath, backupFileName)

    Files.copy(Path.of(dbPath), backupFilePath, StandardCopyOption.REPLACE_EXISTING)
}
