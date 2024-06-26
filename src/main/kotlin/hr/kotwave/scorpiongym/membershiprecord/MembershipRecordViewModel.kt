package hr.kotwave.scorpiongym.membershiprecord

import org.koin.core.component.KoinComponent

class MembershipRecordViewModel(private val membershipRecordDao: MembershipRecordDao) : KoinComponent {
    private val _membershipRecords = mutableListOf<MembershipRecord>()
    val membershipRecords: List<MembershipRecord> get() = _membershipRecords

    init {
        getAllRecords()
    }

    private fun getAllRecords() {
        val loadedMembershipRecords = membershipRecordDao.getAllMembershipRecords()
        _membershipRecords.addAll(loadedMembershipRecords)
    }
}