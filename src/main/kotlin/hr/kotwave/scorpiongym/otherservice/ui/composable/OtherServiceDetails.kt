package hr.kotwave.scorpiongym.otherservice.ui.composable


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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.otherservice.OtherService
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.theme.Shapes

@Composable
fun OtherServiceDetails(
    otherService: OtherService,
    onBackClick: () -> Unit,
    onUpdateClick: (OtherService) -> Unit
) {
    val focusRequesters = List(3) { FocusRequester() }

    var name by remember(otherService) {
        mutableStateOf(
            TextFieldValue(
                otherService.name,
                selection = TextRange(otherService.name.length)
            )
        )
    }
    var price by remember { mutableStateOf(0.0) }
    var priceInput by remember { mutableStateOf(TextFieldValue(price.toString())) }


    LaunchedEffect(otherService) {
        name = TextFieldValue(otherService.name, selection = TextRange(otherService.name.length))
        price = otherService.price
        priceInput = TextFieldValue(otherService.price.toString())
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
                    Text(text = "Ostala usluga", style = MaterialTheme.typography.h5)
                }
                Spacer(modifier = Modifier.height(16.dp))

                FocusableOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Ime usluge",
                    currentFocusRequester = focusRequesters[0],
                    nextFocusRequester = focusRequesters[1]
                )

                FocusableOutlinedTextField(
                    value = priceInput,
                    onValueChange = { input ->
                        val filteredInput = input.text.filter { it.isDigit() || it == '.' }

                        if (filteredInput.count { it == '.' } <= 1) {
                            priceInput = input.copy(text = filteredInput)
                            price = filteredInput.toDoubleOrNull() ?: 0.0
                        }
                    },
                    label = "Cijena",
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
                        val updatedOtherService = otherService.copy(
                            name = name.text,
                            price = price
                        )
                        onUpdateClick(updatedOtherService)
                    }, text = if (otherService.id != 0) "Ažuriraj" else "Dodaj"
                    )
                }
            }
        }
    }
}