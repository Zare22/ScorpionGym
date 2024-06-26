package hr.kotwave.scorpiongym.otherservice.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hr.kotwave.scorpiongym.otherservice.OtherService
import hr.kotwave.scorpiongym.otherservice.OtherServiceViewModel
import hr.kotwave.scorpiongym.otherservice.ui.composable.OtherServiceDetails
import hr.kotwave.scorpiongym.otherservice.ui.composable.OtherServiceList
import hr.kotwave.scorpiongym.ui.custom.elements.CustomBackIcon
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

class OtherServiceScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        var selectedService by remember { mutableStateOf<OtherService?>(null) }
        var isCreatingNewService by remember { mutableStateOf(false) }
        var detailsVisible by remember { mutableStateOf(false) }

        val otherServiceViewModel: OtherServiceViewModel = getKoin().get()

        val coroutineScope = rememberCoroutineScope()

        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CustomBackIcon(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            navigator = navigator
                        )

                        HoverableButton(
                            onClick = {
                                selectedService = OtherService(
                                    name = "",
                                    price = 0.0
                                )
                                isCreatingNewService = true
                                detailsVisible = true
                            },
                            text = "Dodaj novu uslugu"
                        )
                    }

                    OtherServiceList(
                        onItemClick = { otherService ->
                            selectedService = otherService
                            isCreatingNewService = false
                            detailsVisible = true
                        }
                    )
                }
            }

            AnimatedVisibility(
                modifier = Modifier.fillMaxHeight().weight(2f),
                visible = detailsVisible,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(450, easing = LinearEasing)
                ),
                exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(450, easing = LinearEasing))
            ) {
                selectedService?.let { otherService ->
                    OtherServiceDetails(
                        otherService = otherService,
                        onBackClick = {
                            detailsVisible = false
                            coroutineScope.launch {
                                delay(450)
                                selectedService = null
                            }
                        },
                        onUpdateClick = { updatedOtherService ->
                            if (isCreatingNewService) {
                                otherServiceViewModel.addOtherService(updatedOtherService)
                                isCreatingNewService = false
                            } else {
                                otherServiceViewModel.updateOtherService(updatedOtherService)
                            }
                            selectedService = updatedOtherService
                        }
                    )
                }
            }
        }
    }
}