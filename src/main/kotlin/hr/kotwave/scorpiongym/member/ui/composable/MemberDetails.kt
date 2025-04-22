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
import androidx.compose.ui.graphics.Color
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
import hr.kotwave.scorpiongym.member.Gender
import hr.kotwave.scorpiongym.member.Member
import hr.kotwave.scorpiongym.member.MemberViewModel
import hr.kotwave.scorpiongym.member.MembersListViewModel
import hr.kotwave.scorpiongym.membership.Membership
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecord
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecordDao
import hr.kotwave.scorpiongym.organization.Organization
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import hr.kotwave.scorpiongym.ui.custom.dialog.InformativeDialog
import hr.kotwave.scorpiongym.ui.custom.elements.Dropdown
import hr.kotwave.scorpiongym.ui.custom.elements.FocusableOutlinedTextField
import hr.kotwave.scorpiongym.ui.custom.elements.HoverableButton
import hr.kotwave.scorpiongym.ui.theme.Gold
import hr.kotwave.scorpiongym.ui.theme.Shapes
import hr.kotwave.scorpiongym.util.Locales
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate
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

    val focusRequesters = List(9) { FocusRequester() }
    val verticalScrollState = rememberScrollState(0)

    var showInfoDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }

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

    val dateOfBirthFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    var dateOfBirth by remember(memberViewModel.currentMember) {
        mutableStateOf(
            memberViewModel.currentMember.dateOfBirth?.let {
                TextFieldValue(it.format(dateOfBirthFormatter))
            } ?: TextFieldValue("")
        )
    }

    var gender by remember(memberViewModel.currentMember) {
        mutableStateOf(memberViewModel.currentMember.gender)
    }

    var signedUpDate by remember(memberViewModel.currentMember) { mutableStateOf(memberViewModel.currentMember.signedUpDate) }
    var organization by remember(memberViewModel.currentMember) { mutableStateOf(memberViewModel.currentMember.organizationId.toString()) }

    var membership by remember(memberViewModel.currentMember) {
        mutableStateOf(memberViewModel.activeMembershipRecord?.membershipId.toString())
    }

    var currentRecordFinishDate by remember(memberViewModel.currentMember) { mutableStateOf(memberViewModel.activeMembershipRecord?.dateFinished) }
    var currentRecordStartDate by remember(memberViewModel.currentMember) { mutableStateOf(memberViewModel.activeMembershipRecord?.dateStarted) }

    // Dropdown states
    var expandedMembership by remember { mutableStateOf(false) }
    var expandedOrganization by remember { mutableStateOf(false) }

    // Renew dialog
    var renewMembershipDialogOpened by remember { mutableStateOf(false) }
    var showSameMemberDialog by remember { mutableStateOf(false) }
    var expiredMembershipDialogOpened by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    var dateFinished by remember(memberViewModel.currentMember) {
        mutableStateOf(
            currentRecordFinishDate?.format(dateFormatter) ?: "Nema trenutno aktivne članarine"
        )
    }

    var dateStarted by remember(memberViewModel.currentMember) {
        mutableStateOf(
            currentRecordFinishDate?.format(dateFormatter) ?: "Nema trenutno aktivne članarine"
        )
    }

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
        currentRecordFinishDate = memberViewModel.activeMembershipRecord?.dateFinished
        currentRecordStartDate = memberViewModel.activeMembershipRecord?.dateStarted
        dateFinished = currentRecordFinishDate?.format(dateFormatter) ?: "Nema trenutno aktivne članarine"
        dateStarted = currentRecordStartDate?.format(dateFormatter) ?: "Nema trenutno aktivne članarine"

        dateOfBirth = TextFieldValue(
            memberViewModel.currentMember.dateOfBirth?.format(dateOfBirthFormatter) ?: ""
        )

        gender = memberViewModel.currentMember.gender
    }

    when {

        showInfoDialog -> {
            InformativeDialog(infoMessage) { showInfoDialog = false }
        }

        showSameMemberDialog -> {
            AlertDialog(
                onDismissRequest = { showSameMemberDialog = false },
                title = { Text("Obavijest", color = Gold) },
                text = { Text("Već postoji član s istim imenom i prezimenom. Dodajte datum rođenja ili nadimak kako bi se razlikovali") },
                confirmButton = {
                    HoverableButton(
                        text = "Potvrdi",
                        buttonBackgroundColor = Color.Green,
                        onClick = {
                            val updatedMember = memberViewModel.currentMember.copy(
                                name = name.text,
                                surname = surname.text,
                                phoneNumber = phoneNumber.text,
                                signedUpDate = signedUpDate,
                                membershipRecordId = memberViewModel.currentMember.membershipRecordId,
                                organizationId = organization.toIntOrNull()
                                    ?: memberViewModel.currentMember.organizationId,
                                remark = remark.text,
                                dateOfBirth = runCatching {
                                    LocalDate.parse(
                                        dateOfBirth.text,
                                        dateOfBirthFormatter
                                    )
                                }.getOrNull(),
                            )
                            try {
                                memberViewModel.updateMember(updatedMember)
                            } catch (e: Exception) {
                                infoMessage = "Greška pri ažuriranju člana"
                                showInfoDialog = true
                            }
                            onUpdateClick(updatedMember)
                            showSameMemberDialog = false
                        }
                    )
                },
                dismissButton = {
                    HoverableButton(
                        text = "Odustani",
                        onClick = { showSameMemberDialog = false }
                    )
                }
            )
        }

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

                                    append(" ćete upisati članarinu ")

                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(selectedMembership?.name)
                                    }

                                    append(" sa početkom na datum: ")

                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(
                                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy."))
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
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
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
                                        try {
                                            memberViewModel.addNewMembershipRecord(
                                                MembershipRecord(
                                                    id = 0,
                                                    memberId = memberViewModel.currentMember.id,
                                                    membershipId = membership.toInt(),
                                                    isActive = true,
                                                    isPaid = isPaid,
                                                    dateFinished = selectedMembership?.let {
                                                        LocalDate.now().plusMonths(it.duration).minusDays(1)
                                                    } ?: LocalDate.now().plusMonths(1).minusDays(1)
                                                )
                                            )
                                        } catch (e: Exception) {
                                            infoMessage = "Greška pri produživanju članarine"
                                            showInfoDialog = true
                                        }
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
                                        try {
                                            onUpdateClick(updatedMember)
                                        } catch (e: Exception) {
                                            infoMessage = "Greška pri ažuriranju člana"
                                            showInfoDialog = true
                                        }
                                        memberViewModel.initViewModel()
                                        currentRecordFinishDate =
                                            memberViewModel.memberRecords.find { membershipRecord -> membershipRecord.isActive }?.dateFinished
                                        currentRecordStartDate =
                                            memberViewModel.memberRecords.find { membershipRecord -> membershipRecord.isActive }?.dateStarted
                                        dateFinished = currentRecordFinishDate?.format(dateFormatter)
                                            ?: "Nema trenutno aktivne članarine"
                                        dateStarted = currentRecordStartDate?.format(dateFormatter)
                                            ?: "Nema trenutno aktivne članarine"
                                        renewMembershipDialogOpened = false
                                    },
                                    text = "Obnovi",
                                    buttonBackgroundColor = Color.Green
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
                FocusableOutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { newValue ->
                        dateOfBirth = newValue
                    },
                    label = "Datum rođenja",
                    currentFocusRequester = focusRequesters[3],
                    nextFocusRequester = focusRequesters[4]
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Gender.entries.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = gender == option,
                                onCheckedChange = { isChecked ->
                                    gender = if (isChecked) option else null
                                }
                            )
                            Text(text = option.label)
                        }
                    }
                }

                OutlinedTextField(
                    value = signedUpDate.format(dateFormatter),
                    onValueChange = {},
                    label = { Text("Datum učlanjenja") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    readOnly = true
                )



                memberViewModel.currentMember.id.takeIf { it != 0 }?.let {

                    if (memberViewModel.activeMembershipRecord != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = dateStarted,
                                onValueChange = { newValue ->
                                    dateStarted = newValue
                                    val parsedDateTime = runCatching {
                                        LocalDate.parse(newValue, dateFormatter)
                                    }.getOrNull()

                                    currentRecordStartDate = parsedDateTime
                                },
                                label = { Text("Datum početka trenutne članarine") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                readOnly = memberViewModel.activeMembershipRecord == null
                            )

                            OutlinedTextField(
                                value = dateFinished,
                                onValueChange = { newValue ->
                                    dateFinished = newValue
                                    val parsedDateTime = runCatching {
                                        LocalDate.parse(newValue, dateFormatter)
                                    }.getOrNull()

                                    currentRecordFinishDate = parsedDateTime
                                },
                                label = { Text("Datum isteka trenutne članarine") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                readOnly = memberViewModel.activeMembershipRecord == null
                            )
                        }
                    }


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
                            focusRequester = focusRequesters[4],
                            nextFocusRequester = focusRequesters[5],
                            itemLabel = { it.name }
                        )

                        if (memberViewModel.activeMembershipRecord == null) {
                            HoverableButton(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .focusRequester(focusRequesters[5])
                                    .onPreviewKeyEvent {
                                        if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                                            focusRequesters[6].requestFocus()
                                            true
                                        } else false
                                    },
                                onClick = { renewMembershipDialogOpened = true },
                                text = "Obnovi članarinu"
                            )
                        }
                    }
                }

                val nextFocusIndex = when {
                    memberViewModel.currentMember.id == 0 -> 4
                    memberViewModel.currentMember.id != 0 && memberViewModel.activeMembershipRecord != null -> 5
                    else -> 6
                }
                Dropdown(
                    expanded = expandedOrganization,
                    onExpandedChange = { expandedOrganization = it },
                    label = "Organizacija",
                    items = organizations,
                    selectedItem = organizations.find { it.id.toString() == organization },
                    onItemSelected = { organization = it.id.toString() },
                    focusRequester = focusRequesters[nextFocusIndex],
                    nextFocusRequester = focusRequesters[nextFocusIndex + 1],
                    itemLabel = { it.name }
                )

                FocusableOutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = "Napomena",
                    currentFocusRequester = focusRequesters[nextFocusIndex + 1],
                    nextFocusRequester = focusRequesters[nextFocusIndex + 2],
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
                            .focusRequester(focusRequesters[nextFocusIndex + 2])
                            .onPreviewKeyEvent {
                                if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                                    focusRequesters[0].requestFocus()
                                    true
                                } else false
                            },
                        onClick = {
                            if (getKoin().get<MembersListViewModel>().members.any {
                                    it.id != memberViewModel.currentMember.id &&
                                            it.name.lowercase(Locales.CroatianLocale) == name.text.lowercase(
                                        Locales.CroatianLocale
                                    ) && it.surname.lowercase(Locales.CroatianLocale) == surname.text.lowercase(
                                        Locales.CroatianLocale
                                    )
                                }) {
                                showSameMemberDialog = true
                            } else {
                                val updatedMember = memberViewModel.currentMember.copy(
                                    name = name.text,
                                    surname = surname.text,
                                    phoneNumber = phoneNumber.text,
                                    signedUpDate = signedUpDate,
                                    membershipRecordId = memberViewModel.currentMember.membershipRecordId,
                                    organizationId = organization.toIntOrNull()
                                        ?: memberViewModel.currentMember.organizationId,
                                    remark = remark.text,
                                    dateOfBirth = kotlin.runCatching {
                                        LocalDate.parse(
                                            dateOfBirth.text,
                                            dateOfBirthFormatter
                                        )
                                    }.getOrNull(),
                                    gender = gender
                                )
                                if (memberViewModel.activeMembershipRecord != null) {
                                    val id = memberViewModel.activeMembershipRecord?.id
                                    val index = memberViewModel.memberRecords.indexOfFirst { it.id == id }
                                    memberViewModel.updateMembershipRecord(
                                        index,
                                        memberViewModel.activeMembershipRecord!!.copy(
                                            membershipId = membership.toInt(),
                                            dateStarted = currentRecordStartDate!!,
                                            dateFinished = currentRecordFinishDate!!
                                        )
                                    )
                                    memberViewModel.confirmMembershipRecordsUpdates()
                                    if (memberViewModel.trainingSessionsInActiveMembership.size >= memberViewModel.numberOfTrainingsAvailable) {
                                        val membershipRecordDao: MembershipRecordDao = getKoin().get()
                                        memberViewModel.activeMembershipRecord?.copy(isActive = false, dateFinished = LocalDate.now())
                                            ?.let { membershipRecordDao.updateMembershipRecord(it) }
                                        memberViewModel.initViewModel()
                                        expiredMembershipDialogOpened = true
                                    }
                                }
                                try {
                                    memberViewModel.updateMember(updatedMember)
                                } catch (e: Exception) {
                                    infoMessage = "Greška pri ažuriranju člana"
                                    showInfoDialog = true
                                }
                                onUpdateClick(updatedMember)
                            }
                        },
                        text = if (memberViewModel.currentMember.id != 0) "Ažuriraj" else "Dodaj",
                        buttonBackgroundColor = Color.Green
                    )
                }
            }
            if (expiredMembershipDialogOpened) {
                InformativeDialog(
                    message = "Članu ${memberViewModel.currentMember.name} ${memberViewModel.currentMember.surname} je istekla trenutna članarina",
                    onDismiss = {
                        expiredMembershipDialogOpened = false
                    }
                )
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(verticalScrollState),
            )
        }
    }
}