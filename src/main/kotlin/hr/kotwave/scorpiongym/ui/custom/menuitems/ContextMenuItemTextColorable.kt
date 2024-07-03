package hr.kotwave.scorpiongym.ui.custom.menuitems

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.dzirbel.contextmenu.ContextMenuParams
import com.dzirbel.contextmenu.CustomContentContextMenuItem

class ContextMenuItemTextColorable(private val customLabel: String, private val color: Color, onClick: () -> Unit) : CustomContentContextMenuItem(onClick) {
    @Composable
    override fun Content(onDismissRequest: () -> Unit, params: ContextMenuParams) {
        Text(customLabel, color = color)
    }
}