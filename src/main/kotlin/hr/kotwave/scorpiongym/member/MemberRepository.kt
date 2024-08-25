package hr.kotwave.scorpiongym.member

interface MemberRepository {
    fun getAllMembers(): List<Member>
    fun getMemberById(id: Int): Member?
    fun insertMember(member: Member): Int
    fun updateMember(member: Member)
    fun deleteMember(member: Member)
}