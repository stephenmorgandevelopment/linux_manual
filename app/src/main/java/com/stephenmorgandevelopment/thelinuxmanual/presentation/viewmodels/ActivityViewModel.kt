package com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.domain.GetSectionsById
import com.stephenmorgandevelopment.thelinuxmanual.domain.SyncDatabase
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.AddTab
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.CloseDatabase
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.ShowOfflineDialog
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.TabSelected
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenOptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenOptionsMenuAction.ShowPrivacyPolicyDialog
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ScreenState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ShowDialogEvents
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ShowDialogEvents.NoInternet
import com.stephenmorgandevelopment.thelinuxmanual.presentation.TabInfo
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences
import com.stephenmorgandevelopment.thelinuxmanual.utils.launchInCompletable
import com.stephenmorgandevelopment.thelinuxmanual.utils.stringFromRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val preferences: Preferences,
    private val syncDatabase: SyncDatabase,
    private val getSectionsById: GetSectionsById,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ScreenState, MainScreenAction>(savedStateHandle = savedStateHandle) {
    private var syncJob: CompletableJob? = null

    private val _preferencesFlow = preferences.preferenceListener
    private val _selectedTab = MutableStateFlow(0)
    private val _tabs = MutableStateFlow(listOf(lookupTab))
    private val _syncProgress = MutableStateFlow<String?>(null)

    private val initialState = savedStateHandle[FULL_STATE_KEY] ?: ScreenState(
        title = stringFromRes(R.string.app_name),
        subtitle = preferences.currentRelease.replaceFirstChar { it.uppercase() },
        selectedTabIndex = 0,
        tabs = _tabs.value,
        tabsOnBottom = preferences.tabsOnBottom,
        searchOnBottom = preferences.searchOnBottom,
        syncProgress = null,
    )

    private val _showDialogEvent =
        MutableSharedFlow<ShowDialogEvents>(0, 1, BufferOverflow.DROP_OLDEST)

    val showDialogEvent = _showDialogEvent.asSharedFlow()

    override val state: StateFlow<ScreenState> = combine(
        _preferencesFlow, _selectedTab, _tabs, _syncProgress
    ) { prefs, selectedTab, tabs, syncProgress ->
        val title =
            if (selectedTab == 0) {
                stringFromRes(R.string.app_name)
            } else {
                tabs[selectedTab].title
            }

        ScreenState(
            title = title,
            subtitle = prefs.release.replaceFirstChar { it.uppercase() },
            selectedTabIndex = selectedTab,
            tabs = tabs,
            tabsOnBottom = prefs.tabsOnBottom,
            searchOnBottom = prefs.searchOnBottm,
            syncProgress = syncProgress,
        ).also { persistState(it) }
    }.stateIn(viewModelScope, started = SharingStarted.Lazily, initialState)

    init {
        viewModelScope.launch { syncIfNoData() }
    }

    override fun onAction(action: MainScreenAction) {
        when (action) {
            is TabSelected -> changeSelectedTabTo(action.tabIndex)
            CloseDatabase -> syncDatabase.closeDatabase().start()
            is AddTab -> addTab(action.title, action.itemId).start()
            MainScreenAction.CloseTab -> closeTab().start()
            is ShowOfflineDialog -> {
                if (currentTab.manPageId == action.manPageId) _showDialogEvent.tryEmit(NoInternet)
            }
        }
    }

    override fun onOptionMenuAction(action: OptionsMenuAction) {
        when (action) {
            MainScreenOptionsMenuAction.ReSync -> resyncCurrentRelease()
            is MainScreenOptionsMenuAction.ChangeVersion -> switchToVersion(action.version)
            MainScreenOptionsMenuAction.ToggleTabsOnBottom -> toggleTabsOnBottom().start()
            MainScreenOptionsMenuAction.ToggleSearchOnBottom -> toggleSearchOnBottom().start()
            ShowPrivacyPolicyDialog -> _showDialogEvent.tryEmit(ShowDialogEvents.PrivacyPolicy)
        }
    }

    private fun closeTab() = viewModelScope.async(Dispatchers.IO) {
        with(state.value) {
            val currentTabManPageId = tabs[selectedTabIndex].manPageId
            if (_selectedTab.value == _tabs.value.lastIndex) _selectedTab.value =
                selectedTabIndex.minus(1)
            _tabs.value = state.value.tabs.filterNot { tab -> tab.manPageId == currentTabManPageId }
        }
    }

    private fun addTab(title: String, itemId: Long) = viewModelScope.async {
        withContext(Dispatchers.IO) {
            if (_tabs.value.none { it.manPageId == itemId }) {
                _tabs.value =
                    listOf(
                        *_tabs.value.toTypedArray(),
                        TabInfo(title, itemId, getSectionsById(itemId)),
                    )
            }
        }
    }

    private fun switchToVersion(version: String) {
        _tabs.value = listOf(lookupTab)
        syncJob?.cancel()
        syncDatabase(version)
        syncJob = trackSyncProgress().apply { invokeOnCompletion { syncJob = null } }
    }

    private fun resyncCurrentRelease() {
        _tabs.value = listOf(lookupTab)
        syncJob?.cancel()
        syncJob = sync().apply { invokeOnCompletion { syncJob = null } }
    }

    private fun changeSelectedTabTo(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    private fun toggleTabsOnBottom() = viewModelScope.async {
        withContext(Dispatchers.IO) {
            with(preferences) { PreferencesWriteAccess().setTabsOnBottom(!tabsOnBottom) }
        }
    }

    private fun toggleSearchOnBottom() = viewModelScope.async {
        withContext(Dispatchers.IO) {
            with(preferences) { PreferencesWriteAccess().setSearchOnBottom(!searchOnBottom) }
        }
    }

    private suspend fun syncIfNoData() = withContext(Dispatchers.IO) {
        if (!syncDatabase.hasData() && !CommandSyncService.isWorking()) {
            if (Helpers.hasInternet()) {
                syncJob = sync().apply { invokeOnCompletion { syncJob = null } }
            } else {
                _syncProgress.value = stringFromRes(R.string.offline_without_database)
            }
        }
    }

    private fun trackSyncProgress(): CompletableJob =
        syncDatabase.progress.onEach { text ->
            withContext(Dispatchers.IO) {
                if (text == CommandSyncService.COMPLETE_TAG) {
                    _syncProgress.emit(null)
                    syncJob?.complete()
                } else {
                    _syncProgress.emit(_syncProgress.value + text)
                }
            }
        }.launchInCompletable(viewModelScope)

    private fun sync(): CompletableJob {
        _tabs.value = listOf(lookupTab)
        syncDatabase()
        return trackSyncProgress()
    }

    private val lookupTab get() = TabInfo(stringFromRes(R.string.search))
    private val currentTab get() = _tabs.value[_selectedTab.value]
}
