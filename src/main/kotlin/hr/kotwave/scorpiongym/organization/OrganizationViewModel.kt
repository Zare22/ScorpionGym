package hr.kotwave.scorpiongym.organization

import hr.kotwave.scorpiongym.util.CrudViewModel

class OrganizationViewModel(dao: OrganizationDao) : CrudViewModel<Organization>(
    loader = dao::getAllOrganizations,
    inserter = dao::insertOrganization,
    updater = dao::updateOrganization,
    deleter = dao::deleteOrganization,
) {
    val organizations: List<Organization> get() = items

    fun addOrganization(organization: Organization) = add(organization)
    fun updateOrganization(organization: Organization) = update(organization)
    fun deleteOrganization(organization: Organization) = delete(organization)
}
