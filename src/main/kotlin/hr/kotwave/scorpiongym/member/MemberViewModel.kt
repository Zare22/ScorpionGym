package hr.kotwave.scorpiongym.member

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import hr.kotwave.scorpiongym.memberotherservice.MemberOtherService
import hr.kotwave.scorpiongym.memberotherservice.MemberOtherServiceDao
import hr.kotwave.scorpiongym.membership.MembershipDao
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecord
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecordDao
import hr.kotwave.scorpiongym.trainingsession.TrainingSession
import hr.kotwave.scorpiongym.trainingsession.TrainingSessionDao
import org.koin.core.component.KoinComponent
import java.time.LocalDateTime

class MemberViewModel(
    private val member: Member,
    private val membershipDao: MembershipDao,
    private val membershipRecordDao: MembershipRecordDao,
    private val trainingSessionDao: TrainingSessionDao,
    private val memberOtherServiceDao: MemberOtherServiceDao
) : KoinComponent {

    val currentMember: Member get() = member

    private val _memberRecords = mutableStateListOf<MembershipRecord>()
    val memberRecords: List<MembershipRecord> get() = _memberRecords

    var activeMembershipRecord by mutableStateOf<MembershipRecord?>(null)
        private set

    private val _trainingSessionsInActiveMembership = mutableStateListOf<TrainingSession>()
    val trainingSessionsInActiveMembership: SnapshotStateList<TrainingSession> get() = _trainingSessionsInActiveMembership

    var numberOfTrainingsAvailable by mutableStateOf(0)
        private set

    init {
        initMembersRecords()
    }

    fun initMembersRecords() {
        val loadedRecords = membershipRecordDao.getMembersMembershipRecords(member.id)
        _memberRecords.clear()
        _trainingSessionsInActiveMembership.clear()
        numberOfTrainingsAvailable = 0
        _memberRecords.addAll(loadedRecords)
        activeMembershipRecord = memberRecords.find { membershipRecord -> membershipRecord.isActive }
        if (activeMembershipRecord != null) {
            val membership = membershipDao.getMembershipById(activeMembershipRecord!!.membershipId)
            numberOfTrainingsAvailable = membership?.numberOfTrainingsAvailable ?: 0
            val trainingSessions = trainingSessionDao.getAllTrainingSessionsForMembershipRecord(activeMembershipRecord!!.id)
            _trainingSessionsInActiveMembership.addAll(trainingSessions)
        }
    }

    fun addNewMembershipRecord(membershipRecord: MembershipRecord) {
        member.membershipRecordId = membershipRecordDao.insertMembershipRecord(membershipRecord)
        _memberRecords.add(membershipRecord)
    }

    fun addNewTrainingSessionToMember() {
        if (member.membershipRecordId == null) throw Exception("Ne postoji trenutno aktivna članarina")
        val trainingSession =
            TrainingSession(membershipRecordId = member.membershipRecordId!!, sessionDateTime = LocalDateTime.now())
        trainingSessionDao.insertTrainingSession(trainingSession)
    }

    fun addNewMemberOtherService(otherServiceId: Int) {
        val memberOtherService = MemberOtherService(
            memberId = member.id,
            otherServiceId = otherServiceId,
            dateOfService = LocalDateTime.now()
        )
        memberOtherServiceDao.insertMemberOtherService(memberOtherService)
    }

    fun confirmTrainingSessionUpdates() {
        _trainingSessionsInActiveMembership.forEach { session ->
            if (session.id == 0) {
                val id = trainingSessionDao.insertTrainingSession(session)
                session.id = id
            } else
                trainingSessionDao.updateTrainingSession(session)
        }
    }

    fun addTrainingSession(newSession: TrainingSession) {
        if (_trainingSessionsInActiveMembership.size < numberOfTrainingsAvailable) {
            _trainingSessionsInActiveMembership.add(newSession)
        }
    }

    fun updateTrainingSession(index: Int, updatedSession: TrainingSession) {
        if (index in _trainingSessionsInActiveMembership.indices) {
            _trainingSessionsInActiveMembership[index] = updatedSession
        }
    }

    fun removeTrainingSessionsWithoutId() {
        _trainingSessionsInActiveMembership.removeIf { training -> training.id == 0 }
    }

    fun updateMembershipRecordsIsPaid(index: Int, updatedMembershipRecord: MembershipRecord) {
        if (index in _memberRecords.indices) {
            _memberRecords[index] = updatedMembershipRecord
        }
    }

    fun confirmMembershipRecordsIsPaid() {
        _memberRecords.forEach { record ->
            membershipRecordDao.updateMembershipRecord(record)
        }
    }
}