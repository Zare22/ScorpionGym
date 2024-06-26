package hr.kotwave.scorpiongym.membership

data class Membership(
    val id: Int,
    val name: String,
    val price: Double,
    val numberOfTrainingsAvailable: Int
)
