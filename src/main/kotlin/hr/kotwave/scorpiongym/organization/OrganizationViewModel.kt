package hr.kotwave.scorpiongym.organization

import androidx.compose.runtime.mutableStateListOf
import org.koin.core.component.KoinComponent

class OrganizationViewModel(private val organizationDao: OrganizationDao) : KoinComponent {
    private val _organizations = mutableStateListOf<Organization>()
    val organizations: List<Organization> get() = _organizations

    init {
        getOrganizations()
    }

    private fun getOrganizations() {
        val loadedOrganizations = organizationDao.getAllOrganizations()
        _organizations.addAll(loadedOrganizations)
    }

    fun addOrganization(organization: Organization) {
        organization.id = organizationDao.insertOrganization(organization)
        _organizations.add(organization)
    }

    fun deleteOrganization(organization: Organization) {
        organizationDao.deleteOrganization(organization.id)
        _organizations.remove(organization)
    }

    fun updateOrganization(organization: Organization) {
        organizationDao.updateOrganization(organization)
        val index = _organizations.indexOfFirst { it.id == organization.id }
        if (index != -1) {
            _organizations[index] = organization
        }
    }
}