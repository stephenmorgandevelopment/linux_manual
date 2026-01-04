@file:OptIn(ExperimentalCoroutinesApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.UbuntuHtmlApiConverter.BASE_URL
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.UbuntuHtmlApiConverter.crawlForManDirs
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.UbuntuHtmlApiConverter.crawlForManPages
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences
import com.stephenmorgandevelopment.thelinuxmanual.utils.dlog
import com.stephenmorgandevelopment.thelinuxmanual.utils.elog
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response

internal const val COMPLETE_TAG = "complete"

@HiltWorker
class CommandSyncWorker @AssistedInject constructor(
    @Assisted applicationContext: Context,
    @Assisted params: WorkerParameters,
    private val roomDatabase: SimpleCommandsDatabase,
    private val preferences: Preferences,
    private val httpClient: HttpClient,
) : CoroutineWorker(applicationContext, params) {

    override suspend fun doWork(): Result {
        working = true

        withContext(Dispatchers.IO) {
            syncSimpleCommands().also {
                working = false
                _progress.value = COMPLETE_TAG
            }
        }

        return if (progress.value == COMPLETE_TAG) {
            javaClass.dlog("Sync Successful")
            Result.success().also { working = false }
        } else Result.retry().also { javaClass.dlog("Sync failed..retrying.") }
    }

    private suspend fun syncSimpleCommands(): Unit = withContext(Dispatchers.IO) {
        val dirs = httpClient.fetchDirsHtml().await()
        mapHtmlToManDirs(dirs)
            .forEach { request ->
                httpClient.client.newCall(request)
                    .execute()
                    .let { processAndSaveResponse(it) }
            }
    }

    private suspend fun processAndSaveResponse(response: Response): Unit =
        withContext(Dispatchers.Default) {
            val reqUrl = response.request.url.toString()

            if (reqUrl.endsWith("3/")) {
                _progress.value = "$PULLED_DATA_FROM$reqUrl$LARGE_DATA$PROCESSING_DATA"
            } else {
                _progress.value = "$PULLED_DATA_FROM$reqUrl$PROCESSING_DATA"
            }

            val pageCommands: List<MatchingItem> =
                crawlForManPages(response.body.string(), reqUrl)

            _progress.value = SAVING_DATA

            pageCommands
                .takeIf { it.isNotEmpty() }
                .run {
                    roomDatabase.dao().insertAll(pageCommands)
                }
        }

    private suspend fun mapHtmlToManDirs(response: Response): List<Request> {
        return withContext(Dispatchers.Default) {
            if (response.isSuccessful && response.code == 200) {
                val url = "${BASE_URL}${preferences.currentRelease}/${Helpers.getLocale()}"

                val dirPaths = crawlForManDirs(response.body.string())
                val requests = mutableListOf<Request>()

                for (path: String in dirPaths) {
                    requests.add(Request.Builder().url("$url/$path").build())
                }

                requests
            } else {
                javaClass.elog("Request unsuccessful: ${response.code}")
                emptyList<Request>()
            }
        }
    }

    companion object {
        private val _progress = MutableStateFlow(
            "Running initial sync to build local command database.",
        )

        val progress = _progress.asStateFlow()

        var working = false
            private set

        private const val PROCESSING_DATA = "\nProcessing data..."
        private const val PULLED_DATA_FROM = "\nPulled data from "
        private const val LARGE_DATA = "\nLarge data set, longest processing."
        private const val SAVING_DATA = "\nSaving data locally..."
    }
}