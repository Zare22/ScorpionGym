package hr.kotwave.scorpiongym.appuser

import hr.kotwave.scorpiongym.util.PreferencesHelper
import java.security.MessageDigest

class AppUserViewModel(private val appUserDao: AppUserDao) {

    val allUsers: List<AppUser> by lazy {
        appUserDao.getAllUsers()
    }

    fun loginAppUser(username: String, password: String) : Boolean {
        val hashedPassword = hashPassword(password)
        val user = appUserDao.loginAppUser(username, hashedPassword)
        return run {
            PreferencesHelper().loggedInUserId = user.id
            PreferencesHelper().isAdmin = user.isAdmin
            true
        }
    }

    fun getAllActivityLogs(): List<Triple<String, String, String>> = appUserDao.getAllActivityLogs()

    fun registerAppUser(userName: String, password: String, isAdmin: Boolean = false) {
        val newUser = createAppUser(userName, password, isAdmin)
        appUserDao.registerAppUser(newUser.username, newUser.password, isAdmin)
    }

    private fun createAppUser(username: String, plainPassword: String, isAdmin: Boolean): AppUser {
        val hashedPassword = hashPassword(plainPassword)
        return AppUser(username = username, password = hashedPassword, isAdmin = isAdmin)
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}