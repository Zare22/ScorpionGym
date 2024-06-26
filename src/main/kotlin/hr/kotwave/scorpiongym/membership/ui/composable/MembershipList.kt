package hr.kotwave.scorpiongym.membership.ui.composable

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
import hr.kotwave.scorpiongym.membership.Membership

@Composable
fun MembershipList(memberships: List<Membership>, onItemClick: (Membership) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pretraži tip članarine") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        val filteredMemberships = if (searchQuery.isEmpty()) {
            memberships
        } else {
            memberships.filter { membership ->
                membership.name.contains(searchQuery, ignoreCase = true)
            }
        }

        LazyColumn {
            items(filteredMemberships) { membership ->
                MembershipItem(membership = membership, onClick = { onItemClick(membership) })
            }
        }
    }
}


@Composable
fun MembershipItem(membership: Membership, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand, false),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = membership.name, style = MaterialTheme.typography.h6)
        }
    }
}