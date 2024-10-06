package hr.kotwave.scorpiongym.paymentauditlog

import hr.kotwave.scorpiongym.appuser.AppUser
import hr.kotwave.scorpiongym.memberotherservice.MemberOtherService
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecord
import hr.kotwave.scorpiongym.unregisteredservice.UnregisteredService
import java.time.LocalDate

data class PaymentAuditLog(
    val id: Int,
    val membershipRecord: MembershipRecord?,
    val memberOtherService: MemberOtherService?,
    val unregisteredService: UnregisteredService?,
    val isPaidOld: Boolean?,
    val isPaidNew: Boolean?,
    val price: Double?,
    val changedAt: LocalDate?,
    val appUser: AppUser?,
    val isUnregisteredServiceMembership: Boolean?
)