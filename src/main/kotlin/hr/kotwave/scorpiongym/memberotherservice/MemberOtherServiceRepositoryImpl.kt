package hr.kotwave.scorpiongym.memberotherservice

class MemberOtherServiceRepositoryImpl(private val memberOtherServiceDao: MemberOtherServiceDao) : MemberOtherServiceRepository {
    override fun getAllMemberOtherServices(): List<MemberOtherService> = memberOtherServiceDao.getAllMemberOtherServices()
    override fun getMemberOtherServiceById(id: Int): MemberOtherService? = memberOtherServiceDao.getMemberOtherServiceById(id)
    override fun insertMemberOtherService(memberOtherService: MemberOtherService) = memberOtherServiceDao.insertMemberOtherService(memberOtherService)
    override fun updateMemberOtherService(memberOtherService: MemberOtherService) = memberOtherServiceDao.updateMemberOtherService(memberOtherService)
    override fun deleteMemberOtherServiceById(id: Int) = memberOtherServiceDao.deleteMemberOtherServiceById(id)
}