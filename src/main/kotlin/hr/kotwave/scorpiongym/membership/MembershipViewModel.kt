package hr.kotwave.scorpiongym.membership

import androidx.compose.runtime.mutableStateListOf
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecordDao
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
        membershipDao.insertMembership(membership)
        _memberships.add(membership)
    }

    fun removeMembership(membership: Membership) {
        membershipDao.deleteMembership(membership.id)
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