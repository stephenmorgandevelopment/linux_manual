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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageSearchState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuHandler
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ActivityViewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.LookupViewModel
import com.stephenmorgandevelopment.thelinuxmanual.sync.CommandSyncWorker
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.BaseScreen
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.UbuntuManPageTheme
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.misc.lazyListStateMapSavable
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.misc.parcelableMapSaver
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.systemScrimHex
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val activityViewModel: ActivityViewModel by viewModels()
    private val lookupViewModel: LookupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(systemScrimHex)
            )

            var searchStates: Map<Long, ManPageSearchState> by rememberSaveable(
                stateSaver = parcelableMapSaver,
            ) { mutableStateOf(mapOf()) }

            var listStateMap: Map<Long, LazyListState> by rememberSaveable(
                stateSaver = lazyListStateMapSavable,
            ) { mutableStateOf(mapOf()) }

            val optionsMenuHandler = OptionsMenuHandler()

            CompositionLocalProvider(
                LocalLifecycleOwner provides this,
                LocalActivity provides this,
                LocalViewModelStoreOwner provides this as ViewModelStoreOwner,
            ) {
                UbuntuManPageTheme {
                    BaseScreen(
                        activityViewModel = activityViewModel,
                        lookupViewModel = lookupViewModel,
                        optionsMenuHandler = optionsMenuHandler,
                        searchStates = searchStates.toMap(),
                        listStateMap = listStateMap,
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
            if (!CommandSyncWorker.working) {
                activityViewModel.onAction(MainScreenAction.CloseDatabase)
            }
        }
    }
}
