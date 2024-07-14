package hr.kotwave.scorpiongym.typeoforganization

import androidx.compose.runtime.mutableStateListOf
import org.koin.core.component.KoinComponent

class TypeOfOrganizationViewModel(private val typeOfOrganizationDao: TypeOfOrganizationDao) : KoinComponent {
    private val _organizationTypes = mutableStateListOf<TypeOfOrganization>()
    val organizationTypes: List<TypeOfOrganization> get() = _organizationTypes

    init {
        getTypeOfOrganizations()
    }

    private fun getTypeOfOrganizations() {
        val loadedOrganizations = typeOfOrganizationDao.getAllTypesOfOrganizations()
        _organizationTypes.addAll(loadedOrganizations)
    }

    fun addTypeOfOrganization(typeOfOrganization: TypeOfOrganization) {
        typeOfOrganization.id = typeOfOrganizationDao.insertTypeOfOrganization(typeOfOrganization)
        _organizationTypes.add(typeOfOrganization)
    }

    fun deleteTypeOfOrganization(typeOfOrganization: TypeOfOrganization) {
        typeOfOrganizationDao.deleteTypeOfOrganization(typeOfOrganization.id)
        _organizationTypes.remove(typeOfOrganization)
    }

    fun updateTypeOfOrganization(typeOfOrganization: TypeOfOrganization) {
        typeOfOrganizationDao.updateTypeOfOrganization(typeOfOrganization)
        val index = _organizationTypes.indexOfFirst { it.id == typeOfOrganization.id }
        if (index != -1) {
            _organizationTypes[index] = typeOfOrganization
        }
    }
}