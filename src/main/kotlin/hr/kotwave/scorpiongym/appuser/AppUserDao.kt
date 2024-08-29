package hr.kotwave.scorpiongym.appuser

interface AppUserDao {
    fun getAllUsers(): ArrayList<AppUser>
    fun loginAppUser(username:String, password:String):AppUser
    fun registerAppUser(username:String, password:String, isAdmin: Boolean):AppUser
    fun getUserActivityLogs(appUserId: Int): List<Pair<String, String>>
}