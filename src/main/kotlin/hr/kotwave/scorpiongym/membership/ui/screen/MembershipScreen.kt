package hr.kotwave.scorpiongym.membership.ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import hr.kotwave.scorpiongym.membership.Membership
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.membership.ui.composable.MembershipDetails
import hr.kotwave.scorpiongym.membership.ui.composable.MembershipList
import hr.kotwave.scorpiongym.ui.custom.composable.CrudScreenScaffold
import org.koin.java.KoinJavaComponent.getKoin

class MembershipScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: MembershipViewModel = getKoin().get()

        CrudScreenScaffold(
            emptyEntity = { Membership() },
            addButtonText = "Dodaj novi tip članarine",
            createErrorMessage = "Greška pri kreiranju članarine",
            updateErrorMessage = "Greška pri ažuriranju članarine",
            onAdd = viewModel::addMembership,
            onUpdate = viewModel::updateMembership,
            list = { onItemClick -> MembershipList(onItemClick = onItemClick) },
            details = { entity, onBack, onSave ->
                MembershipDetails(
                    membership = entity,
                    onBackClick = onBack,
                    onUpdateClick = onSave,
                )
            },
        )
    }
}
