package hr.kotwave.scorpiongym.membership

interface MembershipRepository {
    fun getAllMemberships(): List<Membership>
    fun getMembershipById(id:Int): Membership?
    fun insertMembership(membership: Membership): Int
    fun updateMembership(membership: Membership)
    fun deleteMembership(membership: Membership)
}