package hr.kotwave.scorpiongym.typeoforganization.ui.window

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun TypeOfOrganizationDialog(onClose: () -> Unit) {
    val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()

    val focusRequesters = List(3) { FocusRequester() }

    var typeName by remember { mutableStateOf(TextFieldValue("")) }
    var discountRate by remember { mutableStateOf(0.0) }
    var discountRateInput by remember { mutableStateOf(TextFieldValue(discountRate.toString())) }

    LaunchedEffect(focusRequesters) {
        focusRequesters[0].requestFocus()
    }

    Dialog(onDismissRequest = { onClose() }) {
        Card(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                FocusableOutlinedTextField(
                    value = typeName,
                    onValueChange = { typeName = it },
                    label = "Ime tipa organizacije",
                    currentFocusRequester = focusRequesters[0],
                    nextFocusRequester = focusRequesters[1]
                )

                FocusableOutlinedTextField(
                    value = discountRateInput,
                    onValueChange = { input ->
                        val filteredInput = input.text.filter { it.isDigit() || it == '.' }

                        if (filteredInput.count { it == '.' } <= 1) {
                            discountRateInput = input.copy(text = filteredInput)
                            discountRate = filteredInput.toDoubleOrNull() ?: 0.0
                        }
                    },
                    label = "Popust",
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
                        onClick = { onClose() }, text = "Povratak"
                    )
                    HoverableButton(
                        modifier = Modifier
                            .focusRequester(focusRequesters[2])
                            .onPreviewKeyEvent {
                                if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                                    focusRequesters[0].requestFocus()
                                    true
                                } else false
                            },
                        onClick = {
                            typeOfOrganizationViewModel.addOrganization(
                                typeOfOrganization = TypeOfOrganization(
                                    name = typeName.text,
                                    discountRate = discountRate
                                )
                            )
                            onClose()
                        },
                        text = "Dodaj",
                        buttonBackgroundColor = Color.Green
                    )

                }
            }
        }
    }
}