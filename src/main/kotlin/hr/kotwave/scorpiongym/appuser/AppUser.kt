package hr.kotwave.scorpiongym.appuser

data class AppUser(
    val id: Int = 0,
    val username: String,
    val password: String = "",
    val isAdmin: Boolean = false
)