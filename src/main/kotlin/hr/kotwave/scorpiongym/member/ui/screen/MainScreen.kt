package hr.kotwave.scorpiongym.member.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MembersListViewModel
import hr.kotwave.scorpiongym.member.ui.composable.MemberDetails
import hr.kotwave.scorpiongym.member.ui.composable.MemberList
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.membership.ui.screen.MembershipScreen
import hr.kotwave.scorpiongym.organization.OrganizationViewModel
import hr.kotwave.scorpiongym.organization.ui.screen.OrganizationScreen
import hr.kotwave.scorpiongym.otherservice.ui.screen.OtherServiceScreen
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

class MainScreen : Screen {

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        var selectedMember by remember { mutableStateOf<Member?>(null) }
        var isCreatingNewMember by remember { mutableStateOf(false) }
        var detailsVisible by remember { mutableStateOf(false) }

        //ViewModel init
        val membersListViewModel: MembersListViewModel = getKoin().get()
        val membershipViewModel: MembershipViewModel = getKoin().get()
        val organizationViewModel: OrganizationViewModel = getKoin().get()

        //Coroutine
        val coroutineScope = rememberCoroutineScope()

        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                    ) {
                        HoverableButton(
                            padding = 8.dp,
                            onClick = {
                                selectedMember = Member()
                                isCreatingNewMember = true
                                detailsVisible = true
                            },
                            text = "Dodaj novog člana"
                        )

                        HoverableButton(
                            padding = 8.dp,
                            onClick = { navigator.push(MembershipScreen()) },
                            text = "Članarine"
                        )
                        HoverableButton(
                            padding = 8.dp,
                            onClick = { navigator.push(OrganizationScreen()) },
                            text = "Organizacije"
                        )
                        HoverableButton(
                            padding = 8.dp,
                            onClick = { navigator.push(OtherServiceScreen()) },
                            text = "Ostale usluge"
                        )
                    }

                    MemberList(
                        onItemClick = { member ->
                            selectedMember = member
                            isCreatingNewMember = false
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
                selectedMember?.let { member ->
                    MemberDetails(
                        member = member,
                        memberships = membershipViewModel.memberships,
                        organizations = organizationViewModel.organizations,
                        onBackClick = {
                            detailsVisible = false
                            coroutineScope.launch {
                                delay(450)
                                selectedMember = null
                            }
                        },
                        onUpdateClick = { updatedMember ->
                            if (isCreatingNewMember) {
                                membersListViewModel.addMember(updatedMember)
                                isCreatingNewMember = false
                            } else {
                                membersListViewModel.updateMember(updatedMember)
                            }
                            selectedMember = updatedMember
                        }
                    )
                }
            }
        }
    }
}
