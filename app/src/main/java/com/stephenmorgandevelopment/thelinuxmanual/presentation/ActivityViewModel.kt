package com.stephenmorgandevelopment.thelinuxmanual.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase
import com.stephenmorgandevelopment.thelinuxmanual.domain.SyncDatabase
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.ChangeVersion
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.CloseDatabase
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.ReSync
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.TabSelected
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.ToggleSearchOnBottom
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.ToggleTabsOnBottom
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences
import com.stephenmorgandevelopment.thelinuxmanual.utils.launchInCompletable
import com.stephenmorgandevelopment.thelinuxmanual.utils.stringFromRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val preferences: Preferences,
    private val syncDatabase: SyncDatabase,
    private val roomDatabase: SimpleCommandsDatabase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ScreenState, MainScreenAction>(savedStateHandle = savedStateHandle) {
    private var syncJob: CompletableJob? = null
    private val initialState: ScreenState = savedStateHandle[FULL_STATE_KEY] ?: ScreenState(
        "Ubuntu Test Pages",
        savedStateHandle[SELECTED_TAB_INDEX_KEY] ?: 0,
        listOf(PagerTab(stringFromRes(R.string.search))),
        preferences.tabsOnBottom,
        preferences.searchOnBottom,
        null,
    )

    override val _state: MutableStateFlow<ScreenState> = MutableStateFlow(initialState)
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    init {
        preferences.preferenceListener.onEach { prefs ->
            _state.update {
                it.copy(tabsOnBottom = prefs.tabsOnBottom, searchOnBottom = prefs.searchOnBottm)
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch { syncIfNoData() }
    }

    override fun onAction(action: MainScreenAction) {
        when (action) {
            is TabSelected -> changeTabsTo(action.tabIndex)
            is ChangeVersion -> switchToVersion(action.version)
            ToggleTabsOnBottom -> toggleTabsOnBottom()
            ToggleSearchOnBottom -> toggleSearchOnBottom()
            ReSync -> resyncCurrentRelease()
            CloseDatabase -> roomDatabase.close()
            is MainScreenAction.ItemClick -> itemClicked(action.itemId, action.url)
        }
    }

    private fun itemClicked(itemId: Long, url: String) {

    }

    private fun switchToVersion(version: String) {
        syncJob?.cancel()
        syncDatabase(version)
        syncJob = trackSyncProgress().apply { invokeOnCompletion { syncJob = null } }
    }

    private fun resyncCurrentRelease() {
        syncJob?.cancel()
        syncJob = sync().apply { invokeOnCompletion { syncJob = null } }
    }

    private fun changeTabsTo(tabIndex: Int) {
        _state.update { it.copy(selectedTabIndex = tabIndex) }
        savedStateHandle[SELECTED_TAB_INDEX_KEY] = tabIndex
    }

    private fun toggleTabsOnBottom() {
        with(preferences) { PreferencesWriteAccess().setTabsOnBottom(!tabsOnBottom) }
    }

    private fun toggleSearchOnBottom() {
        with(preferences) { PreferencesWriteAccess().setSearchOnBottom(!searchOnBottom) }
    }

    private suspend fun syncIfNoData() {
        if (!roomDatabase.hasData() && !CommandSyncService.isWorking() && syncJob?.isActive != true) {
            syncJob = sync().apply { invokeOnCompletion { syncJob = null } }
        }
    }

    private fun trackSyncProgress(): CompletableJob =
        syncDatabase.progress.onEach { text ->
            if (text == CommandSyncService.COMPLETE_TAG) {
                _state.update { it.copy(syncProgress = null) }
                syncJob?.complete()
            } else {
                _state.update { it.copy(syncProgress = it.syncProgress + "\n$text") }
            }
        }.launchInCompletable(viewModelScope)

    private fun sync(): CompletableJob {
        syncDatabase()
        return trackSyncProgress()
    }

    companion object {
        private const val SELECTED_TAB_INDEX_KEY = "SELECTED_TAB_INDEX_KEY"
    }
}
