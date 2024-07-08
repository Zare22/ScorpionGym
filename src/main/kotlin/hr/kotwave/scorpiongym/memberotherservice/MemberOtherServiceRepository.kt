package hr.kotwave.scorpiongym.memberotherservice

interface MemberOtherServiceRepository {
    fun getAllMemberOtherServices(): List<MemberOtherService>
    fun getMemberOtherServiceById(id:Int): MemberOtherService?
    fun insertMemberOtherService(memberOtherService: MemberOtherService): Int
    fun updateMemberOtherService(memberOtherService: MemberOtherService)
    fun deleteMemberOtherServiceById(id: Int)
    fun getMembersOtherServices(memberId: Int): List<MemberOtherService>
}