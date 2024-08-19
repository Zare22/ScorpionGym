package hr.kotwave.scorpiongym.appuser

import java.security.MessageDigest

class AppUserViewModel(private val appUserDao: AppUserDao) {

    fun registerAppUser(userName: String, password: String) {
        val newUser = createAppUser(userName, password)
        appUserDao.registerAppUser(newUser.username, newUser.password)
    }

    private fun createAppUser(username: String, plainPassword: String): AppUser {
        val hashedPassword = hashPassword(plainPassword)
        return AppUser(username = username, password = hashedPassword)
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}