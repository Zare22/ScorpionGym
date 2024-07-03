package hr.kotwave.scorpiongym.di

import hr.kotwave.scorpiongym.database.DatabaseFactory
import hr.kotwave.scorpiongym.member.*
import hr.kotwave.scorpiongym.memberotherservice.MemberOtherServiceDao
import hr.kotwave.scorpiongym.memberotherservice.MemberOtherServiceDaoImpl
import hr.kotwave.scorpiongym.memberotherservice.MemberOtherServiceViewModel
import hr.kotwave.scorpiongym.membership.MembershipDao
import hr.kotwave.scorpiongym.membership.MembershipDaoImpl
import hr.kotwave.scorpiongym.membership.MembershipViewModel
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecordDao
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecordDaoImpl
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecordViewModel
import hr.kotwave.scorpiongym.organization.OrganizationDao
import hr.kotwave.scorpiongym.organization.OrganizationDaoImpl
import hr.kotwave.scorpiongym.organization.OrganizationViewModel
import hr.kotwave.scorpiongym.otherservice.OtherServiceDao
import hr.kotwave.scorpiongym.otherservice.OtherServiceDaoImpl
import hr.kotwave.scorpiongym.otherservice.OtherServiceViewModel
import hr.kotwave.scorpiongym.status.StatusDao
import hr.kotwave.scorpiongym.status.StatusDaoImpl
import hr.kotwave.scorpiongym.status.StatusViewModel
import hr.kotwave.scorpiongym.trainingsession.TrainingSessionDao
import hr.kotwave.scorpiongym.trainingsession.TrainingSessionDaoImpl
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationDao
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationDaoImpl
import hr.kotwave.scorpiongym.typeoforganization.TypeOfOrganizationViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single { DatabaseFactory.connect() }

    // DAO bindings
    single<MemberDao> { MemberDaoImpl(get()) }
    single<MembershipDao> { MembershipDaoImpl(get()) }
    single<MembershipRecordDao> { MembershipRecordDaoImpl(get()) }
    single<OrganizationDao> { OrganizationDaoImpl(get()) }
    single<TypeOfOrganizationDao> { TypeOfOrganizationDaoImpl(get()) }
    single<StatusDao> { StatusDaoImpl(get()) }
    single<TrainingSessionDao> { TrainingSessionDaoImpl(get()) }
    single<OtherServiceDao> { OtherServiceDaoImpl(get()) }
    single<MemberOtherServiceDao> { MemberOtherServiceDaoImpl(get()) }

    // ViewModel bindings
    scope(named("MemberScope")) {
        scoped { (member: Member) -> MemberViewModel(member, get(), get(), get(), get()) }
    }
    factory { (member: Member) -> MemberViewModel(member, get(), get(), get(), get()) }
    single { MembersListViewModel(get()) }
    single { MembershipViewModel(get()) }
    single { MembershipRecordViewModel(get()) }
    single { OrganizationViewModel(get()) }
    single { TypeOfOrganizationViewModel(get()) }
    single { StatusViewModel(get()) }
    single { OtherServiceViewModel(get()) }
    single { MemberOtherServiceViewModel(get()) }
}
