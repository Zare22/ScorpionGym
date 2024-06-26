package hr.kotwave.scorpiongym.organization.ui.composable

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
import hr.kotwave.scorpiongym.organization.Organization
import hr.kotwave.scorpiongym.organization.OrganizationViewModel
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun OrganizationList(onItemClick: (Organization) -> Unit) {
    val organizationViewModel: OrganizationViewModel = getKoin().get()
    var searchQuery by remember { mutableStateOf("") }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pretraži organizaciju") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        val filteredOrganizations = if (searchQuery.isEmpty()) {
            organizationViewModel.organizations
        } else {
            organizationViewModel.organizations.filter { organization ->
                organization.name.contains(searchQuery, ignoreCase = true)
            }
        }

        LazyColumn {
            items(filteredOrganizations) { organization ->
                OrganizationItem(organization = organization, onClick = { onItemClick(organization) })
            }
        }
    }
}


@Composable
fun OrganizationItem(organization: Organization, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand, false),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = organization.name, style = MaterialTheme.typography.h6)
        }
    }
}