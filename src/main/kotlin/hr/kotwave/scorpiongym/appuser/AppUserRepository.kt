package hr.kotwave.scorpiongym.appuser

interface AppUserRepository {
    fun loginAppUser(username:String, password:String):AppUser
    fun registerAppUser(username:String, password:String):AppUser
}