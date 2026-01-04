package com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.domain.GetSectionsById
import com.stephenmorgandevelopment.thelinuxmanual.domain.SyncDatabase
import com.stephenmorgandevelopment.thelinuxmanual.presentation.DialogEvent
import com.stephenmorgandevelopment.thelinuxmanual.presentation.DialogEvent.NoInternet
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.AddTab
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.CloseDatabase
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.ShowOfflineDialog
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.TabSelected
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.UpdateSubtitle
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenOptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenOptionsMenuAction.ShowPrivacyPolicyDialog
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ScreenState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.TabInfo
import com.stephenmorgandevelopment.thelinuxmanual.sync.COMPLETE_TAG
import com.stephenmorgandevelopment.thelinuxmanual.sync.CommandSyncWorker
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences
import com.stephenmorgandevelopment.thelinuxmanual.utils.launchInCompletable
import com.stephenmorgandevelopment.thelinuxmanual.utils.stringFromRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    private val _subtitle = MutableStateFlow(
        preferences.currentRelease.replaceFirstChar { it.uppercase() },
    )

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
        MutableSharedFlow<DialogEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    val showDialogEvent = _showDialogEvent.asSharedFlow()

    override val state: StateFlow<ScreenState> = combine(
        _preferencesFlow, _selectedTab, _tabs, _syncProgress, _subtitle
    ) { prefs, selectedTab, tabs, syncProgress, subtitle ->
        val title =
            if (selectedTab == 0) {
                stringFromRes(R.string.app_name)
            } else {
                tabs[selectedTab].title
            }

        ScreenState(
            title = title,
            subtitle =
                if (selectedTab == 0) prefs.release.replaceFirstChar { it.uppercase() }
                else subtitle,
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
            is UpdateSubtitle -> _subtitle.update { action.subtitle }
            is ShowOfflineDialog -> {
                if (currentTab.manPageId == action.manPageId) _showDialogEvent.tryEmit(NoInternet)
            }
        }
    }

    override fun onOptionMenuAction(action: OptionsMenuAction) {
        when (action) {
            MainScreenOptionsMenuAction.CloseTab -> closeTab().start()
            MainScreenOptionsMenuAction.ReSync -> resyncCurrentRelease()
            is MainScreenOptionsMenuAction.ChangeVersion -> switchToVersion(action.version)
            MainScreenOptionsMenuAction.ToggleTabsOnBottom -> toggleTabsOnBottom().start()
            MainScreenOptionsMenuAction.ToggleSearchOnBottom -> toggleSearchOnBottom().start()
            ShowPrivacyPolicyDialog -> _showDialogEvent.tryEmit(DialogEvent.PrivacyPolicy)
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

    private fun addTab(title: String, itemId: Long): Deferred<Unit> = viewModelScope.async {
        withContext(Dispatchers.IO) {
            if (_tabs.value.none { it.manPageId == itemId }) {
                val sections = getSectionsById(itemId)
                if (sections.isEmpty()) {
                    delay(timeMillis = 75)
                    addTab(title, itemId)
                } else {
                    _tabs.value =
                        listOf(
                            *_tabs.value.toTypedArray(),
                            TabInfo(title, itemId, sections),
                        )
                }
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
        if (!syncDatabase.hasData() && !CommandSyncWorker.working) {
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
                if (text == COMPLETE_TAG) {
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
