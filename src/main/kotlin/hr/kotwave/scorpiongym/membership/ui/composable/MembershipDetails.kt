package hr.kotwave.scorpiongym.membership.ui.composable

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
import hr.kotwave.scorpiongym.membership.Membership
import hr.kotwave.scorpiongym.ui.theme.Shapes
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton

@Composable
fun MembershipDetails(
    membership: Membership,
    onBackClick: () -> Unit,
    onUpdateClick: (Membership) -> Unit
) {
    val focusRequesters = List(4) { FocusRequester() }

    var name by remember(membership) {
        mutableStateOf(
            TextFieldValue(
                membership.name,
                selection = TextRange(membership.name.length)
            )
        )
    }
    var price by remember(membership) {
        mutableStateOf(
            TextFieldValue(
                membership.price.toString(),
                selection = TextRange(membership.price.toString().length)
            )
        )
    }
    var numberOfTrainings by remember(membership) {
        mutableStateOf(
            TextFieldValue(
                membership.numberOfTrainingsAvailable.toString(),
                selection = TextRange(membership.numberOfTrainingsAvailable.toString().length)
            )
        )
    }

    LaunchedEffect(membership) {
        name = TextFieldValue(membership.name, selection = TextRange(membership.name.length))
        price = TextFieldValue(membership.price.toString(), selection = TextRange(membership.price.toString().length))
        numberOfTrainings = TextFieldValue(membership.numberOfTrainingsAvailable.toString(), selection = TextRange(membership.numberOfTrainingsAvailable.toString().length))
        focusRequesters[0].requestFocus()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        elevation = 4.dp,
        shape = Shapes.large,
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Članarina", style = MaterialTheme.typography.h5)
                }
                Spacer(modifier = Modifier.height(16.dp))

                FocusableOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Naziv članarine",
                    currentFocusRequester = focusRequesters[0],
                    nextFocusRequester = focusRequesters[1]
                )
                FocusableOutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = "Cijena članarine",
                    currentFocusRequester = focusRequesters[1],
                    nextFocusRequester = focusRequesters[2]
                )
                FocusableOutlinedTextField(
                    value = numberOfTrainings,
                    onValueChange = { numberOfTrainings = it },
                    label = "Broj dozvoljenih treninga",
                    currentFocusRequester = focusRequesters[2],
                    nextFocusRequester = focusRequesters[3]
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HoverableButton(
                        onClick = { onBackClick() },
                        text = "Povratak"
                    )
                    HoverableButton(
                        modifier = Modifier
                            .focusRequester(focusRequesters[3])
                            .onPreviewKeyEvent {
                                if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                                    focusRequesters[0].requestFocus()
                                    true
                                } else false
                            },
                        onClick = {
                            val updatedMembership = membership.copy(
                                name = name.text,
                                price = price.text.toDouble(),
                                numberOfTrainingsAvailable = numberOfTrainings.text.toInt()
                            )
                            onUpdateClick(updatedMembership)
                        },
                        text = if (membership.id != 0) "Ažuriraj" else "Dodaj"
                    )
                }
            }
        }
    }
}