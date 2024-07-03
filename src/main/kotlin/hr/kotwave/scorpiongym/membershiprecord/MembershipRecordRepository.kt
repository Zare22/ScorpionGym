package hr.kotwave.scorpiongym.membershiprecord

interface MembershipRecordRepository {
    fun getAllMembershipRecords(): List<MembershipRecord>
    fun getMembershipRecordById(id: Int): MembershipRecord?
    fun insertMembershipRecord(record: MembershipRecord): Int
    fun updateMembershipRecord(record: MembershipRecord)
    fun deleteMembershipRecord(id: Int)
    fun getMembersMembershipRecords(id: Int): List<MembershipRecord>
}