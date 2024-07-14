package hr.kotwave.scorpiongym.typeoforganization.ui.composable

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
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.util.Locales
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun TypeOfOrganizationList(onItemClick: (TypeOfOrganization) -> Unit) {
    val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()
    var searchQuery by remember { mutableStateOf("") }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pretraži tip organizacije") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        val filteredTypeOfOrganizations = if (searchQuery.isEmpty()) {
            typeOfOrganizationViewModel.organizationTypes.sortedWith(compareBy { it.name.lowercase(Locales.CroatianLocale) })
        } else {
            typeOfOrganizationViewModel.organizationTypes.sortedWith(compareBy { it.name.lowercase(Locales.CroatianLocale) })
                .filter { organization ->
                    organization.name.contains(searchQuery, ignoreCase = true)
                }
        }

        LazyColumn {
            items(filteredTypeOfOrganizations) { typeOfOrganization ->
                TypeOfOrganizationItem(typeOfOrganization = typeOfOrganization, onClick = { onItemClick(typeOfOrganization) })
            }
        }
    }
}

@Composable
fun TypeOfOrganizationItem(typeOfOrganization: TypeOfOrganization, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand, false),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = typeOfOrganization.name, style = MaterialTheme.typography.h6)
        }
    }
}