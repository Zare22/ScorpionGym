package hr.kotwave.scorpiongym.organization

class OrganizationRepositoryImpl(private val organizationDao: OrganizationDao) : OrganizationRepository {
    override fun getAllOrganizations(): List<Organization> = organizationDao.getAllOrganizations()
    override fun getOrganizationById(id: Int): Organization? = organizationDao.getOrganizationById(id)
    override fun insertOrganization(organization: Organization): Int = organizationDao.insertOrganization(organization)
    override fun updateOrganization(organization: Organization) = organizationDao.updateOrganization(organization)
    override fun deleteOrganization(organization: Organization) = organizationDao.deleteOrganization(organization)
}