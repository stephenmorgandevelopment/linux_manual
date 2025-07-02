@file:Suppress("DEPRECATION")
@file:SuppressLint("UseKtx")

package com.stephenmorgandevelopment.thelinuxmanual.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.stephenmorgandevelopment.thelinuxmanual.distros.AvailableReleases.NOBLE
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers.getApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class CurrentPreferences(
    val release: String,
    val tabsOnBottom: Boolean,
    val searchOnBottm: Boolean,
)

@Singleton
class Preferences @Inject constructor() {
    private val currentPrefs get() = CurrentPreferences(release, tabsOnBottom, searchOnBottom)
    private val _preferenceListener = MutableStateFlow(currentPrefs)
    val preferenceListener = _preferenceListener.asStateFlow()

    /**
     * Added due to issues that arise in the http/database creation and usage,
     * when having to wait for the shared prefs to come from the disk.
     */
    val currentRelease
        get() = preferenceListener.value.release
            .also { staticReleaseString = it }

    private val preferences: SharedPreferences?
        get() = try {
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        } catch (e: Throwable) {
            null
        }

    var release: String
        private set(value) {
            preferences?.edit()?.putString(RELEASE, value)?.commit().also {
                if (it == true) _preferenceListener.tryEmit(currentPrefs)
            } ?: Log.w(LOG_TAG, "Failed to store release value in shared prefs.")
        }
        get() {
            return (preferences?.getString(RELEASE, NOBLE.pathString)
                ?: NOBLE.pathString)
        }

    var tabsOnBottom: Boolean
        private set(value) {
            preferences?.edit()?.putBoolean(TABS_ON_BOTTOM, value)?.commit().also {
                if (it == true) _preferenceListener.tryEmit(currentPrefs)
            } ?: Log.w(LOG_TAG, "Failed to store release value in shared prefs.")
        }
        get() {
            return preferences?.getBoolean(TABS_ON_BOTTOM, false) == true
        }

    var searchOnBottom: Boolean
        private set(value) {
            preferences?.edit()?.putBoolean(SEARCH_ON_BOTTOM, value)?.commit().also {
                if (it == true) _preferenceListener.tryEmit(currentPrefs)
            } ?: Log.w(LOG_TAG, "Failed to store release value in shared prefs.")
        }
        get() {
            return preferences?.getBoolean(SEARCH_ON_BOTTOM, false) == true
        }

    inner class PreferencesWriteAccess() {
        fun setRelease(versionRelease: String): Unit {
            release = versionRelease
        }

        fun setTabsOnBottom(enabled: Boolean): Unit {
            tabsOnBottom = enabled
        }

        fun setSearchOnBottom(enabled: Boolean): Unit {
            searchOnBottom = enabled
        }
    }

    companion object {
        /**
         * It appears as if Hilt is creating multiple instances for classes, despite being
         * annotated with Singleton or being InstalledIn SingletonComponent.  Opting not to
         * refactor into object.
         *
         * Implementing singleton via instance, returned in provides module..
         */
//        internal val instance: Preferences = Preferences()
        var staticReleaseString: String = NOBLE.pathString
            private set

        private const val LOG_TAG = "UbuntuManPages"
        private const val RELEASE: String = "RELEASE"
        private const val TABS_ON_BOTTOM: String = "TABS_ON_BOTTOM"
        private const val SEARCH_ON_BOTTOM: String = "SEARCH_ON_BOTTOM"
    }
}