package hr.kotwave.scorpiongym.membership

import hr.kotwave.scorpiongym.util.CrudViewModel

class MembershipViewModel(dao: MembershipDao) : CrudViewModel<Membership>(
    loader = dao::getAllMemberships,
    inserter = dao::insertMembership,
    updater = dao::updateMembership,
    deleter = dao::deleteMembership,
) {
    val memberships: List<Membership> get() = items

    fun addMembership(membership: Membership) = add(membership)
    fun updateMembership(membership: Membership) = update(membership)
    fun deleteMembership(membership: Membership) = delete(membership)
}
