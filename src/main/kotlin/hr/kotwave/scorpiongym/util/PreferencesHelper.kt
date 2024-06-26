package hr.kotwave.scorpiongym.util

import java.util.prefs.Preferences

class PreferencesHelper {
    private val preferences: Preferences = Preferences.userRoot().node(this.javaClass.name)

    companion object {
        private const val DARK_THEME_KEY = "dark_theme"
    }

    var isDarkTheme: Boolean
        get() = preferences.getBoolean(DARK_THEME_KEY, false)
        set(value) {
            preferences.putBoolean(DARK_THEME_KEY, value)
        }
}
