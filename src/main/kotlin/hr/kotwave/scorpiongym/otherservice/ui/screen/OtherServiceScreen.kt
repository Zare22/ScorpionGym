package hr.kotwave.scorpiongym.otherservice.ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import hr.kotwave.scorpiongym.otherservice.OtherService
import hr.kotwave.scorpiongym.otherservice.OtherServiceViewModel
import hr.kotwave.scorpiongym.otherservice.ui.composable.OtherServiceDetails
import hr.kotwave.scorpiongym.otherservice.ui.composable.OtherServiceList
import hr.kotwave.scorpiongym.ui.custom.composable.CrudScreenScaffold
import org.koin.java.KoinJavaComponent.getKoin

class OtherServiceScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: OtherServiceViewModel = getKoin().get()

        CrudScreenScaffold(
            emptyEntity = { OtherService(name = "", price = 0.0) },
            addButtonText = "Dodaj novu uslugu",
            createErrorMessage = "Greška pri kreiranju usluge",
            updateErrorMessage = "Greška pri ažuriranju usluge",
            onAdd = viewModel::addOtherService,
            onUpdate = viewModel::updateOtherService,
            list = { onItemClick -> OtherServiceList(onItemClick = onItemClick) },
            details = { entity, onBack, onSave ->
                OtherServiceDetails(
                    otherService = entity,
                    onBackClick = onBack,
                    onUpdateClick = onSave,
                )
            },
        )
    }
}
