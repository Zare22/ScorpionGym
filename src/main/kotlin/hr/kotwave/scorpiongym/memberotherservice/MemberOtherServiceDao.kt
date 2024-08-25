package hr.kotwave.scorpiongym.memberotherservice

interface MemberOtherServiceDao {
    fun getAllMemberOtherServices(): List<MemberOtherService>
    fun getMemberOtherServiceById(id:Int): MemberOtherService?
    fun insertMemberOtherService(memberOtherService: MemberOtherService): Int
    fun updateMemberOtherService(memberOtherService: MemberOtherService)
    fun deleteMemberOtherServiceById(memberOtherService: MemberOtherService)
    fun getMembersOtherServices(memberId: Int): List<MemberOtherService>
}