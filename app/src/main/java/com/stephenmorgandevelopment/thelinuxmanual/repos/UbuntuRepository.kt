package com.stephenmorgandevelopment.thelinuxmanual.repos

import android.content.Intent
import androidx.lifecycle.asFlow
import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService
import com.stephenmorgandevelopment.thelinuxmanual.data.LocalStorage
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers
import com.stephenmorgandevelopment.thelinuxmanual.utils.ilog
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UbuntuRepository @Inject constructor(
    private val httpClient: HttpClient,
    private val roomDb: SimpleCommandsDatabase,
    private val localStorage: LocalStorage,
) {

    suspend fun getCommandData(simpleCommand: SimpleCommand): Map<String, String> =
        withContext(Dispatchers.IO) {
            if (localStorage.hasCommand(simpleCommand.id)) {
                return@withContext getCommandFromStorage(simpleCommand)
            }

            if (Helpers.hasInternet()) {
                return@withContext fetchCommandData(simpleCommand.url)
            }

            return@withContext emptyMap()
        }

    suspend fun addDescription(matchedItem: MatchingItem): MatchingItem? {
        return withContext(Dispatchers.IO) {
            httpClient.fetchDescription(matchedItem.toSimpleCommand()).await()
                ?.let {
                    UbuntuHtmlApiConverter.crawlForDescription(it.body?.string())
                }?.let {
                    matchedItem.copy(description = it)
                }?.also {
                    roomDb.dao().update(it)
//                    roomDb.dao().delete(it)
//                    roomDb.dao().insert(it)
                }
        }
    }

    fun launchSyncService(): Flow<String> {
        if (!CommandSyncService.isWorking()) {
            val intent = Intent().apply {
                putExtra(CommandSyncService.DISTRO, UbuntuHtmlApiConverter.NAME)
            }

            return CommandSyncService.enqueueWork(
                Helpers.getApplicationContext(),
                intent
            ).asFlow()
        }

        return CommandSyncService.getProgress().asFlow()
    }

    private suspend fun getCommandFromStorage(simpleCommand: SimpleCommand): Map<String, String> {
        return try {
            localStorage.loadCommand(simpleCommand.id).data
        } catch (e: IOException) {
            javaClass.ilog("Unexpected file error loading - ${simpleCommand.name}: $e")
            emptyMap<String, String>()
        }
    }

    private suspend fun fetchCommandData(pageUrl: String): Map<String, String> {
        return httpClient.fetchCommandManPage(pageUrl)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .flatMap(UbuntuHtmlApiConverter::crawlForCommandInfo)
            .await()
    }
}