package hr.kotwave.scorpiongym.report.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.ui.custom.elements.DatePickerField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import java.time.LocalDate

/**
 * Shared "Od / Do / Ovaj mjesec / Prikaži" filter row used by every report
 * section. "Ovaj mjesec" sets the bounds to the current calendar month and
 * immediately triggers [onShow].
 */
@Composable
fun PeriodPickerRow(
    from: LocalDate?,
    to: LocalDate?,
    onFromChange: (LocalDate?) -> Unit,
    onToChange: (LocalDate?) -> Unit,
    onShow: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        DatePickerField(
            value = from,
            onValueChange = onFromChange,
            label = "Od",
            modifier = Modifier.weight(1f),
        )
        DatePickerField(
            value = to,
            onValueChange = onToChange,
            label = "Do",
            modifier = Modifier.weight(1f).padding(start = 16.dp),
        )
        HoverableButton(
            text = "Ovaj mjesec",
            onClick = {
                val now = LocalDate.now()
                onFromChange(now.withDayOfMonth(1))
                onToChange(now.withDayOfMonth(now.lengthOfMonth()))
                onShow()
            },
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        HoverableButton(
            text = "Prikaži",
            onClick = onShow,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
    }
}
