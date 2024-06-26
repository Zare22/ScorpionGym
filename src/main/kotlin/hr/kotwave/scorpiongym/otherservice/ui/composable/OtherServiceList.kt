package hr.kotwave.scorpiongym.otherservice.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.otherservice.OtherService
import hr.kotwave.scorpiongym.otherservice.OtherServiceViewModel
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun OtherServiceList(onItemClick: (OtherService) -> Unit) {
    val otherServiceViewModel: OtherServiceViewModel = getKoin().get()
    var searchQuery by remember { mutableStateOf("") }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pretraži ostale usluge") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        val filteredOtherServices = if (searchQuery.isEmpty()) {
            otherServiceViewModel.otherServices
        } else {
            otherServiceViewModel.otherServices.filter { otherService ->
                otherService.name.contains(searchQuery, ignoreCase = true)
            }
        }

        LazyColumn {
            items(filteredOtherServices) { otherService ->
                OtherServiceItem(otherService = otherService, onClick = { onItemClick(otherService) })
            }
        }
    }
}

@Composable
fun OtherServiceItem(otherService: OtherService, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand, false),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(otherService.name, style = MaterialTheme.typography.h6)
        }
    }
}