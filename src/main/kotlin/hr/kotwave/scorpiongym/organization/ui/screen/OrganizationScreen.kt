package hr.kotwave.scorpiongym.organization.ui.screen

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
import hr.kotwave.scorpiongym.organization.Organization
import hr.kotwave.scorpiongym.organization.OrganizationViewModel
import hr.kotwave.scorpiongym.organization.ui.composable.OrganizationDetails
import hr.kotwave.scorpiongym.organization.ui.composable.OrganizationList
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.CustomBackIcon
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

class OrganizationScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        var selectedOrganization by remember { mutableStateOf<Organization?>(null) }
        var isCreatingNewOrganization by remember { mutableStateOf(false) }
        var detailsVisible by remember { mutableStateOf(false) }

        val organizationViewModel: OrganizationViewModel = getKoin().get()

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
                                selectedOrganization = Organization()
                                isCreatingNewOrganization = true
                                detailsVisible = true
                            },
                            text = "Dodaj novu organizaciju"
                        )
                    }

                    OrganizationList(
                        onItemClick = { organization ->
                            selectedOrganization = organization
                            isCreatingNewOrganization = false
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
                selectedOrganization?.let { organization ->
                    OrganizationDetails(
                        organization = organization,
                        onBackClick = {
                            detailsVisible = false
                            coroutineScope.launch {
                                delay(450)
                                selectedOrganization = null
                            }
                        },
                        onUpdateClick = { updatedOrganization ->
                            if (isCreatingNewOrganization) {
                                try {
                                    organizationViewModel.addOrganization(updatedOrganization)
                                } catch (e: Exception) {
                                    infoMessage = "Greška pri kreiranju organizacije"
                                    showInfoDialog = true
                                }
                                isCreatingNewOrganization = false
                            } else {
                                try {
                                    organizationViewModel.updateOrganization(updatedOrganization)
                                } catch (e: Exception) {
                                    infoMessage = "Greška pri ažuriranju organizacije"
                                    showInfoDialog = true
                                }
                            }
                            selectedOrganization = updatedOrganization
                        }
                    )
                }
            }
        }
    }
}