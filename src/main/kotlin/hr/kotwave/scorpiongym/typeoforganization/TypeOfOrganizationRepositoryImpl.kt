package hr.kotwave.scorpiongym.typeoforganization

class TypeOfOrganizationRepositoryImpl(private val typeOfOrganizationDao: TypeOfOrganizationDao) :
    TypeOfOrganizationRepository {
    override fun getAllTypesOfOrganizations(): List<TypeOfOrganization> =
        typeOfOrganizationDao.getAllTypesOfOrganizations()

    override fun getTypeOfOrganizationById(id: Int): TypeOfOrganization? =
        typeOfOrganizationDao.getTypeOfOrganizationById(id)

    override fun insertTypeOfOrganization(typeOfOrganization: TypeOfOrganization): Int =
        typeOfOrganizationDao.insertTypeOfOrganization(typeOfOrganization)

    override fun updateTypeOfOrganization(typeOfOrganization: TypeOfOrganization) =
        typeOfOrganizationDao.updateTypeOfOrganization(typeOfOrganization)

    override fun deleteTypeOfOrganization(typeOfOrganization: TypeOfOrganization) =
        typeOfOrganizationDao.deleteTypeOfOrganization(typeOfOrganization)
}