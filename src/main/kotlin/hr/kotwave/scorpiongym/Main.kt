package hr.kotwave.scorpiongym

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import hr.kotwave.scorpiongym.database.DatabaseFactory
import hr.kotwave.scorpiongym.di.appModule
import hr.kotwave.scorpiongym.member.ui.screen.MainScreen
import hr.kotwave.scorpiongym.ui.theme.ScorpionGymTheme
import hr.kotwave.scorpiongym.util.PreferencesHelper
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import java.awt.Dimension

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
fun main() = application {
    startKoin {
        modules(appModule)
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
//                LocalContextMenuRepresentation provides MaterialContextMenuRepresentation(),
//                LocalTextContextMenu provides MaterialTextContextMenu
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
