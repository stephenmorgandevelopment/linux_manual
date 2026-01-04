package com.stephenmorgandevelopment.thelinuxmanual.repos

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.data.LocalStorage
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.UbuntuHtmlApiConverter
import com.stephenmorgandevelopment.thelinuxmanual.models.Command
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient
import com.stephenmorgandevelopment.thelinuxmanual.sync.CommandSyncWorker
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers
import com.stephenmorgandevelopment.thelinuxmanual.utils.dlog
import com.stephenmorgandevelopment.thelinuxmanual.utils.ilog
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNotNull
import com.stephenmorgandevelopment.thelinuxmanual.utils.stringFromRes
import com.stephenmorgandevelopment.thelinuxmanual.utils.wlog
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class UbuntuRepository @Inject constructor(
    private val httpClient: HttpClient,
    private val roomDb: SimpleCommandsDatabase,
    private val localStorage: LocalStorage,
) {

    suspend fun getMatchingItemById(id: Long): MatchingItem? = withContext(Dispatchers.IO) {
        roomDb.dao().getCommandBy(id)
    }

    suspend fun getCommandData(simpleCommand: MatchingItem): Map<String, String> =
        withContext(Dispatchers.IO) {
            if (localStorage.hasCommand(simpleCommand.id)) {
                return@withContext getCommandFromStorage(simpleCommand.id)
            }

            if (Helpers.hasInternet()) {
                return@withContext fetchCommandData(simpleCommand.url)
                    .also { saveCommandInBackground(simpleCommand.id, it) }
            } else {
                return@withContext emptyMap<String, String>()
            }
        }

    suspend fun addDescription(matchedItem: MatchingItem): MatchingItem {
        return withContext(Dispatchers.Default) {
            httpClient.fetchDescription(matchedItem).await()
                ?.let {
                    UbuntuHtmlApiConverter.crawlForDescriptionAndSections(it.body?.string())
                }?.let { pair ->
                    val description = AnnotatedString.fromHtml(
                        pair.first ?: stringFromRes(R.string.no_description)
                    ).text.run { substring(0, min(length, 160)) }

                    val sections = pair.second.filterNotNull().toList()

                    matchedItem.copy(descriptionPreview = description, sections = sections)
                }?.also {
                    this.async(Dispatchers.IO) { roomDb.dao().update(it) }.start()
                } ?: matchedItem
        }
    }

    private val syncRequest
        get() = OneTimeWorkRequestBuilder<CommandSyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build().also {
                javaClass.ilog("Creating new instance of sync request.")
            }

    internal fun startSync(): Unit {
        WorkManager.getInstance(Helpers.getApplicationContext())
            .enqueue(syncRequest)
    }

    fun launchSyncWorker(): Flow<String> {
        if (!CommandSyncWorker.working) {
            startSync()
        }

        return CommandSyncWorker.progress
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveCommandInBackground(id: Long, data: Map<String, String>) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                localStorage.saveCommand(
                    Command(id, data)
                )
            }
        }.invokeOnCompletion { e ->
            if (e.isNotNull()) javaClass.dlog("Encountered error saving $id - ${e?.message}")
        }
    }

    private suspend fun getCommandFromStorage(id: Long): Map<String, String> {
        return try {
            withContext(Dispatchers.IO) {
                localStorage.loadCommand(id).data
            }
        } catch (e: IOException) {
            javaClass.wlog("Unexpected file error loading - $id: $e")
            emptyMap<String, String>()
        }
    }

    private suspend fun fetchCommandData(pageUrl: String): Map<String, String> {
        return try {
            httpClient.fetchCommandManPage(pageUrl)
                .doOnError {}
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMap(UbuntuHtmlApiConverter::crawlForCommandInfo)
                .await()
        } catch (e: Throwable) {
            javaClass.wlog("Error fetching command data: ${e.message}")
            emptyMap()
        }
    }
}
