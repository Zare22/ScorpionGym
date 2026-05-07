package hr.kotwave.scorpiongym.organization.ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import hr.kotwave.scorpiongym.organization.Organization
import hr.kotwave.scorpiongym.organization.OrganizationViewModel
import hr.kotwave.scorpiongym.organization.ui.composable.OrganizationDetails
import hr.kotwave.scorpiongym.organization.ui.composable.OrganizationList
import hr.kotwave.scorpiongym.ui.custom.composable.CrudScreenScaffold
import org.koin.java.KoinJavaComponent.getKoin

class OrganizationScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: OrganizationViewModel = getKoin().get()

        CrudScreenScaffold(
            emptyEntity = { Organization() },
            addButtonText = "Dodaj novu organizaciju",
            createErrorMessage = "Greška pri kreiranju organizacije",
            updateErrorMessage = "Greška pri ažuriranju organizacije",
            onAdd = viewModel::addOrganization,
            onUpdate = viewModel::updateOrganization,
            list = { onItemClick -> OrganizationList(onItemClick = onItemClick) },
            details = { entity, onBack, onSave ->
                OrganizationDetails(
                    organization = entity,
                    onBackClick = onBack,
                    onUpdateClick = onSave,
                )
            },
        )
    }
}
