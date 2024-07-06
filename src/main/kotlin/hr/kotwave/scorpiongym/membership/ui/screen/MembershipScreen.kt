package hr.kotwave.scorpiongym.membership.ui.screen

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
import hr.kotwave.scorpiongym.membership.Membership
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.membership.ui.composable.MembershipDetails
import hr.kotwave.scorpiongym.membership.ui.composable.MembershipList
import hr.kotwave.scorpiongym.ui.custom.elements.CustomBackIcon
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

class MembershipScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        var selectedMemberShip by remember { mutableStateOf<Membership?>(null) }
        var isCreatingNewMembership by remember { mutableStateOf(false) }
        var detailsVisible by remember { mutableStateOf(false) }

        val membershipViewModel: MembershipViewModel = getKoin().get()

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
                                selectedMemberShip = Membership()
                                isCreatingNewMembership = true
                                detailsVisible = true
                            },
                            text = "Dodaj novi tip članarine"
                        )
                    }

                    MembershipList(
                        onItemClick = { membership ->
                            selectedMemberShip = membership
                            isCreatingNewMembership = false
                            detailsVisible = true
                        }
                    )
                }
            }

            // Member Details
            AnimatedVisibility(
                modifier = Modifier.fillMaxHeight().weight(2f),
                visible = detailsVisible,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(450, easing = LinearEasing)
                ),
                exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(450, easing = LinearEasing))
            ) {
                selectedMemberShip?.let { membership ->
                    MembershipDetails(
                        membership = membership,
                        onBackClick = {
                            detailsVisible = false
                            coroutineScope.launch {
                                delay(450)
                                selectedMemberShip = null
                            }
                        },
                        onUpdateClick = { updatedMembership ->
                            if (isCreatingNewMembership) {
                                membershipViewModel.addMembership(updatedMembership)
                                isCreatingNewMembership = false
                            } else {
                                membershipViewModel.updateMembership(updatedMembership)
                            }
                            selectedMemberShip = updatedMembership
                        }
                    )
                }
            }
        }
    }
}