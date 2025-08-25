package com.stephenmorgandevelopment.thelinuxmanual

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LazyListStateWithId
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageSearchState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ActivityViewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.LookupViewModel
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.UbuntuManPageTheme
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.PagerNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val activityViewModel: ActivityViewModel by viewModels()
    private val lookupViewModel: LookupViewModel by viewModels()

    private val parcelableMapSaver =
        Saver<Map<Long, ManPageSearchState>, Array<ManPageSearchState>>(
            save = {
                it.map { entry -> entry.value }.toTypedArray()
            },
            restore = {
                it.associateBy { searchState -> searchState.id }
            }
        )

    private val lazyListStateMapSavable =
        Saver<Map<Long, LazyListState>, Array<LazyListStateWithId>>(
            save = {
                it.map { entry ->
                    LazyListStateWithId(
                        id = entry.key,
                        firstVisibleItemIndex = entry.value.firstVisibleItemIndex,
                        firstVisibleItemScrollOffset = entry.value.firstVisibleItemScrollOffset,
                    )
                }.toTypedArray()
            },
            restore = {
                it.associateBy { stateWithId -> stateWithId.id }
                    .mapValues { entry ->
                        LazyListState(
                            entry.value.firstVisibleItemIndex,
                            entry.value.firstVisibleItemScrollOffset,
                        )
                    }
            }
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(0xededed)
            )

            var searchStates: Map<Long, ManPageSearchState> by rememberSaveable(
                stateSaver = parcelableMapSaver,
            ) { mutableStateOf(mapOf()) }

            var listStateMap: Map<Long, LazyListState> by rememberSaveable(
                stateSaver = lazyListStateMapSavable,
            ) { mutableStateOf(mapOf()) }

            CompositionLocalProvider(
                LocalLifecycleOwner provides this,
                LocalActivity provides this,
                LocalViewModelStoreOwner provides this as ViewModelStoreOwner,
            ) {
                UbuntuManPageTheme {
                    PagerNavHost(
                        activityViewModel = activityViewModel,
                        lookupViewModel = lookupViewModel,
                        searchStates = searchStates.toMap(),
                        listStateMap = listStateMap,
                        onFinish = { finish() },
                        updateSearchState = {
                            searchStates.toMutableMap().let { mutableMap ->
                                mutableMap[it.id] = it
                                searchStates = mutableMap
                            }
                        },
                        updateListMap = { id, listState ->
                            listStateMap.toMutableMap().let { mutableMap ->
                                mutableMap[id] = listState
                                listStateMap = mutableMap.toMap()
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            if (!CommandSyncService.isWorking()) {
                activityViewModel.onAction(MainScreenAction.CloseDatabase)
            }
        }
    }
}
