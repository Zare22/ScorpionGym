package hr.kotwave.scorpiongym.appuser

interface AppUserDao {
    fun loginAppUser(username:String, password:String):AppUser
    fun registerAppUser(username:String, password:String, isAdmin: Boolean):AppUser
}