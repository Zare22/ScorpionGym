package hr.kotwave.scorpiongym.otherservice.ui.composable

import androidx.compose.runtime.Composable
import hr.kotwave.scorpiongym.otherservice.OtherService
import hr.kotwave.scorpiongym.otherservice.OtherServiceViewModel
import hr.kotwave.scorpiongym.ui.custom.composable.CrudList
import hr.kotwave.scorpiongym.ui.custom.composable.CrudListLabels
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun OtherServiceList(onItemClick: (OtherService) -> Unit) {
    val viewModel: OtherServiceViewModel = getKoin().get()
    CrudList(
        entities = viewModel.otherServices,
        nameOf = { it.name },
        onItemClick = onItemClick,
        onDelete = viewModel::deleteOtherService,
        labels = CrudListLabels(
            searchHint = "Pretraži ostale usluge",
            deleteDialogTitle = "Brisanje ostale usluge",
            deleteDialogBody = "Ukoliko nastavite pobrisat ćete ostalu uslugu ",
            deleteErrorMessage = "Nije moguće pobrisati ostalu uslugu jer se koristi!",
            contextMenuLabel = "Obriši ostalu uslugu",
        ),
    )
}
