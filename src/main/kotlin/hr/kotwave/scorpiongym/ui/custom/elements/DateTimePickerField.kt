package hr.kotwave.scorpiongym.ui.custom.elements

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.compose.material3.OutlinedTextField as M3OutlinedTextField
import androidx.compose.material3.Text as M3Text

private val DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * A read-only text field that opens a Material 3 dialog containing a
 * [DatePicker] plus a manual `HH:mm` time text input. On confirm, the date
 * and time are combined into a single [LocalDateTime] passed back.
 *
 * The dialog is built on a plain [Dialog] + [Surface] rather than M3's
 * [androidx.compose.material3.DatePickerDialog], because the latter caps its
 * height at the mobile-sized ~568dp and clips the buttons off the bottom
 * when an extra control is stacked under the calendar.
 *
 * The time is entered manually (typed) rather than via M3's `TimePicker`,
 * which is mobile-first and visually heavy on desktop. Typing `14:30` is
 * faster than clicking an analog clock.
 *
 * The confirm button is disabled while the time field is empty or unparseable.
 *
 * Date math is done in UTC at the boundary to avoid off-by-one-day errors when
 * the local timezone shifts midnight relative to UTC. Time is interpreted as
 * the literal wall-clock time the user typed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    value: LocalDateTime?,
    onValueChange: (LocalDateTime?) -> Unit,
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
        val seed = value ?: LocalDateTime.now()
        val initialDateMillis = seed.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis,
            yearRange = yearRange,
            selectableDates = remember(isDateSelectable) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        isDateSelectable(Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate())
                }
            },
        )
        var timeText by remember { mutableStateOf(seed.toLocalTime().format(TIME_FORMATTER)) }
        val parsedTime by remember(timeText) {
            derivedStateOf { runCatching { LocalTime.parse(timeText, TIME_FORMATTER) }.getOrNull() }
        }

        MatchedM3Theme {
            Dialog(onDismissRequest = { dialogOpen = false }) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 6.dp,
                ) {
                    Column {
                        DatePicker(
                            state = datePickerState,
                            title = null,
                            headline = {
                                val text = datePickerState.selectedDateMillis?.let { millis ->
                                    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().format(DATE_FORMATTER)
                                } ?: "Odaberi datum"
                                M3Text(
                                    text = text,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 20.dp, bottom = 12.dp),
                                )
                            },
                            showModeToggle = false,
                        )
                        M3OutlinedTextField(
                            value = timeText,
                            onValueChange = { newValue ->
                                timeText = newValue.filter { it.isDigit() || it == ':' }.take(5)
                            },
                            label = { M3Text("Vrijeme (HH:mm)") },
                            isError = timeText.isNotEmpty() && parsedTime == null,
                            singleLine = true,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth(0.5f)
                                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { dialogOpen = false }) { M3Text("Odustani") }
                            TextButton(
                                onClick = {
                                    val pickedDate = datePickerState.selectedDateMillis?.let { millis ->
                                        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                                    }
                                    val pickedTime = parsedTime
                                    if (pickedDate != null && pickedTime != null) {
                                        onValueChange(pickedDate.atTime(pickedTime))
                                    }
                                    dialogOpen = false
                                },
                                enabled = parsedTime != null,
                            ) { M3Text("Odaberi") }
                        }
                    }
                }
            }
        }
    }
}
