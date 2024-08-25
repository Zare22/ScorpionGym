package hr.kotwave.scorpiongym.util

import java.util.prefs.Preferences

class PreferencesHelper {
    private val preferences: Preferences = Preferences.userRoot().node(this.javaClass.name)

    companion object {
        private const val DARK_THEME_KEY = "dark_theme"
        private const val USER_ID_KEY = "logged_in_user_id"
        private const val IS_ADMIN_KEY = "is_admin"
    }

    var isDarkTheme: Boolean
        get() = preferences.getBoolean(DARK_THEME_KEY, false)
        set(value) {
            preferences.putBoolean(DARK_THEME_KEY, value)
        }

    var loggedInUserId: Int?
        get() {
            val userId = preferences.getInt(USER_ID_KEY, -1)
            return if (userId == -1) null else userId
        }
        set(value) {
            if (value != null) {
                preferences.putInt(USER_ID_KEY, value)
            } else {
                preferences.remove(USER_ID_KEY)
            }
        }

    var isAdmin: Boolean
        get() = preferences.getBoolean(IS_ADMIN_KEY, false)
        set(value) {
            preferences.putBoolean(IS_ADMIN_KEY, value)
        }

    fun clearUser() {
        preferences.remove(USER_ID_KEY)
        preferences.remove(IS_ADMIN_KEY)
    }
}
