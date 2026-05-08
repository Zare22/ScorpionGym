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

class MemberDetailsViewModel(
    member: Member,
    private val membershipDao: MembershipDao,
    private val membershipRecordDao: MembershipRecordDao,
    private val trainingSessionDao: TrainingSessionDao,
    private val memberOtherServiceDao: MemberOtherServiceDao
) : KoinComponent {

    private val _currentMember = mutableStateOf(member)
    val currentMember: Member get() = _currentMember.value

    private val _memberRecords = mutableStateListOf<MembershipRecord>()
    val memberRecords: List<MembershipRecord> get() = _memberRecords

    private val _memberOtherServices = mutableStateListOf<MemberOtherService>()
    val memberOtherServices: List<MemberOtherService> get() = _memberOtherServices

    var activeMembershipRecord by mutableStateOf<MembershipRecord?>(null)
        private set

    private val _trainingSessionsInActiveMembership = mutableStateListOf<TrainingSession>()
    val trainingSessionsInActiveMembership: SnapshotStateList<TrainingSession> get() = _trainingSessionsInActiveMembership

    private val modifiedSessions = mutableListOf<TrainingSession>()
    private val modifiedRecords = mutableListOf<MembershipRecord>()
    private val modifiedMemberOtherServices = mutableListOf<MemberOtherService>()

    var numberOfTrainingsAvailable by mutableStateOf(0)
        private set

    init {
        initViewModel()
    }

    fun initViewModel() {
        _memberRecords.clear()
        _trainingSessionsInActiveMembership.clear()
        _memberOtherServices.clear()
        numberOfTrainingsAvailable = 0
        _memberRecords.addAll(membershipRecordDao.getMembershipRecordsForMember(currentMember.id))
        activeMembershipRecord = memberRecords.find { membershipRecord -> membershipRecord.isActive }
        if (activeMembershipRecord != null) {
            val membership = membershipDao.getMembershipById(activeMembershipRecord!!.membershipId)
            numberOfTrainingsAvailable = membership?.numberOfTrainingsAvailable ?: 0

            _trainingSessionsInActiveMembership.addAll(
                trainingSessionDao.getAllTrainingSessionsForMembershipRecord(
                    activeMembershipRecord!!.id
                )
            )
        }
        _memberOtherServices.addAll(memberOtherServiceDao.getOtherServicesForMember(currentMember.id))
    }

    fun addNewMembershipRecord(membershipRecord: MembershipRecord) {
        currentMember.membershipRecordId = membershipRecordDao.insertMembershipRecord(membershipRecord)
        _memberRecords.add(membershipRecord)
    }

    fun addFutureMembershipRecord(membershipRecord: MembershipRecord) = _memberRecords.add(membershipRecord)
    fun removeUnconfirmedFutureMembershipRecords() = _memberRecords.removeIf { membershipRecord -> membershipRecord.id == 0 }
    fun removeUnconfirmedMemberOtherServices() = _memberOtherServices.removeIf { memberOtherService -> memberOtherService.id == 0 }

    fun addNewTrainingSessionToMember() {
        if (activeMembershipRecord == null) throw Exception("Ne postoji trenutno aktivna članarina")
        val trainingSession =
            TrainingSession(membershipRecordId = activeMembershipRecord!!.id, sessionDateTime = LocalDateTime.now())
        trainingSession.id = trainingSessionDao.insertTrainingSession(trainingSession)
        _trainingSessionsInActiveMembership.add(trainingSession)
    }

    fun addNewMemberOtherService(memberOtherService: MemberOtherService, temporary: Boolean = false) {
        if (!temporary) memberOtherService.id = memberOtherServiceDao.insertMemberOtherService(memberOtherService)
        _memberOtherServices.add(memberOtherService)
    }

    fun addTrainingSession(newSession: TrainingSession) {
        if (_trainingSessionsInActiveMembership.size < numberOfTrainingsAvailable) {
            _trainingSessionsInActiveMembership.add(newSession)
        }
    }

    fun updateTrainingSession(index: Int, updatedSession: TrainingSession) {
        if (index in _trainingSessionsInActiveMembership.indices) {
            _trainingSessionsInActiveMembership[index] = updatedSession
            val existingSession = modifiedSessions.find { it.id == updatedSession.id }

            if (existingSession != null) {
                val existingIndex = modifiedSessions.indexOf(existingSession)
                modifiedSessions[existingIndex] = updatedSession
            } else if (updatedSession.id != 0)
                modifiedSessions.add(updatedSession)
        }
    }

    fun updateMembershipRecord(index: Int, updatedMembershipRecord: MembershipRecord) {
        if (index in _memberRecords.indices) {
            _memberRecords[index] = updatedMembershipRecord
            val existingRecord = modifiedRecords.find { it.id == updatedMembershipRecord.id }

            if (existingRecord != null) {
                val existingIndex = modifiedRecords.indexOf(existingRecord)
                modifiedRecords[existingIndex] = updatedMembershipRecord
            } else if (updatedMembershipRecord.id != 0)
                modifiedRecords.add(updatedMembershipRecord)
        }
    }

    fun updateMember(updatedMember: Member) {
        _currentMember.value = updatedMember
    }

    fun updateMemberOtherService(index: Int, updatedMemberOtherService: MemberOtherService) {
        if (index in _memberOtherServices.indices) {

            _memberOtherServices[index] = updatedMemberOtherService
            val existingService = modifiedMemberOtherServices.find { it.id == updatedMemberOtherService.id}

            if (existingService != null) {
                val existingIndex = modifiedMemberOtherServices.indexOf(existingService)
                modifiedMemberOtherServices[existingIndex] = updatedMemberOtherService
            } else if (updatedMemberOtherService.id != 0)
                modifiedMemberOtherServices.add(updatedMemberOtherService)
        }
    }

    fun deleteTrainingSession(trainingSession: TrainingSession) {
        _trainingSessionsInActiveMembership.remove(trainingSession)
        if (trainingSession.id != 0)
            trainingSessionDao.deleteTrainingSession(trainingSession)
    }

    fun removeTrainingSessionsWithoutId() {
        _trainingSessionsInActiveMembership.removeIf { training -> training.id == 0 }
    }

    fun deleteMembershipRecord(record: MembershipRecord) {
        _memberRecords.remove(record)
        if (record.id != 0) {
            membershipRecordDao.deleteTrainingsForRecord(record)
            membershipRecordDao.deleteMembershipRecord(record)
        }
    }

    fun deleteMemberOtherService(memberOtherService: MemberOtherService) {
        _memberOtherServices.remove(memberOtherService)
        memberOtherServiceDao.deleteMemberOtherService(memberOtherService)
    }

    fun confirmTrainingSessionUpdates() {
        _trainingSessionsInActiveMembership.forEach { session ->
            if (session.id == 0) trainingSessionDao.insertTrainingSession(session)
        }
        modifiedSessions.forEach { session ->
            if (session.id != 0) trainingSessionDao.updateTrainingSession(session)
        }
        modifiedSessions.clear()
    }

    fun confirmMembershipRecordsUpdates() {
        _memberRecords.forEach { record ->
            if (record.id == 0) membershipRecordDao.insertMembershipRecord(record)
        }
        modifiedRecords.forEach { record ->
            if (record.id != 0) membershipRecordDao.updateMembershipRecord(record)
        }
        modifiedRecords.clear()
        initViewModel()
    }

    fun confirmMemberOtherServicesUpdates() {
        _memberOtherServices.forEach { memberOtherService ->
            if(memberOtherService.id == 0) memberOtherServiceDao.insertMemberOtherService(memberOtherService)
        }
        modifiedMemberOtherServices.forEach { modifiedMemberOtherService ->
            if (modifiedMemberOtherService.id != 0) memberOtherServiceDao.updateMemberOtherService(modifiedMemberOtherService)
        }
        modifiedMemberOtherServices.clear()
        initViewModel()
    }

    fun assignActiveMembershipRecord(membershipRecord: MembershipRecord) {
        activeMembershipRecord = membershipRecord
        _trainingSessionsInActiveMembership.clear()
        numberOfTrainingsAvailable = 0

        if (activeMembershipRecord != null) {
            val membership = membershipDao.getMembershipById(activeMembershipRecord!!.membershipId)
            numberOfTrainingsAvailable = membership?.numberOfTrainingsAvailable ?: 0

            _trainingSessionsInActiveMembership.addAll(
                trainingSessionDao.getAllTrainingSessionsForMembershipRecord(activeMembershipRecord!!.id)
            )
        }
    }

}