package hr.kotwave.scorpiongym.otherservice

import hr.kotwave.scorpiongym.util.Identifiable

data class OtherService(
    override var id: Int = 0,
    val name: String,
    val price: Double
) : Identifiable