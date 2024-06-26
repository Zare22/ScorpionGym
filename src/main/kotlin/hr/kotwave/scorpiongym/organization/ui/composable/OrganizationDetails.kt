package hr.kotwave.scorpiongym.organization.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import hr.kotwave.scorpiongym.organization.Organization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.typeoforganization.ui.window.TypeOfOrganizationWindow
import hr.kotwave.scorpiongym.ui.theme.Shapes
import hr.kotwave.scorpiongym.ui.custom.elements.Dropdown
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun OrganizationDetails(
    organization: Organization,
    onBackClick: () -> Unit,
    onUpdateClick: (Organization) -> Unit
) {
    val focusRequesters = List(4) { FocusRequester() }
    val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()

    var organizationTypeWindowOpened by remember { mutableStateOf(false) }

    var name by remember(organization) {
        mutableStateOf(
            TextFieldValue(
                organization.name,
                selection = TextRange(organization.name.length)
            )
        )
    }
    var typeOfOrganization by remember(organization) { mutableStateOf(organization.typeOfOrganizationId.toString()) }

    var expandedOrganizationType by remember { mutableStateOf(false) }

    LaunchedEffect(organization) {
        name = TextFieldValue(organization.name, selection = TextRange(organization.name.length))
        typeOfOrganization = organization.typeOfOrganizationId.toString()
        focusRequesters[0].requestFocus()
    }

    Surface(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        elevation = 4.dp,
        shape = Shapes.large,
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Organizacija", style = MaterialTheme.typography.h5)
                }
                Spacer(modifier = Modifier.height(16.dp))

                FocusableOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Ime organizacije",
                    currentFocusRequester = focusRequesters[0],
                    nextFocusRequester = focusRequesters[1]
                )

                Row(
                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Dropdown(modifier = Modifier.weight(1f),
                        expanded = expandedOrganizationType,
                        onExpandedChange = { expandedOrganizationType = it },
                        label = "Tip organizacije",
                        items = typeOfOrganizationViewModel.organizationTypes,
                        selectedItem = typeOfOrganizationViewModel.organizationTypes.find { it.id.toString() == typeOfOrganization },
                        onItemSelected = { typeOfOrganization = it.id.toString() },
                        focusRequester = focusRequesters[1],
                        nextFocusRequester = focusRequesters[2],
                        itemLabel = { it.name }
                    )

                    HoverableButton(
                        modifier = Modifier.wrapContentWidth(),
                        text = "Kreiraj novi tip organizacije",
                        onClick = {
                            organizationTypeWindowOpened = true
                        }
                    )
                }

                if (organizationTypeWindowOpened) {
                    Window(
                        onCloseRequest = { organizationTypeWindowOpened = false },
                        title = "Kreiraj novi tip organizacije",
                        alwaysOnTop = true,
                        state = WindowState(
                            position = WindowPosition.Aligned(Alignment.Center),
                            height = 400.dp, width = 400.dp
                        ),
                        icon = painterResource("ScorpionWindowIcon.png")
                    ) {
                        TypeOfOrganizationWindow(onClose = { organizationTypeWindowOpened = false })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HoverableButton(
                        onClick = { onBackClick() }, text = "Povratak"
                    )
                    HoverableButton(modifier = Modifier.focusRequester(focusRequesters[3]).onPreviewKeyEvent {
                        if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                            focusRequesters[0].requestFocus()
                            true
                        } else false
                    }, onClick = {
                        val updatedOrganization = organization.copy(
                            name = name.text,
                            typeOfOrganizationId = typeOfOrganization.toIntOrNull() ?: organization.typeOfOrganizationId
                        )
                        onUpdateClick(updatedOrganization)
                    }, text = if (organization.id != 0) "Ažuriraj" else "Dodaj"
                    )
                }
            }
        }
    }
}