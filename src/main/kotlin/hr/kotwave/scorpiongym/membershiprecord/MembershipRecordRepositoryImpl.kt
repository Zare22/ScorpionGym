package hr.kotwave.scorpiongym.membershiprecord

class MembershipRecordRepositoryImpl(private val membershipRecordDao: MembershipRecordDao) : MembershipRecordRepository {
    override fun getAllMembershipRecords(): List<MembershipRecord> = membershipRecordDao.getAllMembershipRecords()
    override fun getMembershipRecordById(id: Int): MembershipRecord? = membershipRecordDao.getMembershipRecordById(id)
    override fun insertMembershipRecord(record: MembershipRecord) = membershipRecordDao.insertMembershipRecord(record)
    override fun updateMembershipRecord(record: MembershipRecord) = membershipRecordDao.updateMembershipRecord(record)
    override fun deleteMembershipRecord(id: Int) = membershipRecordDao.deleteMembershipRecord(id)
    override fun getMembersMembershipRecords(id: Int): List<MembershipRecord> = membershipRecordDao.getMembersMembershipRecords(id)
}