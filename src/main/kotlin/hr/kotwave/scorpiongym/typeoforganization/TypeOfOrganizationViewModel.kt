package hr.kotwave.scorpiongym.typeoforganization

import hr.kotwave.scorpiongym.util.CrudViewModel

class TypeOfOrganizationViewModel(dao: TypeOfOrganizationDao) : CrudViewModel<TypeOfOrganization>(
    loader = dao::getAllTypesOfOrganizations,
    inserter = dao::insertTypeOfOrganization,
    updater = dao::updateTypeOfOrganization,
    deleter = dao::deleteTypeOfOrganization,
) {
    val organizationTypes: List<TypeOfOrganization> get() = items

    fun addTypeOfOrganization(typeOfOrganization: TypeOfOrganization) = add(typeOfOrganization)
    fun updateTypeOfOrganization(typeOfOrganization: TypeOfOrganization) = update(typeOfOrganization)
    fun deleteTypeOfOrganization(typeOfOrganization: TypeOfOrganization) = delete(typeOfOrganization)
}
