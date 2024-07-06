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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import hr.kotwave.scorpiongym.di.rememberMemberViewModel
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.membership.Membership
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecord
import hr.kotwave.scorpiongym.organization.Organization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.ui.custom.elements.Dropdown
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.theme.Shapes
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MemberDetails(
    member: Member,
    memberships: List<Membership>,
    organizations: List<Organization>,
    onBackClick: () -> Unit,
    onUpdateClick: (Member) -> Unit
) {

    val memberViewModel: MemberViewModel = rememberMemberViewModel(member)

    //Elements that can gain focus
    val focusRequesters = List(8) { FocusRequester() }
    val verticalScrollState = rememberScrollState(0)

    // Member properties
    var name by remember(memberViewModel.currentMember) {
        mutableStateOf(
            TextFieldValue(
                memberViewModel.currentMember.name,
                selection = TextRange(memberViewModel.currentMember.name.length)
            )
        )
    }
    var surname by remember(memberViewModel.currentMember) {
        mutableStateOf(
            TextFieldValue(
                memberViewModel.currentMember.surname,
                selection = TextRange(memberViewModel.currentMember.surname.length)
            )
        )
    }
    var phoneNumber by remember(memberViewModel.currentMember) {
        mutableStateOf(
            TextFieldValue(
                memberViewModel.currentMember.phoneNumber ?: "",
                selection = TextRange((memberViewModel.currentMember.phoneNumber ?: "").length)
            )
        )
    }
    var remark by remember(memberViewModel.currentMember) {
        mutableStateOf(
            TextFieldValue(
                memberViewModel.currentMember.remark ?: "",
                selection = TextRange(memberViewModel.currentMember.remark?.length ?: 0)
            )
        )
    }

    var signedUpDate by remember(memberViewModel.currentMember) { mutableStateOf(memberViewModel.currentMember.signedUpDate) }
    var organization by remember(memberViewModel.currentMember) { mutableStateOf(memberViewModel.currentMember.organizationId.toString()) }

    var membership by remember(memberViewModel.currentMember) {
        mutableStateOf(memberViewModel.activeMembershipRecord?.membershipId.toString())
    }
    var currentRecordFinishDate by remember { mutableStateOf(memberViewModel.activeMembershipRecord?.dateFinished) }

    // Dropdown states
    var expandedMembership by remember { mutableStateOf(false) }
    var expandedOrganization by remember { mutableStateOf(false) }

    // Renew dialog
    var renewMembershipDialogOpened by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'u' HH:mm")

    LaunchedEffect(memberViewModel.currentMember) {
        name = TextFieldValue(
            memberViewModel.currentMember.name,
            selection = TextRange(memberViewModel.currentMember.name.length)
        )
        surname = TextFieldValue(
            memberViewModel.currentMember.surname,
            selection = TextRange(memberViewModel.currentMember.surname.length)
        )
        phoneNumber = TextFieldValue(
            memberViewModel.currentMember.phoneNumber ?: "",
            selection = TextRange((memberViewModel.currentMember.phoneNumber ?: "").length)
        )
        organization = memberViewModel.currentMember.organizationId.toString()
        membership = memberViewModel.activeMembershipRecord?.membershipId.toString()
        remark = TextFieldValue(
            memberViewModel.currentMember.remark ?: "",
            selection = TextRange(memberViewModel.currentMember.remark?.length ?: 0)
        )
        signedUpDate = memberViewModel.currentMember.signedUpDate
        currentRecordFinishDate =
            memberViewModel.memberRecords.find { membershipRecord -> membershipRecord.isActive }?.dateFinished
    }

    when {
        renewMembershipDialogOpened -> {
            Dialog(onDismissRequest = { renewMembershipDialogOpened = false }) {
                val selectedMembership = memberships.find { memb -> memb.id == membership.toIntOrNull() }
                var price = selectedMembership?.price
                var isPaid by remember { mutableStateOf(false) }

                if (organization != "") {
                    val selectedOrganization = organizations.find { org -> org.id == organization.toIntOrNull() }
                    val typeOfOrganizationViewModel: TypeOfOrganizationViewModel = getKoin().get()
                    val typeOfOrganization = typeOfOrganizationViewModel.organizationTypes.find { typeOfOrg ->
                        typeOfOrg.id == selectedOrganization?.typeOfOrganizationId
                    }
                    if (typeOfOrganization != null) {
                        val discountRate = typeOfOrganization.discountRate / 100.0
                        price?.let {
                            price = it * (1 - discountRate)
                        }
                    }
                }
                Card(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = buildAnnotatedString {
                                    append("Članu ")

                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("${memberViewModel.currentMember.surname} ${memberViewModel.currentMember.name}")
                                    }

                                    append(" ćete upisati člarinu ${selectedMembership?.name} sa početkom na datum: ")

                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(
                                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm"))
                                        )
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = buildAnnotatedString {
                                    append("Cijena članarine sa popustom iznosi: ")

                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("$price €")
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Checkbox(
                                    modifier = Modifier.wrapContentWidth(),
                                    checked = isPaid,
                                    onCheckedChange = { isPaid = !isPaid }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Plaćeno")
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                HoverableButton(
                                    modifier = Modifier.wrapContentWidth(),
                                    text = "Povratak",
                                    onClick = { renewMembershipDialogOpened = false }
                                )

                                HoverableButton(
                                    modifier = Modifier.wrapContentWidth(),
                                    onClick = {
                                        memberViewModel.addNewMembershipRecord(
                                            MembershipRecord(
                                                id = 0,
                                                memberId = memberViewModel.currentMember.id,
                                                membershipId = membership.toInt(),
                                                isActive = true,
                                                isPaid = isPaid
                                            )
                                        )
                                        val updatedMember = memberViewModel.currentMember.copy(
                                            name = name.text,
                                            surname = surname.text,
                                            phoneNumber = phoneNumber.text,
                                            signedUpDate = signedUpDate,
                                            membershipRecordId = memberViewModel.currentMember.membershipRecordId,
                                            organizationId = organization.toIntOrNull()
                                                ?: memberViewModel.currentMember.organizationId,
                                            remark = remark.text
                                        )
                                        onUpdateClick(updatedMember)
                                        memberViewModel.initMembersRecords()
                                        currentRecordFinishDate =
                                            memberViewModel.memberRecords.find { membershipRecord -> membershipRecord.isActive }?.dateFinished
                                        renewMembershipDialogOpened = false
                                    },
                                    text = "Obnovi"
                                )
                            }
                        }
                    }
                }
            }
        }
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
                    Text(text = "Detalji člana", style = MaterialTheme.typography.h2)

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
                    value = currentRecordFinishDate?.format(dateFormatter) ?: "Nema trenutno aktivne članarine",
                    onValueChange = {},
                    label = { Text("Datum isteka trenutne članarine") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    readOnly = true
                )


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
                        itemLabel = { it.name },
                        readOnly = memberViewModel.activeMembershipRecord != null
                    )
                    if (memberViewModel.activeMembershipRecord == null) {
                        HoverableButton(
                            modifier = Modifier
                                .wrapContentWidth()
                                .focusRequester(focusRequesters[4])
                                .onPreviewKeyEvent {
                                    if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                                        focusRequesters[5].requestFocus()
                                        true
                                    } else false
                                },
                            onClick = { renewMembershipDialogOpened = true },
                            text = "Obnovi članarinu"
                        )
                    }
                }

                //Organizacija kojoj član pripada --> Škola, fakultet, posao....
                Dropdown(
                    expanded = expandedOrganization,
                    onExpandedChange = { expandedOrganization = it },
                    label = "Organizacija",
                    items = organizations,
                    selectedItem = organizations.find { it.id.toString() == organization },
                    onItemSelected = { organization = it.id.toString() },
                    focusRequester = focusRequesters[if (memberViewModel.activeMembershipRecord == null) 5 else 4],
                    nextFocusRequester = focusRequesters[if (memberViewModel.activeMembershipRecord == null) 6 else 5],
                    itemLabel = { it.name }
                )

                FocusableOutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = "Napomena",
                    currentFocusRequester = focusRequesters[if (memberViewModel.activeMembershipRecord == null) 6 else 5],
                    nextFocusRequester = focusRequesters[if (memberViewModel.activeMembershipRecord == null) 7 else 6],
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
                            .focusRequester(focusRequesters[if (currentRecordFinishDate == null) 7 else 6])
                            .onPreviewKeyEvent {
                                if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                                    focusRequesters[0].requestFocus()
                                    true
                                } else false
                            },
                        onClick = {
                            val updatedMember = memberViewModel.currentMember.copy(
                                name = name.text,
                                surname = surname.text,
                                phoneNumber = phoneNumber.text,
                                signedUpDate = signedUpDate,
                                membershipRecordId = memberViewModel.currentMember.membershipRecordId,
                                organizationId = organization.toIntOrNull()
                                    ?: memberViewModel.currentMember.organizationId,
                                remark = remark.text
                            )
                            onUpdateClick(updatedMember)
                        },
                        text = if (memberViewModel.currentMember.id != 0) "Ažuriraj" else "Dodaj"
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