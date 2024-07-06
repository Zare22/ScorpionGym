package hr.kotwave.scorpiongym.organization

interface OrganizationDao {
    fun getAllOrganizations(): List<Organization>
    fun getOrganizationById(id:Int): Organization?
    fun insertOrganization(organization: Organization): Int
    fun updateOrganization(organization: Organization)
    fun deleteOrganization(id: Int)
}