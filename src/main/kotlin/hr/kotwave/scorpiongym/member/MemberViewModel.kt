package hr.kotwave.scorpiongym.member

import hr.kotwave.scorpiongym.memberotherservice.MemberOtherService
import hr.kotwave.scorpiongym.memberotherservice.MemberOtherServiceDao
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecord
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecordDao
import hr.kotwave.scorpiongym.trainingsession.TrainingSession
import hr.kotwave.scorpiongym.trainingsession.TrainingSessionDao
import org.koin.core.component.KoinComponent
import java.time.LocalDateTime

class MemberViewModel(
    private val member: Member,
    private val membershipRecordDao: MembershipRecordDao,
    private val trainingSessionDao: TrainingSessionDao,
    private val memberOtherServiceDao: MemberOtherServiceDao
) : KoinComponent {

    private val _memberRecords = mutableListOf<MembershipRecord>()
    val memberRecords: List<MembershipRecord> get() = _memberRecords

    init {
        initMembersRecords()
    }

    private fun initMembersRecords() {
        val loadedRecords = membershipRecordDao.getMembersMembershipRecords(member.id)
        _memberRecords.clear()
        _memberRecords.addAll(loadedRecords)
    }

    fun addNewMembershipRecord(membershipRecord: MembershipRecord) {
        membershipRecordDao.insertMembershipRecord(membershipRecord)
        _memberRecords.add(membershipRecord)
    }

    fun addNewTrainingSessionToMember() {
        if (member.membershipRecordId == null) throw Exception("Ne postoji trenutno aktivna članarina")
        val trainingSession =
            TrainingSession(membershipRecordId = member.membershipRecordId, sessionDateTime = LocalDateTime.now())
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
}