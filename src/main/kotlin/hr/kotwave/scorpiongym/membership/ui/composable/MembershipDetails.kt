package hr.kotwave.scorpiongym.membership.ui.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.membership.Membership
import hr.kotwave.scorpiongym.ui.custom.composable.CrudDetailsScaffold
import hr.kotwave.scorpiongym.ui.custom.composable.rememberSyncedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField

@Composable
fun MembershipDetails(
    membership: Membership,
    onBackClick: () -> Unit,
    onUpdateClick: (Membership) -> Unit,
) {
    val focusRequesters = List(6) { FocusRequester() }

    var name by rememberSyncedTextField(membership, membership.name)
    var price by rememberSyncedTextField(membership, membership.price.toString())
    var duration by rememberSyncedTextField(membership, membership.duration.toString())

    val initialNumberOfTrainings =
        if (membership.numberOfTrainingsAvailable != Int.MAX_VALUE) membership.numberOfTrainingsAvailable.toString() else "0"
    var numberOfTrainings by rememberSyncedTextField(membership, initialNumberOfTrainings)

    var isNoLimit by remember(membership) { mutableStateOf(membership.isNoLimit) }

    LaunchedEffect(membership) {
        isNoLimit = membership.isNoLimit
    }

    CrudDetailsScaffold(
        title = "Članarina",
        isNew = membership.id == 0,
        onBack = onBackClick,
        onSave = {
            onUpdateClick(
                membership.copy(
                    name = name.text,
                    price = price.text.toDouble(),
                    numberOfTrainingsAvailable = if (isNoLimit) Int.MAX_VALUE else numberOfTrainings.text.toInt(),
                    duration = duration.text.toLong(),
                    isNoLimit = isNoLimit,
                )
            )
        },
        firstFocusRequester = focusRequesters[0],
        saveFocusRequester = focusRequesters[4],
    ) {
        FocusableOutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = "Naziv članarine",
            currentFocusRequester = focusRequesters[0],
            nextFocusRequester = focusRequesters[1],
        )
        FocusableOutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = "Cijena članarine",
            currentFocusRequester = focusRequesters[1],
            nextFocusRequester = focusRequesters[2],
            isMaxWidth = false,
        )
        FocusableOutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = "Duljina članarine(u mjesecima)",
            currentFocusRequester = focusRequesters[2],
            nextFocusRequester = focusRequesters[3],
            isMaxWidth = false,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FocusableOutlinedTextField(
                value = numberOfTrainings,
                onValueChange = { numberOfTrainings = it },
                label = "Broj dozvoljenih treninga",
                currentFocusRequester = focusRequesters[3],
                nextFocusRequester = focusRequesters[4],
                isMaxWidth = false,
                readOnly = isNoLimit,
            )

            Spacer(modifier = Modifier.width(30.dp))

            Checkbox(
                checked = isNoLimit,
                onCheckedChange = {
                    numberOfTrainings = TextFieldValue("0", selection = TextRange("0".length))
                    isNoLimit = !isNoLimit
                },
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("No limit")
        }
    }
}
