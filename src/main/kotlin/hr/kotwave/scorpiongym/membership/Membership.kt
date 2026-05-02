package hr.kotwave.scorpiongym.membership

import hr.kotwave.scorpiongym.util.Identifiable

data class Membership(
    override var id: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val numberOfTrainingsAvailable: Int = 0,
    val duration: Long = 1,
    val isNoLimit: Boolean = false
) : Identifiable
