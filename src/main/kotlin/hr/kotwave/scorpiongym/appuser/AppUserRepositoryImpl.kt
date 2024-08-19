package hr.kotwave.scorpiongym.appuser

class AppUserRepositoryImpl(private val appUserDao: AppUserDao) : AppUserRepository {
    override fun loginAppUser(username: String, password: String): AppUser  = appUserDao.loginAppUser(username, password)
    override fun registerAppUser(username: String, password: String): AppUser = appUserDao.registerAppUser(username, password)
}