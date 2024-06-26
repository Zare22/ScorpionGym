package hr.kotwave.scorpiongym.member.ui.composable

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.membership.Membership
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecord
import hr.kotwave.scorpiongym.organization.Organization
import hr.kotwave.scorpiongym.status.Status
import hr.kotwave.scorpiongym.ui.theme.Shapes
import hr.kotwave.scorpiongym.ui.custom.elements.Dropdown
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.getKoin
import java.time.format.DateTimeFormatter

@Composable
fun MemberDetails(
    member: Member,
    memberships: List<Membership>,
    statuses: List<Status>,
    organizations: List<Organization>,
    onBackClick: () -> Unit,
    onUpdateClick: (Member) -> Unit
) {

    val memberViewModel: MemberViewModel = getKoin().get {  parametersOf(member) }

    //Elements that can gain focus
    val focusRequesters = List(8) { FocusRequester() }
    val verticalScrollState = rememberScrollState(0)

    // Member properties
    var name by remember(member) { mutableStateOf(TextFieldValue(member.name, selection = TextRange(member.name.length))) }
    var surname by remember(member) { mutableStateOf(TextFieldValue(member.surname, selection = TextRange(member.surname.length))) }
    var phoneNumber by remember(member) { mutableStateOf(TextFieldValue(member.phoneNumber ?: "", selection = TextRange((member.phoneNumber ?: "").length))) }
    var signedUpDate by remember(member) { mutableStateOf(member.signedUpDate) }
    var membership by remember(member) { mutableStateOf(member.membershipRecordId.toString()) }
    var organization by remember(member) { mutableStateOf(member.organizationId.toString()) }
    var status by remember(member) { mutableStateOf(member.statusId.toString()) }
    var remark by remember(member) { mutableStateOf(TextFieldValue(member.remark ?: "", selection = TextRange(member.remark?.length ?: 0))) }

    var currentRecordFinished by remember { mutableStateOf(memberViewModel.memberRecords.find { membershipRecord -> membershipRecord.isActive }?.dateFinished) }

    // Dropdown states
    var expandedMembership by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedOrganization by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'u' HH:mm")

    LaunchedEffect(member) {
        name = TextFieldValue(member.name, selection = TextRange(member.name.length))
        surname = TextFieldValue(member.surname, selection = TextRange(member.surname.length))
        phoneNumber = TextFieldValue(member.phoneNumber ?: "", selection = TextRange((member.phoneNumber ?: "").length))
        membership = member.membershipRecordId.toString()
        organization = member.organizationId.toString()
        status = member.statusId.toString()
        remark = TextFieldValue(member.remark ?: "", selection = TextRange(member.remark?.length ?: 0))
        signedUpDate = member.signedUpDate
        currentRecordFinished = memberViewModel.memberRecords.find { membershipRecord -> membershipRecord.isActive }?.dateFinished
        focusRequesters[0].requestFocus()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        elevation = 4.dp,
        shape = Shapes.large,
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(verticalScrollState)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Detalji člana", style = MaterialTheme.typography.h5)

                }

                Spacer(modifier = Modifier.height(16.dp))

                FocusableOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Ime",
                    currentFocusRequester = focusRequesters[0],
                    nextFocusRequester = focusRequesters[1]
                )
                FocusableOutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = "Prezime",
                    currentFocusRequester = focusRequesters[1],
                    nextFocusRequester = focusRequesters[2]
                )
                FocusableOutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Broj telefona",
                    currentFocusRequester = focusRequesters[2],
                    nextFocusRequester = focusRequesters[3]
                )

                OutlinedTextField(
                    value = signedUpDate.format(dateFormatter),
                    onValueChange = {},
                    label = { Text("Datum učlanjenja") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    readOnly = true
                )

                OutlinedTextField(
                    value = currentRecordFinished?.format(dateFormatter) ?: "Nema trenutno aktivne članarine",
                    onValueChange = {},
                    label = { Text("Datum isteka trenutne članarine") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    readOnly = true
                )

                if(currentRecordFinished == null) {
                    var isPaid by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Dropdown(
                            modifier = Modifier.weight(1f),
                            expanded = expandedMembership,
                            onExpandedChange = { expandedMembership = it },
                            label = "Tip članarine",
                            items = memberships,
                            selectedItem = memberships.find { it.id.toString() == membership },
                            onItemSelected = { membership = it.id.toString() },
                            focusRequester = focusRequesters[3],
                            nextFocusRequester = focusRequesters[4],
                            itemLabel = { it.name }
                        )

                        Checkbox(
                            modifier = Modifier.wrapContentWidth(),
                            checked = isPaid,
                            onCheckedChange = { isPaid = !isPaid }
                        )

                        HoverableButton(
                            modifier = Modifier.wrapContentWidth(),
                            onClick = {
                                memberViewModel.addNewMembershipRecord(
                                    MembershipRecord(
                                        id = 0,
                                        memberId = member.id,
                                        membershipId = membership.toInt(),
                                        isActive = true,
                                        isPaid = isPaid
                                    )
                                )
                                currentRecordFinished = memberViewModel.memberRecords.find { membershipRecord -> membershipRecord.isActive }?.dateFinished
                            },
                            text = "Obnovi članarinu"
                        )
                    }
                }

                //Status člana --> Aktivan/neaktivan...
                Dropdown(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = it },
                    label = "Status člana",
                    items = statuses,
                    selectedItem = statuses.find { it.id.toString() == status },
                    onItemSelected = { status = it.id.toString() },
                    focusRequester = focusRequesters[4],
                    nextFocusRequester = focusRequesters[5],
                    itemLabel = { it.description }
                )

                //Organizacija kojoj član pripada --> Škola, fakultet, posao....
                Dropdown(
                    expanded = expandedOrganization,
                    onExpandedChange = { expandedOrganization = it },
                    label = "Organizacija",
                    items = organizations,
                    selectedItem = organizations.find { it.id.toString() == organization },
                    onItemSelected = { organization = it.id.toString() },
                    focusRequester = focusRequesters[5],
                    nextFocusRequester = focusRequesters[6],
                    itemLabel = { it.name }
                )

                FocusableOutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = "Napomena",
                    currentFocusRequester = focusRequesters[6],
                    nextFocusRequester = focusRequesters[7],
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HoverableButton(
                        onClick = { onBackClick() },
                        text = "Povratak"
                    )
                    HoverableButton(
                        modifier = Modifier
                            .focusRequester(focusRequesters[7])
                            .onPreviewKeyEvent {
                                if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                                    focusRequesters[0].requestFocus()
                                    true
                                } else false
                            },
                        onClick = {
                            val updatedMember = member.copy(
                                name = name.text,
                                surname = surname.text,
                                phoneNumber = phoneNumber.text,
                                signedUpDate = signedUpDate,
                                membershipRecordId = null,
                                organizationId = organization.toIntOrNull() ?: member.organizationId,
                                statusId = status.toIntOrNull() ?: member.statusId,
                                remark = remark.text
                            )
                            onUpdateClick(updatedMember)
                        },
                        text = if (member.id != 0) "Ažuriraj" else "Dodaj"
                    )
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(verticalScrollState),
            )
        }
    }
}