package com.stephenmorgandevelopment.thelinuxmanual

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.rememberNavController
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(0xededed)
            )

            val navController = rememberNavController()

            var searchStates: Map<Long, ManPageSearchState> by rememberSaveable(
                stateSaver = parcelableMapSaver,
            ) {
                mutableStateOf(mapOf())
            }

            CompositionLocalProvider(
                LocalLifecycleOwner provides this,
                LocalActivity provides this,
                LocalViewModelStoreOwner provides this as ViewModelStoreOwner,
            ) {
                UbuntuManPageTheme {
                    PagerNavHost(
                        activityViewModel,
                        lookupViewModel,
                        navController,
                        searchStates.toMap(),
                        onFinish = { finish() },
                        updateSearchState = {
                            searchStates.toMutableMap().let { mutableMap ->
                                mutableMap[it.id] = it
                                searchStates = mutableMap
                            }
                        }
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
