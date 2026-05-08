package hr.kotwave.scorpiongym.memberotherservice

import androidx.compose.runtime.mutableStateListOf

class MemberOtherServiceViewModel(private val memberOtherServiceDao: MemberOtherServiceDao) {
    private val _memberOtherServices = mutableStateListOf<MemberOtherService>()
    val memberOtherServices: List<MemberOtherService> get() = _memberOtherServices

    init {
        getMemberOtherServices()
    }

    private fun getMemberOtherServices() {
        val loadedMemberOtherService = memberOtherServiceDao.getAllMemberOtherServices()
        _memberOtherServices.addAll(loadedMemberOtherService)
    }

    fun addMemberOtherService(memberOtherService: MemberOtherService) {
        memberOtherServiceDao.insertMemberOtherService(memberOtherService)
        _memberOtherServices.add(memberOtherService)
    }

    fun deleteMemberOtherService(memberOtherService: MemberOtherService) {
        memberOtherServiceDao.deleteMemberOtherService(memberOtherService)
        _memberOtherServices.remove(memberOtherService)
    }
}