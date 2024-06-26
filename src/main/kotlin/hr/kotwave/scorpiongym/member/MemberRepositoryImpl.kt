package hr.kotwave.scorpiongym.member

class MemberRepositoryImpl(private val memberDao: MemberDao) : MemberRepository {
    override fun getAllMembers(): List<Member>  = memberDao.getAllMembers()
    override fun getMemberById(id: Int): Member? = memberDao.getMemberById(id)
    override fun insertMember(member: Member): Int = memberDao.insertMember(member)
    override fun updateMember(member: Member) = memberDao.updateMember(member)
    override fun deleteMember(id: Int) = memberDao.deleteMember(id)
}