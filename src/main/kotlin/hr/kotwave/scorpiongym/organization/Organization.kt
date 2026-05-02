package hr.kotwave.scorpiongym.organization

import hr.kotwave.scorpiongym.util.Identifiable

data class Organization(
    override var id: Int = 0,
    val name: String = "",
    val typeOfOrganizationId: Int? = null
) : Identifiable
