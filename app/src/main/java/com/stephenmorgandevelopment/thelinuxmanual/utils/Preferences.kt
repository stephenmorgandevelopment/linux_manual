@file:Suppress("DEPRECATION")
@file:SuppressLint("UseKtx")
@file:OptIn(DelicateCoroutinesApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.AvailableReleases.NOBLE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class CurrentPreferences(
    val release: String,
    val tabsOnBottom: Boolean,
    val searchOnBottm: Boolean,
)

@Singleton
class Preferences @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) {
    private val currentPrefs get() = CurrentPreferences(release, tabsOnBottom, searchOnBottom)
    private val _preferenceListener = MutableStateFlow(currentPrefs)
    val preferenceListener = _preferenceListener.asStateFlow()

    /**
     * Added due to issues that arise in the http/database creation and usage,
     * when having to wait for the shared prefs to come from the disk.
     */
    val currentRelease
        get() = preferenceListener.value.release

    private val preferences: SharedPreferences?
        get() = try {
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        } catch (e: Throwable) {
            null
        }

    var release: String
        private set(value) {
            preferences?.edit()?.putString(RELEASE, value)?.commit().also {
                if (it == true) _preferenceListener.tryEmit(currentPrefs)
            } ?: javaClass.wlog("Failed storing release value in shared prefs.")
        }
        get() {
            return (preferences?.getString(RELEASE, NOBLE.pathString)
                ?: NOBLE.pathString)
        }

    var tabsOnBottom: Boolean
        private set(value) {
            preferences?.edit()?.putBoolean(TABS_ON_BOTTOM, value)?.commit().also {
                if (it == true) _preferenceListener.tryEmit(currentPrefs)
            } ?: javaClass.wlog("Failed to store release value in shared prefs.")
        }
        get() {
            return preferences?.getBoolean(TABS_ON_BOTTOM, false) == true
        }

    var searchOnBottom: Boolean
        private set(value) {
            preferences?.edit()?.putBoolean(SEARCH_ON_BOTTOM, value)?.commit().also {
                if (it == true) _preferenceListener.tryEmit(currentPrefs)
            } ?: javaClass.wlog("Failed to store release value in shared prefs.")
        }
        get() {
            return preferences?.getBoolean(SEARCH_ON_BOTTOM, false) == true
        }

    inner class PreferencesWriteAccess() {
        fun setRelease(versionRelease: String) = GlobalScope.async {
            withContext(Dispatchers.IO) {
                release = versionRelease
            }
        }

        fun setTabsOnBottom(enabled: Boolean) = GlobalScope.async {
            withContext(Dispatchers.IO) {
                tabsOnBottom = enabled
            }
        }

        fun setSearchOnBottom(enabled: Boolean) = GlobalScope.async {
            withContext(Dispatchers.IO) {
                searchOnBottom = enabled
            }
        }
    }

    companion object {
        private const val LOG_TAG = "UbuntuManPages"
        private const val RELEASE: String = "RELEASE"
        private const val TABS_ON_BOTTOM: String = "TABS_ON_BOTTOM"
        private const val SEARCH_ON_BOTTOM: String = "SEARCH_ON_BOTTOM"
    }
}