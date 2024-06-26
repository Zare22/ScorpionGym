package hr.kotwave.scorpiongym.typeoforganization

import androidx.compose.runtime.mutableStateListOf
import org.koin.core.component.KoinComponent

class TypeOfOrganizationViewModel(private val typeOfOrganizationDao: TypeOfOrganizationDao) : KoinComponent {
    private val _organizationTypes = mutableStateListOf<TypeOfOrganization>()
    val organizationTypes: List<TypeOfOrganization> get() = _organizationTypes

    init {
        getOrganizations()
    }

    private fun getOrganizations() {
        val loadedOrganizations = typeOfOrganizationDao.getAllTypesOfOrganizations()
        _organizationTypes.addAll(loadedOrganizations)
    }

    fun addOrganization(typeOfOrganization: TypeOfOrganization) {
        typeOfOrganizationDao.insertTypeOfOrganization(typeOfOrganization)
        _organizationTypes.add(typeOfOrganization)
    }

    fun deleteOrganization(typeOfOrganization: TypeOfOrganization) {
        typeOfOrganizationDao.deleteTypeOfOrganization(typeOfOrganization.id)
        _organizationTypes.remove(typeOfOrganization)
    }

    fun updateOrganization(typeOfOrganization: TypeOfOrganization) {
        typeOfOrganizationDao.updateTypeOfOrganization(typeOfOrganization)
        val index = _organizationTypes.indexOfFirst { it.id == typeOfOrganization.id }
        if (index != -1) {
            _organizationTypes[index] = typeOfOrganization
        }
    }
}