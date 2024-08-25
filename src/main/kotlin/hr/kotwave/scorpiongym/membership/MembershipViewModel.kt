package hr.kotwave.scorpiongym.membership

import androidx.compose.runtime.mutableStateListOf
import org.koin.core.component.KoinComponent

class MembershipViewModel(private val membershipDao: MembershipDao) : KoinComponent {
    private val _memberships = mutableStateListOf<Membership>()
    val memberships: List<Membership> get() = _memberships

    init {
        getMemberships()
    }

    private fun getMemberships() {
        val loadedMemberships = membershipDao.getAllMemberships()
        _memberships.addAll(loadedMemberships)
    }

    fun addMembership(membership: Membership) {
        val membershipId = membershipDao.insertMembership(membership)
        membership.id = membershipId
        _memberships.add(membership)
    }

    fun removeMembership(membership: Membership) {
        membershipDao.deleteMembership(membership)
        _memberships.remove(membership)
    }

    fun updateMembership(membership: Membership) {
        membershipDao.updateMembership(membership)
        val index = _memberships.indexOfFirst { it.id == membership.id }
        if (index != -1) {
            _memberships[index] = membership
        }
    }

}