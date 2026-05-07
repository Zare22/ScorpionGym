package hr.kotwave.scorpiongym.ui.custom.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.CustomBackIcon
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val SLIDE_DURATION_MS = 450

/**
 * Standard chrome for a CRUD screen: a master/details split with the entity
 * list on the left and a sliding details pane on the right. Owns the state
 * machine (selected entity, create-vs-edit flag, animation visibility), the
 * error dialog, and the save try/catch wrapping calls into [onAdd]/[onUpdate].
 *
 * Caller owns the empty-entity factory, the per-entity copy strings, and the
 * list and details composables (passed as slots).
 */
@Composable
fun <T> CrudScreenScaffold(
    emptyEntity: () -> T,
    addButtonText: String,
    createErrorMessage: String,
    updateErrorMessage: String,
    onAdd: (T) -> Unit,
    onUpdate: (T) -> Unit,
    list: @Composable (onItemClick: (T) -> Unit) -> Unit,
    details: @Composable (entity: T, onBack: () -> Unit, onSave: (T) -> Unit) -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()

    var selected by remember { mutableStateOf<T?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var detailsVisible by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

    if (showInfoDialog) {
        InformativeDialog(infoMessage) { showInfoDialog = false }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    CustomBackIcon(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        navigator = navigator,
                    )
                    HoverableButton(
                        onClick = {
                            selected = emptyEntity()
                            isCreatingNew = true
                            detailsVisible = true
                        },
                        text = addButtonText,
                    )
                }
                list { entity ->
                    selected = entity
                    isCreatingNew = false
                    detailsVisible = true
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier.fillMaxHeight().weight(2f),
            visible = detailsVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(SLIDE_DURATION_MS, easing = LinearEasing),
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(SLIDE_DURATION_MS, easing = LinearEasing),
            ),
        ) {
            selected?.let { entity ->
                details(
                    entity,
                    {
                        detailsVisible = false
                        coroutineScope.launch {
                            delay(SLIDE_DURATION_MS.milliseconds)
                            selected = null
                        }
                    },
                    { updated ->
                        if (isCreatingNew) {
                            try {
                                onAdd(updated)
                                isCreatingNew = false
                            } catch (_: Exception) {
                                infoMessage = createErrorMessage
                                showInfoDialog = true
                            }
                        } else {
                            try {
                                onUpdate(updated)
                            } catch (_: Exception) {
                                infoMessage = updateErrorMessage
                                showInfoDialog = true
                            }
                        }
                        selected = updated
                    },
                )
            }
        }
    }
}
