package hr.kotwave.scorpiongym.typeoforganization.ui.composable

import androidx.compose.runtime.Composable
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.ui.custom.composable.CrudList
import hr.kotwave.scorpiongym.ui.custom.composable.CrudListLabels
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun TypeOfOrganizationList(onItemClick: (TypeOfOrganization) -> Unit) {
    val viewModel: TypeOfOrganizationViewModel = getKoin().get()
    CrudList(
        entities = viewModel.organizationTypes,
        nameOf = { it.name },
        onItemClick = onItemClick,
        onDelete = viewModel::deleteTypeOfOrganization,
        labels = CrudListLabels(
            searchHint = "Pretraži tip organizacije",
            deleteDialogTitle = "Brisanje tipa organizacije",
            deleteDialogBody = "Ukoliko nastavite pobrisat ćete tip organizacije ",
            deleteErrorMessage = "Nije moguće pobrisati tip organizacije jer se koristi!",
            contextMenuLabel = "Obriši tip organizacije",
        ),
    )
}
