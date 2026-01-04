package com.stephenmorgandevelopment.thelinuxmanual

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import javax.inject.Inject

@HiltAndroidApp
class LinuxManualApplication @Inject constructor() : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        Helpers.init(this)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    override val workManagerConfiguration =
        Configuration.Builder()
            .setExecutor(Dispatchers.Default.asExecutor())
            .setWorkerFactory(
                EntryPoints.get(
                    this,
                    HiltWorkerFactoryEntryPoint::class.java,
                ).workerFactory()
            )
            .setTaskExecutor(Dispatchers.Default.asExecutor())
            .setMaxSchedulerLimit(Configuration.MIN_SCHEDULER_LIMIT)
            .build()
}
