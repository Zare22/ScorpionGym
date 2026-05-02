package hr.kotwave.scorpiongym.typeoforganization

import hr.kotwave.scorpiongym.util.Identifiable

data class TypeOfOrganization(
    override var id: Int = 0,
    val name: String,
    val discountRate: Double
) : Identifiable
