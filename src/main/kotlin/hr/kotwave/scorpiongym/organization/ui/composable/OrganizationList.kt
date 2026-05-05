package hr.kotwave.scorpiongym.organization.ui.composable

import androidx.compose.runtime.Composable
import hr.kotwave.scorpiongym.organization.Organization
import hr.kotwave.scorpiongym.organization.OrganizationViewModel
import hr.kotwave.scorpiongym.ui.custom.composable.CrudList
import hr.kotwave.scorpiongym.ui.custom.composable.CrudListLabels
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun OrganizationList(onItemClick: (Organization) -> Unit) {
    val viewModel: OrganizationViewModel = getKoin().get()
    CrudList(
        entities = viewModel.organizations,
        nameOf = { it.name },
        onItemClick = onItemClick,
        onDelete = viewModel::deleteOrganization,
        labels = CrudListLabels(
            searchHint = "Pretraži organizaciju",
            deleteDialogTitle = "Brisanje organizacije",
            deleteDialogBody = "Ukoliko nastavite pobrisat ćete organizaciju ",
            deleteErrorMessage = "Nije moguće pobrisati organizaciju jer se koristi!",
            contextMenuLabel = "Obriši organizaciju",
        ),
    )
}
