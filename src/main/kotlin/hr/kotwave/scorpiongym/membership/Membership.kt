package hr.kotwave.scorpiongym.membership

data class Membership(
    var id: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val numberOfTrainingsAvailable: Int = 0
)
