package hr.kotwave.scorpiongym.organization

interface OrganizationRepository {
    fun getAllOrganizations(): List<Organization>
    fun getOrganizationById(id:Int): Organization?
    fun insertOrganization(organization: Organization)
    fun updateOrganization(organization: Organization)
    fun deleteOrganization(id: Int)
}