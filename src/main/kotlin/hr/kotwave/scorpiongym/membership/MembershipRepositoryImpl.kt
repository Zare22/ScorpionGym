package hr.kotwave.scorpiongym.membership

class MembershipRepositoryImpl(private val membershipDao: MembershipDao)  : MembershipRepository {
    override fun getAllMemberships(): List<Membership> = membershipDao.getAllMemberships()
    override fun getMembershipById(id: Int): Membership? = membershipDao.getMembershipById(id)
    override fun insertMembership(membership: Membership) = membershipDao.insertMembership(membership)
    override fun updateMembership(membership: Membership) = membershipDao.updateMembership(membership)
    override fun deleteMembership(id: Int) = membershipDao.deleteMembership(id)
}