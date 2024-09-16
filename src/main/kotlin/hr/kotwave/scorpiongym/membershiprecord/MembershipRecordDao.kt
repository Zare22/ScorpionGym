package hr.kotwave.scorpiongym.membershiprecord

interface MembershipRecordDao {
    fun getAllMembershipRecords(): List<MembershipRecord>
    fun getMembershipRecordById(id: Int): MembershipRecord?
    fun insertMembershipRecord(record: MembershipRecord): Int
    fun updateMembershipRecord(record: MembershipRecord)
    fun deleteMembershipRecord(membershipRecord: MembershipRecord)
    fun getMembersMembershipRecords(id: Int): List<MembershipRecord>
    fun deleteAllTrainingsAssociatedWithRecord(membershipRecord: MembershipRecord)
    fun validateMemberships()
}
