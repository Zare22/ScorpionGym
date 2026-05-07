package hr.kotwave.scorpiongym.typeoforganization.ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.typeoforganization.ui.composable.TypeOfOrganizationDetails
import hr.kotwave.scorpiongym.typeoforganization.ui.composable.TypeOfOrganizationList
import hr.kotwave.scorpiongym.ui.custom.composable.CrudScreenScaffold
import org.koin.java.KoinJavaComponent.getKoin

class TypeOfOrganizationScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: TypeOfOrganizationViewModel = getKoin().get()

        CrudScreenScaffold(
            emptyEntity = { TypeOfOrganization(name = "", discountRate = 0.0) },
            addButtonText = "Dodaj novi tip organizacije",
            createErrorMessage = "Greška pri kreiranju tipa organizacije",
            updateErrorMessage = "Greška pri ažuriranju tipa organizacije",
            onAdd = viewModel::addTypeOfOrganization,
            onUpdate = viewModel::updateTypeOfOrganization,
            list = { onItemClick -> TypeOfOrganizationList(onItemClick = onItemClick) },
            details = { entity, onBack, onSave ->
                TypeOfOrganizationDetails(
                    typeOfOrganization = entity,
                    onBackClick = onBack,
                    onUpdateClick = onSave,
                )
            },
        )
    }
}
