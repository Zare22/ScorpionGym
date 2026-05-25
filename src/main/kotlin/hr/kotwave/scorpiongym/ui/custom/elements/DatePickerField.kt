package hr.kotwave.scorpiongym.ui.custom.elements

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

/**
 * A read-only text field that opens a Material 3 [DatePickerDialog] when clicked.
 *
 * The rest of the app uses Material 2; this is the one component pulled from M3
 * because M2 has no date picker. The dialog content is wrapped in an M3
 * [MaterialTheme] whose [androidx.compose.material3.ColorScheme] is derived from
 * the surrounding M2 palette, so the picker visually matches the rest of the app.
 *
 * Date math is done in UTC at the boundary to avoid off-by-one-day errors when
 * the local timezone shifts midnight relative to UTC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDateSelectable: (LocalDate) -> Boolean = { true },
    yearRange: IntRange = DatePickerDefaults.YearRange,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) dialogOpen = true
    }

    OutlinedTextField(
        value = value?.format(DISPLAY_FORMATTER) ?: "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier,
    )

    if (dialogOpen) {
        val initialMillis = value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            yearRange = yearRange,
            selectableDates = remember(isDateSelectable) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        isDateSelectable(Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate())
                }
            },
        )

        MatchedM3Theme {
            DatePickerDialog(
                onDismissRequest = { dialogOpen = false },
                confirmButton = {
                    TextButton(onClick = {
                        onValueChange(pickerState.selectedDateMillis?.let { millis ->
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        })
                        dialogOpen = false
                    }) { Text("Odaberi") }
                },
                dismissButton = {
                    TextButton(onClick = { dialogOpen = false }) { Text("Odustani") }
                },
            ) {
                DatePicker(
                    state = pickerState,
                    title = null,
                    headline = {
                        val text = pickerState.selectedDateMillis?.let { millis ->
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                                .format(DISPLAY_FORMATTER)
                        } ?: "Odaberi datum"
                        Text(
                            text = text,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 20.dp, bottom = 12.dp),
                        )
                    },
                    showModeToggle = false,
                )
            }
        }
    }
}

