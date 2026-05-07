package hr.kotwave.scorpiongym.typeoforganization.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganization
import hr.kotwave.scorpiongym.ui.custom.composable.CrudDetailsScaffold
import hr.kotwave.scorpiongym.ui.custom.composable.rememberSyncedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField

@Composable
fun TypeOfOrganizationDetails(
    typeOfOrganization: TypeOfOrganization,
    onBackClick: () -> Unit,
    onUpdateClick: (TypeOfOrganization) -> Unit,
) {
    val focusRequesters = List(3) { FocusRequester() }

    var name by rememberSyncedTextField(typeOfOrganization, typeOfOrganization.name)
    var discountRate by rememberSyncedTextField(typeOfOrganization, typeOfOrganization.discountRate.toString())

    CrudDetailsScaffold(
        title = "Tip organizacije",
        isNew = typeOfOrganization.id == 0,
        onBack = onBackClick,
        onSave = {
            onUpdateClick(
                typeOfOrganization.copy(
                    name = name.text,
                    discountRate = discountRate.text.toDouble(),
                )
            )
        },
        firstFocusRequester = focusRequesters[0],
        saveFocusRequester = focusRequesters[2],
    ) {
        FocusableOutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = "Naziv tipa organizacije",
            currentFocusRequester = focusRequesters[0],
            nextFocusRequester = focusRequesters[1],
        )
        FocusableOutlinedTextField(
            value = discountRate,
            onValueChange = { discountRate = it },
            label = "Popust(u postotcima)",
            currentFocusRequester = focusRequesters[1],
            nextFocusRequester = focusRequesters[2],
        )
    }
}
