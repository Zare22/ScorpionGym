package hr.kotwave.scorpiongym.typeoforganization.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganization
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.theme.Shapes

@Composable
fun TypeOfOrganizationDetails(
    typeOfOrganization: TypeOfOrganization,
    onBackClick: () -> Unit,
    onUpdateClick: (TypeOfOrganization) -> Unit
) {
    val focusRequesters = List(3) { FocusRequester() }

    var name by remember(typeOfOrganization) {
        mutableStateOf(
            TextFieldValue(
                typeOfOrganization.name,
                selection = TextRange(typeOfOrganization.name.length)
            )
        )
    }
    var discountRate by remember(typeOfOrganization) {
        mutableStateOf(
            TextFieldValue(
                typeOfOrganization.discountRate.toString(),
                selection = TextRange(typeOfOrganization.discountRate.toString().length)
            )
        )
    }

    LaunchedEffect(typeOfOrganization) {
        name = TextFieldValue(typeOfOrganization.name, selection = TextRange(typeOfOrganization.name.length))
        discountRate = TextFieldValue(typeOfOrganization.discountRate.toString(), selection = TextRange(typeOfOrganization.discountRate.toString().length))
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
                    Text(text = "Tip organizacije", style = MaterialTheme.typography.h2)
                }
                Spacer(modifier = Modifier.height(16.dp))

                FocusableOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Naziv tipa organizacije",
                    currentFocusRequester = focusRequesters[0],
                    nextFocusRequester = focusRequesters[1]
                )
                FocusableOutlinedTextField(
                    value = discountRate,
                    onValueChange = { discountRate = it },
                    label = "Popust(u postotcima)",
                    currentFocusRequester = focusRequesters[1],
                    nextFocusRequester = focusRequesters[2]
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HoverableButton(
                        onClick = { onBackClick() }, text = "Povratak"
                    )
                    HoverableButton(modifier = Modifier.focusRequester(focusRequesters[2]).onPreviewKeyEvent {
                        if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                            focusRequesters[0].requestFocus()
                            true
                        } else false
                    }, onClick = {
                        val updatedOrganization = typeOfOrganization.copy(
                            name = name.text,
                            discountRate = discountRate.text.toDouble()
                        )
                        onUpdateClick(updatedOrganization)
                    }, text = if (typeOfOrganization.id != 0) "Ažuriraj" else "Dodaj",
                        buttonBackgroundColor = Color.Green
                    )
                }
            }
        }
    }
}