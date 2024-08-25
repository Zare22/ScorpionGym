package hr.kotwave.scorpiongym.memberotherservice

import androidx.compose.runtime.mutableStateListOf

class MemberOtherServiceViewModel(private val memberOtherServiceDao: MemberOtherServiceDao) {
    private val _memberOtherService = mutableStateListOf<MemberOtherService>()
    val memberOtherService: List<MemberOtherService> get() = _memberOtherService

    init {
        getMemberOtherServices()
    }

    private fun getMemberOtherServices() {
        val loadedMemberOtherService = memberOtherServiceDao.getAllMemberOtherServices()
        _memberOtherService.addAll(loadedMemberOtherService)
    }

    fun addMemberOtherService(memberOtherService: MemberOtherService) {
        memberOtherServiceDao.insertMemberOtherService(memberOtherService)
        _memberOtherService.add(memberOtherService)
    }

    fun deleteMemberOtherService(memberOtherService: MemberOtherService) {
        memberOtherServiceDao.deleteMemberOtherServiceById(memberOtherService)
        _memberOtherService.remove(memberOtherService)
    }
}