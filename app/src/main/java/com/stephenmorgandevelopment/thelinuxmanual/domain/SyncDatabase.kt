package com.stephenmorgandevelopment.thelinuxmanual.domain

//import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper
import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService
import com.stephenmorgandevelopment.thelinuxmanual.data.LocalStorage
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences
import com.stephenmorgandevelopment.thelinuxmanual.utils.ilog
import com.stephenmorgandevelopment.thelinuxmanual.utils.launchCompletable
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncDatabase @Inject constructor(
    private val preferences: Preferences,
    private val ubuntuRepository: UbuntuRepository,
    private val roomDatabase: SimpleCommandsDatabase,
    private val localStorage: LocalStorage,
) {
    private val syncScope = CoroutineScope(Dispatchers.Default)
    private var syncJob: CompletableJob? = null

    private val _progress: MutableSharedFlow<String> =
        MutableSharedFlow(1, 0, BufferOverflow.DROP_OLDEST)

    val progress: SharedFlow<String> = _progress.asSharedFlow()

    operator fun invoke(releaseName: String? = null): SharedFlow<String> {
        syncJob = syncScope.launchCompletable {
            withContext(Dispatchers.IO) {
                if (Helpers.hasInternet()) {
                    releaseName?.let { preferences.PreferencesWriteAccess().setRelease(it).await() }
                    if (roomDatabase.hasData()) roomDatabase.dao().wipeTable()
                    localStorage.wipeAll()
                    if (roomDatabase.hasData()) roomDatabase.dao().wipeTable()
                    else javaClass.ilog("Cleared room db successfully the first time.")
                    sync()
                } else {
                    _progress.emit(CommandSyncService.COMPLETE_TAG)
                }
            }
        }
        return progress
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun closeDatabase() = GlobalScope.async {
        withContext(Dispatchers.IO) {
            roomDatabase.close()
        }
    }

    suspend fun hasData(): Boolean = roomDatabase.hasData()

    private suspend fun sync() {
        withContext(Dispatchers.Main) {
            ubuntuRepository.launchSyncService()
                .onEach { text ->
                    _progress.emit(text)
                    if (text == CommandSyncService.COMPLETE_TAG) {
                        syncJob?.complete()
                    }
                }.collect()
        }
    }
}
