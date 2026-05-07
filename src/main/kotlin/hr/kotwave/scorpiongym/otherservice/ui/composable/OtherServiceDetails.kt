package hr.kotwave.scorpiongym.otherservice.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.TextFieldValue
import hr.kotwave.scorpiongym.otherservice.OtherService
import hr.kotwave.scorpiongym.ui.custom.composable.CrudDetailsScaffold
import hr.kotwave.scorpiongym.ui.custom.composable.rememberSyncedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField

@Composable
fun OtherServiceDetails(
    otherService: OtherService,
    onBackClick: () -> Unit,
    onUpdateClick: (OtherService) -> Unit,
) {
    val focusRequesters = List(3) { FocusRequester() }

    var name by rememberSyncedTextField(otherService, otherService.name)
    var price by remember { mutableStateOf(0.0) }
    var priceInput by remember { mutableStateOf(TextFieldValue(price.toString())) }

    LaunchedEffect(otherService) {
        price = otherService.price
        priceInput = TextFieldValue(otherService.price.toString())
        focusRequesters[0].requestFocus()
    }

    CrudDetailsScaffold(
        title = "Ostala usluga",
        isNew = otherService.id == 0,
        onBack = onBackClick,
        onSave = {
            onUpdateClick(
                otherService.copy(
                    name = name.text,
                    price = price,
                )
            )
        },
        firstFocusRequester = focusRequesters[0],
        saveFocusRequester = focusRequesters[2],
    ) {
        FocusableOutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = "Ime usluge",
            currentFocusRequester = focusRequesters[0],
            nextFocusRequester = focusRequesters[1],
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
            nextFocusRequester = focusRequesters[2],
        )
    }
}
