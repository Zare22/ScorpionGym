package hr.kotwave.scorpiongym.typeoforganization.ui.screen

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
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.typeoforganization.ui.composable.TypeOfOrganizationDetails
import hr.kotwave.scorpiongym.typeoforganization.ui.composable.TypeOfOrganizationList
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.CustomBackIcon
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

class TypeOfOrganizationScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        var selectedOrganizationType by remember { mutableStateOf<TypeOfOrganization?>(null) }
        var isCreatingNewOrganizationType by remember { mutableStateOf(false) }
        var detailsVisible by remember { mutableStateOf(false) }

        val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()

        val coroutineScope = rememberCoroutineScope()

        var showInfoDialog by remember { mutableStateOf(false) }
        var infoMessage by remember { mutableStateOf("") }

        when {
            showInfoDialog -> {
                InformativeDialog(infoMessage) { showInfoDialog = false }
            }
        }

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
                                selectedOrganizationType = TypeOfOrganization(
                                    name = "",
                                    discountRate = 0.0
                                )
                                isCreatingNewOrganizationType = true
                                detailsVisible = true
                            },
                            text = "Dodaj novi tip organizacije"
                        )
                    }

                    TypeOfOrganizationList(
                        onItemClick = { typeOfOrganization ->
                            selectedOrganizationType = typeOfOrganization
                            isCreatingNewOrganizationType = false
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
                selectedOrganizationType?.let { typeOfOrganization ->
                    TypeOfOrganizationDetails(
                        typeOfOrganization = typeOfOrganization,
                        onBackClick = {
                            detailsVisible = false
                            coroutineScope.launch {
                                delay(450)
                                selectedOrganizationType = null
                            }
                        },
                        onUpdateClick = { updatedTypeOfOrganization ->
                            if (isCreatingNewOrganizationType) {
                                try {
                                    typeOfOrganizationViewModel.addTypeOfOrganization(updatedTypeOfOrganization)
                                } catch (e: Exception) {
                                    infoMessage = "Greška pri kreiranju tipa organizacije"
                                    showInfoDialog = true
                                }
                                isCreatingNewOrganizationType = false
                            } else {
                                try {
                                    typeOfOrganizationViewModel.updateTypeOfOrganization(updatedTypeOfOrganization)
                                } catch (e: Exception) {
                                    infoMessage = "Greška pri ažuriranju tipa organizacije"
                                    showInfoDialog = true
                                }
                            }
                            selectedOrganizationType = updatedTypeOfOrganization
                        }
                    )
                }
            }
        }
    }

}