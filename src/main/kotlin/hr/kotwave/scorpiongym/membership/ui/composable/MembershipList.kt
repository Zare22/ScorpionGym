package hr.kotwave.scorpiongym.membership.ui.composable

import androidx.compose.runtime.Composable
import hr.kotwave.scorpiongym.membership.Membership
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.ui.custom.composable.CrudList
import hr.kotwave.scorpiongym.ui.custom.composable.CrudListLabels
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun MembershipList(onItemClick: (Membership) -> Unit) {
    val viewModel: MembershipViewModel = getKoin().get()
    CrudList(
        entities = viewModel.memberships,
        nameOf = { it.name },
        onItemClick = onItemClick,
        onDelete = viewModel::deleteMembership,
        labels = CrudListLabels(
            searchHint = "Pretraži tip članarine",
            deleteDialogTitle = "Brisanje članarine",
            deleteDialogBody = "Ukoliko nastavite pobrisat ćete članarinu ",
            deleteErrorMessage = "Nije moguće pobrisati članarinu jer se koristi!",
            contextMenuLabel = "Obriši članarinu",
        ),
    )
}
