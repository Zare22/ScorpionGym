package hr.kotwave.scorpiongym.typeoforganization

interface TypeOfOrganizationDao {
    fun getAllTypesOfOrganizations(): List<TypeOfOrganization>
    fun getTypeOfOrganizationById(id: Int): TypeOfOrganization?
    fun insertTypeOfOrganization(typeOfOrganization: TypeOfOrganization): Int
    fun updateTypeOfOrganization(typeOfOrganization: TypeOfOrganization)
    fun deleteTypeOfOrganization(id: Int)
}