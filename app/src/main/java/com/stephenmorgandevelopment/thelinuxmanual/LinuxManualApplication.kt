package com.stephenmorgandevelopment.thelinuxmanual

import android.app.Application
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LinuxManualApplication @Inject constructor() : Application() {
    override fun onCreate() {
        super.onCreate()
        Helpers.init(this)
    }
}