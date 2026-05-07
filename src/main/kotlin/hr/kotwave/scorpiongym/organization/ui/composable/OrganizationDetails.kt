package hr.kotwave.scorpiongym.organization.ui.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import hr.kotwave.scorpiongym.organization.Organization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.typeoforganization.ui.dialog.TypeOfOrganizationDialog
import hr.kotwave.scorpiongym.ui.custom.composable.CrudDetailsScaffold
import hr.kotwave.scorpiongym.ui.custom.composable.rememberSyncedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.Dropdown
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun OrganizationDetails(
    organization: Organization,
    onBackClick: () -> Unit,
    onUpdateClick: (Organization) -> Unit,
) {
    val focusRequesters = List(4) { FocusRequester() }
    val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()

    var organizationTypeWindowOpened by remember { mutableStateOf(false) }
    var name by rememberSyncedTextField(organization, organization.name)
    var typeOfOrganization by remember(organization) { mutableStateOf(organization.typeOfOrganizationId.toString()) }
    var expandedOrganizationType by remember { mutableStateOf(false) }

    LaunchedEffect(organization) {
        typeOfOrganization = organization.typeOfOrganizationId.toString()
    }

    CrudDetailsScaffold(
        title = "Organizacija",
        isNew = organization.id == 0,
        onBack = onBackClick,
        onSave = {
            onUpdateClick(
                organization.copy(
                    name = name.text,
                    typeOfOrganizationId = typeOfOrganization.toIntOrNull(),
                )
            )
        },
        firstFocusRequester = focusRequesters[0],
        saveFocusRequester = focusRequesters[2],
    ) {
        FocusableOutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = "Naziv organizacije",
            currentFocusRequester = focusRequesters[0],
            nextFocusRequester = focusRequesters[1],
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Dropdown(
                modifier = Modifier.weight(1f),
                expanded = expandedOrganizationType,
                onExpandedChange = { expandedOrganizationType = it },
                label = "Tip organizacije",
                items = typeOfOrganizationViewModel.organizationTypes,
                selectedItem = typeOfOrganizationViewModel.organizationTypes.find { it.id.toString() == typeOfOrganization },
                onItemSelected = { typeOfOrganization = it.id.toString() },
                focusRequester = focusRequesters[1],
                nextFocusRequester = focusRequesters[2],
                itemLabel = { it.name },
            )

            HoverableButton(
                modifier = Modifier.wrapContentWidth(),
                text = "Kreiraj novi tip organizacije",
                onClick = { organizationTypeWindowOpened = true },
            )
        }

        if (organizationTypeWindowOpened) {
            TypeOfOrganizationDialog(onClose = { organizationTypeWindowOpened = false })
        }
    }
}
