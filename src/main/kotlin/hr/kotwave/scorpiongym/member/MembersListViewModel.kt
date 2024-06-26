package hr.kotwave.scorpiongym.member

import androidx.compose.runtime.mutableStateListOf
import org.koin.core.component.KoinComponent

class MembersListViewModel(private val memberDao: MemberDao) : KoinComponent {

    private val _members = mutableStateListOf<Member>()
    val members: List<Member> get() = _members

    init {
        getMembers()
    }

    private fun getMembers() {
        val loadedMembers = memberDao.getAllMembers()
        _members.addAll(loadedMembers)
    }

    fun addMember(member: Member) {
        val insertedId = memberDao.insertMember(member)
        val memberWithId = member.copy(id = insertedId)
        _members.add(memberWithId)
    }

    fun updateMember(member: Member) {
        memberDao.updateMember(member)
        val index = _members.indexOfFirst { it.id == member.id }
        if (index != -1) {
            _members[index] = member
        }
    }
}