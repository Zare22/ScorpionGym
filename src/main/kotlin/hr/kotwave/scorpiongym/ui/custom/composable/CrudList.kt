package hr.kotwave.scorpiongym.ui.custom.composable

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.util.Locales
import hr.kotwave.scorpiongym.util.PreferencesHelper

/**
 * Localized strings for a [CrudList].
 *
 * Bundled because Croatian declensions vary across the strings, so they
 * cannot be derived from a single base label.
 */
data class CrudListLabels(
    val searchHint: String,
    val deleteDialogTitle: String,
    /** Trailing space expected — the entity name is appended in bold. */
    val deleteDialogBody: String,
    val deleteErrorMessage: String,
    val contextMenuLabel: String,
)

/**
 * Generic searchable list with admin-gated delete via context menu.
 *
 * Sorts and filters by [nameOf] using Croatian collation. Each row is a
 * [Card] whose body is rendered by [itemContent]; default shows the name
 * in `MaterialTheme.typography.h6`.
 */
@Composable
fun <T> CrudList(
    entities: List<T>,
    nameOf: (T) -> String,
    onItemClick: (T) -> Unit,
    onDelete: (T) -> Unit,
    labels: CrudListLabels,
    itemContent: @Composable (T) -> Unit = {
        Text(nameOf(it), style = MaterialTheme.typography.h6)
    },
) {
    var searchQuery by remember { mutableStateOf("") }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(labels.searchHint) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        val sorted = entities.sortedWith(compareBy { nameOf(it).lowercase(Locales.CroatianLocale) })
        val filtered = if (searchQuery.isEmpty()) sorted
        else sorted.filter { nameOf(it).contains(searchQuery, ignoreCase = true) }

        LazyColumn {
            items(filtered) { entity ->
                CrudListItem(
                    entity = entity,
                    nameOf = nameOf,
                    onClick = { onItemClick(entity) },
                    onDelete = { onDelete(entity) },
                    labels = labels,
                    content = itemContent,
                )
            }
        }
    }
}

@Composable
private fun <T> CrudListItem(
    entity: T,
    nameOf: (T) -> String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    labels: CrudListLabels,
    content: @Composable (T) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    when {
        showInfoDialog -> InformativeDialog(infoMessage) { showInfoDialog = false }

        showDeleteDialog -> AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(labels.deleteDialogTitle, color = Color.Red) },
            text = {
                Text(
                    text = buildAnnotatedString {
                        append(labels.deleteDialogBody)
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(nameOf(entity))
                        }
                    },
                )
            },
            confirmButton = {
                HoverableButton(
                    text = "Potvrdi",
                    buttonBackgroundColor = Color.Red,
                    onClick = {
                        try {
                            onDelete()
                        } catch (_: Exception) {
                            infoMessage = labels.deleteErrorMessage
                            showInfoDialog = true
                        }
                        showDeleteDialog = false
                    },
                )
            },
            dismissButton = {
                HoverableButton(
                    text = "Odustani",
                    onClick = { showDeleteDialog = false },
                )
            },
        )
    }

    ContextMenuArea(
        items = {
            val menuItems = mutableListOf<ContextMenuItem>()
            if (PreferencesHelper().isAdmin) {
                menuItems.add(ContextMenuItem(labels.contextMenuLabel) { showDeleteDialog = true })
            }
            menuItems
        },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable(onClick = onClick)
                .pointerHoverIcon(PointerIcon.Hand, false),
            elevation = 4.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content(entity)
            }
        }
    }
}
